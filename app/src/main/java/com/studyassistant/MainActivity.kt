package com.studyassistant

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.studyassistant.device.DeviceLockManager
import com.studyassistant.ui.screens.TimerScreen
import com.studyassistant.ui.screens.TimerViewModel
import com.studyassistant.ui.theme.StudyAssistantTheme
import com.studyassistant.domain.model.TimerState
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private lateinit var deviceLockManager: DeviceLockManager

    private val adminEnableLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (deviceLockManager.isDeviceAdminActive) {
            Toast.makeText(this, "设备管理员已授权", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "需要设备管理员权限才能使用锁定功能", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        deviceLockManager = DeviceLockManager(this)

        // 检查并恢复异常锁定状态
        deviceLockManager.checkAndRecoverOnStartup()

        setContent {
            StudyAssistantTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    MainContent(
                        deviceLockManager = deviceLockManager,
                        onRequestAdmin = { requestDeviceAdmin() }
                    )
                }
            }
        }
    }

    private fun requestDeviceAdmin() {
        val intent = deviceLockManager.requestDeviceAdmin()
        adminEnableLauncher.launch(intent)
    }

    override fun onPause() {
        super.onPause()
        // Prevent leaving the app during focus
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Swallow back press - user cannot leave during focus
        // This is intentionally empty to prevent accidental exit
    }

    /**
     * 应用被系统杀死时，确保设备解锁
     */
    override fun onDestroy() {
        super.onDestroy()
        // 解锁设备，防止锁机
        if (::deviceLockManager.isInitialized) {
            deviceLockManager.unlockDevice()
        }
    }
}

@Composable
private fun MainContent(
    deviceLockManager: DeviceLockManager,
    onRequestAdmin: () -> Unit
) {
    val viewModel: TimerViewModel = viewModel()
    var isFocusActive by remember { mutableStateOf(false) }
    
    // 紧急解锁进度
    var emergencyProgress by remember { mutableIntStateOf(0) }

    // Handle timer state changes
    LaunchedEffect(Unit) {
        viewModel.timerState.collectLatest { state ->
            when (state) {
                is TimerState.Running -> {
                    if (!isFocusActive) {
                        isFocusActive = true
                        // Lock device and mute audio when timer starts
                        if (!deviceLockManager.isDeviceAdminActive) {
                            // Request admin if not already granted
                            onRequestAdmin()
                        }
                        val locked = deviceLockManager.lockDevice(
                            timeoutMinutes = (state.totalSeconds / 60).coerceAtLeast(30)
                        )
                        if (!locked && deviceLockManager.isDeviceAdminAvailable) {
                            onRequestAdmin()
                        }
                    }
                }

                is TimerState.MathChallenge -> {
                    // 数学挑战期间保持锁定状态
                    // 不改变 isFocusActive
                }

                is TimerState.Idle, is TimerState.Finished -> {
                    if (isFocusActive) {
                        isFocusActive = false
                        deviceLockManager.unlockDevice()
                    }
                }
            }
        }
    }

    // 解锁进度监听
    LaunchedEffect(Unit) {
        viewModel.timerState.collectLatest { _ ->
            emergencyProgress = deviceLockManager.getEmergencyUnlockProgress()
        }
    }

    // Unlock on dispose (app backgrounded/killed)
    DisposableEffect(Unit) {
        onDispose {
            if (isFocusActive) {
                // 确保退出时解锁
                deviceLockManager.unlockDevice()
            }
        }
    }

    // 紧急解锁处理（连续点击5次）
    val context = androidx.compose.ui.platform.LocalContext.current
    val onEmergencyUnlock: () -> Unit = {
        if (deviceLockManager.handleEmergencyUnlockAttempt()) {
            viewModel.cancelTimer()
            viewModel.resetAfterFinished()
            Toast.makeText(
                context,
                "紧急解锁成功",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            val remaining = 5 - deviceLockManager.getEmergencyUnlockProgress()
            if (remaining > 0) {
                Toast.makeText(
                    context,
                    "再点击 $remaining 次紧急解锁",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        TimerScreen(
            viewModel = viewModel,
            onStartTimer = {
                // Timer start handled by LaunchedEffect above
            },
            onCancelTimer = {
                // Cancellation handled by LaunchedEffect above
            },
            onTimerFinished = {
                // Handled by LaunchedEffect above
            },
            modifier = Modifier
                .fillMaxSize()
                // 添加紧急解锁点击区域（右下角小区域）
                .clickable(
                    onClick = {
                        if (viewModel.timerState.value is TimerState.Running ||
                            viewModel.timerState.value is TimerState.MathChallenge) {
                            onEmergencyUnlock()
                        }
                    }
                )
        )
    }
}
