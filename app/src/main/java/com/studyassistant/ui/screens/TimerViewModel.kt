package com.studyassistant.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyassistant.domain.model.TimerState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TimerViewModel : ViewModel() {

    private val _timerState = MutableStateFlow<TimerState>(TimerState.Idle)
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private val _selectedMinutes = MutableStateFlow(25)
    val selectedMinutes: StateFlow<Int> = _selectedMinutes.asStateFlow()

    private val _customMinutes = MutableStateFlow("")
    val customMinutes: StateFlow<String> = _customMinutes.asStateFlow()

    private var timerJob: Job? = null

    fun selectPreset(minutes: Int) {
        _selectedMinutes.value = minutes
        _customMinutes.value = ""
    }

    fun updateCustomMinutes(value: String) {
        _customMinutes.value = value
        if (value.isNotEmpty()) {
            val parsed = value.toIntOrNull() ?: 0
            if (parsed in 1..180) {
                _selectedMinutes.value = parsed
            }
        }
    }

    fun startTimer() {
        val minutes = _selectedMinutes.value
        if (minutes <= 0) return

        timerJob?.cancel()
        val totalSeconds = minutes * 60

        _timerState.value = TimerState.Running(
            totalSeconds = totalSeconds,
            remainingSeconds = totalSeconds
        )

        timerJob = viewModelScope.launch {
            var remaining = totalSeconds
            while (remaining > 0) {
                delay(1000L)
                remaining--
                _timerState.value = TimerState.Running(
                    totalSeconds = totalSeconds,
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

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
