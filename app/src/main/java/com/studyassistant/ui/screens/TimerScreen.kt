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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val selectedMinutes by viewModel.selectedMinutes.collectAsState()
    val customMinutes by viewModel.customMinutes.collectAsState()

    // Handle back press during focus
    BackHandler(enabled = timerState is TimerState.Running) {
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
                        selectedMinutes = selectedMinutes,
                        customMinutes = customMinutes,
                        onSelectPreset = viewModel::selectPreset,
                        onCustomChange = viewModel::updateCustomMinutes,
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
                        }
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
    selectedMinutes: Int,
    customMinutes: String,
    onSelectPreset: (Int) -> Unit,
    onCustomChange: (String) -> Unit,
    onStart: () -> Unit
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

    Spacer(modifier = Modifier.height(48.dp))

    // Preset buttons
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PRESET_DURATIONS.forEach { minutes ->
            PresetChip(
                minutes = minutes,
                isSelected = selectedMinutes == minutes && customMinutes.isEmpty(),
                onClick = { onSelectPreset(minutes) },
                modifier = Modifier.weight(1f)
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Custom input
    OutlinedTextField(
        value = customMinutes,
        onValueChange = { value ->
            if (value.isEmpty() || value.all { it.isDigit() }) {
                onCustomChange(value)
            }
        },
        label = { Text("自定义分钟") },
        placeholder = { Text("1~180") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Green500,
            cursorColor = Green500,
            focusedLabelColor = Green500
        ),
        enabled = true
    )

    if (customMinutes.isNotEmpty()) {
        val parsed = customMinutes.toIntOrNull() ?: 0
        if (parsed !in 1..180) {
            Text(
                text = "请输入 1~180 之间的数字",
                color = Red400,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(48.dp))

    // Start button
    val canStart = selectedMinutes > 0 && selectedMinutes <= 180
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
private fun RunningContent(
    state: TimerState.Running,
    onCancel: () -> Unit
) {
    val progress by animateFloatAsState(
        targetValue = state.progress,
        animationSpec = tween(500),
        label = "progress"
    )

    // Large circular timer
    Box(
        modifier = Modifier.size(260.dp),
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
                text = String.format("%02d:%02d", state.displayMinutes, state.displaySeconds),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Light,
                    fontSize = 64.sp,
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

    Spacer(modifier = Modifier.height(64.dp))

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
