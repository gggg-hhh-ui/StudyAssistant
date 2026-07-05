package com.studyassistant.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyassistant.domain.model.GradeLevel
import com.studyassistant.domain.model.PRESET_DURATIONS
import com.studyassistant.domain.model.TimerState
import com.studyassistant.ui.theme.Amber500
import com.studyassistant.ui.theme.Green500
import com.studyassistant.ui.theme.GreenDark
import com.studyassistant.ui.theme.Red400

@Composable
fun TimerScreen(
    viewModel: TimerViewModel,
    onStartTimer: () -> Unit,
    onCancelTimer: () -> Unit,
    onTimerFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timerState by viewModel.timerState.collectAsState()
    val hours by viewModel.hours.collectAsState()
    val minutes by viewModel.minutes.collectAsState()
    val seconds by viewModel.seconds.collectAsState()
    val startGrade by viewModel.startGrade.collectAsState()
    val endGrade by viewModel.endGrade.collectAsState()
    val problemCount by viewModel.problemCount.collectAsState()

    // Handle back press during focus
    BackHandler(enabled = timerState is TimerState.Running || timerState is TimerState.MathChallenge) {
        // Swallow back press during focus
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (val state = timerState) {
                is TimerState.Idle -> {
                    IdleContent(
                        hours = hours,
                        minutes = minutes,
                        seconds = seconds,
                        startGrade = startGrade,
                        endGrade = endGrade,
                        problemCount = problemCount,
                        onHoursChange = viewModel::setHours,
                        onMinutesChange = viewModel::setMinutes,
                        onSecondsChange = viewModel::setSeconds,
                        onSelectPreset = viewModel::selectPreset,
                        onStartGradeChange = viewModel::setStartGrade,
                        onEndGradeChange = viewModel::setEndGrade,
                        onProblemCountChange = viewModel::setProblemCount,
                        onStart = {
                            viewModel.startTimer()
                            onStartTimer()
                        }
                    )
                }

                is TimerState.Running -> {
                    RunningContent(
                        state = state,
                        onCancel = {
                            viewModel.cancelTimer()
                            onCancelTimer()
                        },
                        onEarlyUnlock = {
                            viewModel.startMathChallenge()
                        }
                    )
                }

                is TimerState.MathChallenge -> {
                    MathChallengeContent(
                        state = state,
                        onAnswer = viewModel::submitAnswer,
                        onSkip = viewModel::skipProblem
                    )
                }

                is TimerState.Finished -> {
                    FinishedContent(
                        onReset = {
                            viewModel.resetAfterFinished()
                            onTimerFinished()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun IdleContent(
    hours: Int,
    minutes: Int,
    seconds: Int,
    startGrade: GradeLevel,
    endGrade: GradeLevel,
    problemCount: Int,
    onHoursChange: (Int) -> Unit,
    onMinutesChange: (Int) -> Unit,
    onSecondsChange: (Int) -> Unit,
    onSelectPreset: (Int) -> Unit,
    onStartGradeChange: (GradeLevel) -> Unit,
    onEndGradeChange: (GradeLevel) -> Unit,
    onProblemCountChange: (Int) -> Unit,
    onStart: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "学习助手",
            style = MaterialTheme.typography.headlineMedium,
            color = Green500,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "专注计时",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 预设时间按钮
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PRESET_DURATIONS.forEach { presetMinutes ->
                PresetChip(
                    minutes = presetMinutes,
                    isSelected = hours == 0 && minutes == presetMinutes && seconds == 0,
                    onClick = { onSelectPreset(presetMinutes) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 自定义时间输入
        Text(
            text = "自定义时间",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TimeInputField(
                value = hours,
                onValueChange = onHoursChange,
                label = "时",
                maxValue = 24,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = ":",
                style = MaterialTheme.typography.headlineMedium,
                color = Green500,
                modifier = Modifier.padding(top = 16.dp)
            )
            TimeInputField(
                value = minutes,
                onValueChange = onMinutesChange,
                label = "分",
                maxValue = 59,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = ":",
                style = MaterialTheme.typography.headlineMedium,
                color = Green500,
                modifier = Modifier.padding(top = 16.dp)
            )
            TimeInputField(
                value = seconds,
                onValueChange = onSecondsChange,
                label = "秒",
                maxValue = 59,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "最长24小时",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 年级范围选择
        Text(
            text = "题目年级范围",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            GradeDropdown(
                selectedGrade = startGrade,
                onGradeSelected = onStartGradeChange,
                label = "起始年级",
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "至",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp)
            )
            GradeDropdown(
                selectedGrade = endGrade,
                onGradeSelected = onEndGradeChange,
                label = "结束年级",
                enabledGrades = startGrade.ordinal..GradeLevel.entries.size,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 题目数量选择
        Text(
            text = "答对题目数：$problemCount 题",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf(3, 5, 10).forEach { count ->
                ProblemCountChip(
                    count = count,
                    isSelected = problemCount == count,
                    onClick = { onProblemCountChange(count) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 开始按钮
        val canStart = (hours * 3600 + minutes * 60 + seconds) > 0
        Button(
            onClick = onStart,
            enabled = canStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Green500,
                contentColor = Color.White,
                disabledContainerColor = GreenDark.copy(alpha = 0.3f),
                disabledContentColor = Color.White.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                text = "开始专注",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun TimeInputField(
    value: Int,
    onValueChange: (Int) -> Unit,
    label: String,
    maxValue: Int,
    modifier: Modifier = Modifier
) {
    var textValue by remember(value) { mutableStateOf(if (value == 0) "" else value.toString()) }

    OutlinedTextField(
        value = textValue,
        onValueChange = { newValue ->
            if (newValue.isEmpty()) {
                textValue = ""
                onValueChange(0)
            } else if (newValue.all { it.isDigit() }) {
                val intValue = newValue.toIntOrNull() ?: 0
                if (intValue <= maxValue) {
                    textValue = newValue
                    onValueChange(intValue)
                } else {
                    textValue = maxValue.toString()
                    onValueChange(maxValue)
                }
            }
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Green500,
            cursorColor = Green500,
            focusedLabelColor = Green500
        )
    )
}

@Composable
private fun GradeDropdown(
    selectedGrade: GradeLevel,
    onGradeSelected: (GradeLevel) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabledGrades: IntRange = 0..GradeLevel.entries.size - 1
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedGrade.displayName,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Amber500,
                cursorColor = Amber500,
                focusedLabelColor = Amber500
            ),
            trailingIcon = {
                Text(
                    text = "▼",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            GradeLevel.entries
                .filter { it.ordinal in enabledGrades }
                .forEach { grade ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = grade.displayName,
                                    fontWeight = if (grade == selectedGrade) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        },
                        onClick = {
                            onGradeSelected(grade)
                            expanded = false
                        }
                    )
                }
        }
    }
}

@Composable
private fun PresetChip(
    minutes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Green500 else Color.Transparent,
        animationSpec = tween(200),
        label = "bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Green500 else MaterialTheme.colorScheme.outline,
        animationSpec = tween(200),
        label = "border"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$minutes",
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "分钟",
                color = if (isSelected) Color.White.copy(alpha = 0.8f)
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun ProblemCountChip(
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Green500 else Color.Transparent,
        animationSpec = tween(200),
        label = "bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Green500 else MaterialTheme.colorScheme.outline,
        animationSpec = tween(200),
        label = "border"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$count 题",
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun RunningContent(
    state: TimerState.Running,
    onCancel: () -> Unit,
    onEarlyUnlock: () -> Unit
) {
    val progress by animateFloatAsState(
        targetValue = state.progress,
        animationSpec = tween(500),
        label = "progress"
    )

    // 大圆形倒计时
    Box(
        modifier = Modifier.size(280.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            color = Green500,
            trackColor = GreenDark.copy(alpha = 0.3f),
            strokeWidth = 12.dp,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = state.displayTime,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Light,
                    fontSize = if (state.displayHours > 0) 48.sp else 64.sp,
                    letterSpacing = 2.sp
                ),
                color = Color.White
            )
            Text(
                text = "专注中",
                style = MaterialTheme.typography.titleMedium,
                color = Green500
            )
        }
    }

    Spacer(modifier = Modifier.height(32.dp))

    // 提前解锁按钮
    Button(
        onClick = onEarlyUnlock,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Amber500,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(26.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "提前解锁",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "答对题目即可结束",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedButton(
        onClick = onCancel,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Red400
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.SolidColor(Red400)
        ),
        shape = RoundedCornerShape(26.dp)
    ) {
        Text(
            text = "取消专注",
            fontSize = 16.sp
        )
    }
}

@Composable
private fun MathChallengeContent(
    state: TimerState.MathChallenge,
    onAnswer: (String) -> Unit,
    onSkip: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "提前解锁挑战",
            style = MaterialTheme.typography.headlineSmall,
            color = Amber500,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "进度：${state.correctCount} / ${state.targetCount}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 题目卡片
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = state.currentProblem.question,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 答案输入
        OutlinedTextField(
            value = state.userAnswer,
            onValueChange = { value ->
                if (value.isEmpty() || value == "-" || value.all { it.isDigit() } || 
                    (value.startsWith("-") && value.drop(1).all { it.isDigit() })) {
                    onAnswer(value)
                }
            },
            label = { Text("输入答案") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Amber500,
                cursorColor = Amber500,
                focusedLabelColor = Amber500
            )
        )

        if (state.showWrongFeedback) {
            Text(
                text = "答案错误，请重试",
                color = Red400,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = onSkip,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(26.dp)
            ) {
                Text("跳过")
            }

            Button(
                onClick = { onAnswer(state.userAnswer) },
                enabled = state.userAnswer.isNotEmpty(),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Amber500,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(26.dp)
            ) {
                Text("提交")
            }
        }
    }
}

@Composable
private fun FinishedContent(
    onReset: () -> Unit
) {
    Text(
        text = "✓",
        fontSize = 72.sp,
        color = Green500,
        modifier = Modifier
            .size(100.dp)
            .background(Green500.copy(alpha = 0.15f), CircleShape)
            .padding(20.dp)
    )

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = "专注结束",
        style = MaterialTheme.typography.headlineMedium,
        color = Green500,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "干得不错！休息一下吧",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(48.dp))

    Button(
        onClick = onReset,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Green500,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(26.dp)
    ) {
        Text(
            text = "再来一轮",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
