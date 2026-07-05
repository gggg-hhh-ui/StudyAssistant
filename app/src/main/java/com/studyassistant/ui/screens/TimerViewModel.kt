package com.studyassistant.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyassistant.domain.model.GradeLevel
import com.studyassistant.domain.model.TimerState
import com.studyassistant.domain.usecase.MathProblemGenerator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TimerViewModel : ViewModel() {

    private val _timerState = MutableStateFlow<TimerState>(TimerState.Idle)
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    // 计时器设置：小时、分钟、秒
    private val _hours = MutableStateFlow(0)
    val hours: StateFlow<Int> = _hours.asStateFlow()

    private val _minutes = MutableStateFlow(25)
    val minutes: StateFlow<Int> = _minutes.asStateFlow()

    private val _seconds = MutableStateFlow(0)
    val seconds: StateFlow<Int> = _seconds.asStateFlow()

    // 年级范围设置
    private val _startGrade = MutableStateFlow(GradeLevel.GRADE_1)
    val startGrade: StateFlow<GradeLevel> = _startGrade.asStateFlow()

    private val _endGrade = MutableStateFlow(GradeLevel.GRADE_6)
    val endGrade: StateFlow<GradeLevel> = _endGrade.asStateFlow()

    // 题目数量
    private val _problemCount = MutableStateFlow(3)
    val problemCount: StateFlow<Int> = _problemCount.asStateFlow()

    private var timerJob: Job? = null
    private var problemGenerator: MathProblemGenerator = MathProblemGenerator(1, 6)

    // 保存计时器状态用于数学挑战后恢复
    private var savedTimerState: TimerState.Running? = null

    // 计算总秒数
    val totalSeconds: Int
        get() = _hours.value * 3600 + _minutes.value * 60 + _seconds.value

    fun setHours(value: Int) {
        _hours.value = value.coerceIn(0, 24)
    }

    fun setMinutes(value: Int) {
        _minutes.value = value.coerceIn(0, 59)
    }

    fun setSeconds(value: Int) {
        _seconds.value = value.coerceIn(0, 59)
    }

    fun setTimeFromSeconds(totalSecs: Int) {
        _hours.value = totalSecs / 3600
        _minutes.value = (totalSecs % 3600) / 60
        _seconds.value = totalSecs % 60
    }

    fun selectPreset(minutes: Int) {
        _hours.value = 0
        _minutes.value = minutes
        _seconds.value = 0
    }

    fun setStartGrade(grade: GradeLevel) {
        _startGrade.value = grade
        // 确保结束年级不早于起始年级
        if (_endGrade.value.gradeNumber < grade.gradeNumber) {
            _endGrade.value = grade
        }
        updateProblemGenerator()
    }

    fun setEndGrade(grade: GradeLevel) {
        _endGrade.value = grade
        updateProblemGenerator()
    }

    fun setProblemCount(count: Int) {
        _problemCount.value = count
    }

    private fun updateProblemGenerator() {
        problemGenerator = MathProblemGenerator(
            startGrade = _startGrade.value.gradeNumber,
            endGrade = _endGrade.value.gradeNumber
        )
    }

    fun startTimer() {
        val total = totalSeconds
        if (total <= 0) return

        timerJob?.cancel()

        _timerState.value = TimerState.Running(
            totalSeconds = total,
            remainingSeconds = total
        )

        timerJob = viewModelScope.launch {
            var remaining = total
            while (remaining > 0) {
                delay(1000L)
                remaining--
                _timerState.value = TimerState.Running(
                    totalSeconds = total,
                    remainingSeconds = remaining
                )
            }
            _timerState.value = TimerState.Finished
        }
    }

    fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
        _timerState.value = TimerState.Idle
    }

    fun resetAfterFinished() {
        timerJob?.cancel()
        timerJob = null
        _timerState.value = TimerState.Idle
    }

    /**
     * 开始数学挑战模式
     */
    fun startMathChallenge() {
        val currentState = _timerState.value
        if (currentState is TimerState.Running) {
            // 保存当前计时器状态
            savedTimerState = currentState
            timerJob?.cancel()

            // 初始化题目生成器
            updateProblemGenerator()

            // 生成第一道题
            val firstProblem = problemGenerator.generate()
            _timerState.value = TimerState.MathChallenge(
                currentProblem = firstProblem,
                correctCount = 0,
                targetCount = _problemCount.value
            )
        }
    }

    /**
     * 提交答案
     */
    fun submitAnswer(answer: String) {
        val currentState = _timerState.value
        if (currentState is TimerState.MathChallenge) {
            if (answer.isEmpty()) return

            // 支持负数答案
            val userAnswer = if (answer.startsWith("-")) {
                answer.drop(1).toIntOrNull()?.let { -it }
            } else {
                answer.toIntOrNull()
            }

            if (userAnswer == null) {
                _timerState.value = currentState.copy(
                    userAnswer = answer,
                    showWrongFeedback = false
                )
                return
            }

            if (userAnswer == currentState.currentProblem.answer) {
                // 答对了
                val newCorrectCount = currentState.correctCount + 1

                if (newCorrectCount >= currentState.targetCount) {
                    // 全部答对，提前解锁
                    _timerState.value = TimerState.Finished
                } else {
                    // 继续下一题
                    val nextProblem = problemGenerator.generate()
                    _timerState.value = TimerState.MathChallenge(
                        currentProblem = nextProblem,
                        correctCount = newCorrectCount,
                        targetCount = currentState.targetCount,
                        userAnswer = "",
                        showWrongFeedback = false
                    )
                }
            } else {
                // 答错了
                _timerState.value = currentState.copy(
                    userAnswer = answer,
                    showWrongFeedback = true
                )
            }
        }
    }

    /**
     * 跳过当前题目
     */
    fun skipProblem() {
        val currentState = _timerState.value
        if (currentState is TimerState.MathChallenge) {
            val nextProblem = problemGenerator.generate()
            _timerState.value = currentState.copy(
                currentProblem = nextProblem,
                userAnswer = "",
                showWrongFeedback = false
            )
        }
    }

    /**
     * 取消数学挑战，恢复计时器
     */
    fun cancelMathChallenge() {
        savedTimerState?.let { saved ->
            _timerState.value = saved
            // 恢复计时
            timerJob = viewModelScope.launch {
                var remaining = saved.remainingSeconds
                while (remaining > 0) {
                    delay(1000L)
                    remaining--
                    _timerState.value = TimerState.Running(
                        totalSeconds = saved.totalSeconds,
                        remainingSeconds = remaining
                    )
                }
                _timerState.value = TimerState.Finished
            }
        }
        savedTimerState = null
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
