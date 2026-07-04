package com.studyassistant

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
}

@Composable
private fun MainContent(
    deviceLockManager: DeviceLockManager,
    onRequestAdmin: () -> Unit
) {
    val viewModel: TimerViewModel = viewModel()
    var isFocusActive by remember { mutableStateOf(false) }

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
                        val locked = deviceLockManager.lockDevice()
                        if (!locked && deviceLockManager.isDeviceAdminAvailable) {
                            onRequestAdmin()
                        }
                    }
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

    // Unlock on dispose (app backgrounded/killed)
    DisposableEffect(Unit) {
        onDispose {
            if (isFocusActive) {
                deviceLockManager.unlockDevice()
            }
        }
    }

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
        }
    )
}
