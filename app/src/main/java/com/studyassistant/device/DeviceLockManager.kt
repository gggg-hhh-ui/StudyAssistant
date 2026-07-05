package com.studyassistant.device

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioManager
import android.os.Build
import android.os.PowerManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import java.util.concurrent.TimeUnit

/**
 * Manages device locking (keyguard) and audio muting during focus sessions.
 * 
 * 安全保护：
 * 1. 自动超时解锁（默认30分钟后自动解锁）
 * 2. 应用启动时检查异常退出状态并解锁
 * 3. SharedPreferences 持久化锁定状态
 */
class DeviceLockManager(
    private val activity: ComponentActivity
) {
    private val devicePolicyManager: DevicePolicyManager =
        activity.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    private val adminComponent = ComponentName(
        activity,
        StudyAssistantDeviceAdminReceiver::class.java
    )

    private val audioManager: AudioManager =
        activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val prefs: SharedPreferences by lazy {
        activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    companion object {
        private const val PREFS_NAME = "study_assistant_lock"
        private const val KEY_IS_LOCKED = "is_locked"
        private const val KEY_LOCK_TIMESTAMP = "lock_timestamp"
        private const val KEY_TIMEOUT_MINUTES = "timeout_minutes"
        private const val DEFAULT_TIMEOUT_MINUTES = 30 // 默认30分钟后自动解锁
        
        // 紧急解锁：连续点击次数
        private const val EMERGENCY_UNLOCK_CLICKS = 5
        private const val EMERGENCY_UNLOCK_WINDOW_MS = 3000L // 3秒内
    }

    /** Returns true if device admin is currently enabled */
    val isDeviceAdminActive: Boolean
        get() = devicePolicyManager.isAdminActive(adminComponent)

    /** Returns true if device admin provisioning is allowed */
    val isDeviceAdminAvailable: Boolean
        get() = devicePolicyManager.isProvisioningAllowed(
            "android.app.admin.DEVICE_ADMIN_FEATURE_ALL_DEVICES"
        )

    /** 检查是否处于锁定状态 */
    val isLocked: Boolean
        get() = prefs.getBoolean(KEY_IS_LOCKED, false)

    /** 检查是否应该自动解锁（超时或异常） */
    private fun shouldAutoUnlock(): Boolean {
        if (!isLocked) return true
        
        val lockTimestamp = prefs.getLong(KEY_LOCK_TIMESTAMP, 0)
        val timeoutMinutes = prefs.getInt(KEY_TIMEOUT_MINUTES, DEFAULT_TIMEOUT_MINUTES)
        
        if (lockTimestamp == 0L) return true
        
        val elapsedMs = System.currentTimeMillis() - lockTimestamp
        val timeoutMs = TimeUnit.MINUTES.toMillis(timeoutMinutes.toLong())
        
        // 超时自动解锁
        if (elapsedMs > timeoutMs) {
            return true
        }
        
        // 如果应用被杀死又重启，解锁（异常保护）
        return false
    }

    /**
     * 应用启动时调用，检查并处理异常锁定
     */
    fun checkAndRecoverOnStartup() {
        if (shouldAutoUnlock()) {
            // 异常退出后的恢复：自动解锁
            if (isDeviceAdminActive) {
                try {
                    devicePolicyManager.setKeyguardDisabled(adminComponent, false)
                    prefs.edit().putBoolean(KEY_IS_LOCKED, false).apply()
                    Toast.makeText(
                        activity,
                        "已恢复锁定状态并解锁",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * Creates an intent to request device admin permission.
     * Launch this via ActivityResultLauncher.
     */
    fun requestDeviceAdmin(): Intent {
        return Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "需要设备管理员权限来锁定屏幕，确保专注期间无法解锁设备。"
            )
        }
    }

    /**
     * Locks the device (disables keyguard) so user cannot unlock during focus.
     * Requires device admin to be active.
     * @param timeoutMinutes 锁定超时时间（分钟），超时后自动解锁
     * @return true if lock was applied successfully
     */
    fun lockDevice(timeoutMinutes: Int = DEFAULT_TIMEOUT_MINUTES): Boolean {
        return try {
            if (isDeviceAdminActive) {
                // 保存锁定状态到 SharedPreferences
                prefs.edit()
                    .putBoolean(KEY_IS_LOCKED, true)
                    .putLong(KEY_LOCK_TIMESTAMP, System.currentTimeMillis())
                    .putInt(KEY_TIMEOUT_MINUTES, timeoutMinutes)
                    .apply()

                // Disable keyguard - user cannot unlock without going through admin
                devicePolicyManager.setKeyguardDisabled(adminComponent, true)

                // Dismiss the keyguard if currently showing
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    activity.setShowWhenLocked(true)
                    activity.setTurnScreenOn(true)
                }

                // Wake up the device
                val powerManager = activity.getSystemService(Context.POWER_SERVICE) as PowerManager
                @Suppress("WakelockTimeout")
                val wakeLock = powerManager.newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
                            PowerManager.ACQUIRE_CAUSES_WAKEUP or
                            PowerManager.ON_AFTER_RELEASE,
                    "StudyAssistant::FocusWakeLock"
                )
                wakeLock.acquire((timeoutMinutes * 60 * 1000L).coerceAtMost(60 * 60 * 1000L))

                // Mute audio
                muteAudio()

                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // 失败时清除锁定状态
            prefs.edit().putBoolean(KEY_IS_LOCKED, false).apply()
            false
        }
    }

    /**
     * Unlocks the device (re-enables keyguard) when focus session ends.
     * @return true if unlock was applied successfully
     */
    fun unlockDevice(): Boolean {
        return try {
            // 清除锁定状态
            prefs.edit().putBoolean(KEY_IS_LOCKED, false).apply()

            if (isDeviceAdminActive) {
                // Re-enable keyguard
                devicePolicyManager.setKeyguardDisabled(adminComponent, false)
            }

            // Restore audio
            unmuteAudio()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            // 即使异常也要清除状态
            prefs.edit().putBoolean(KEY_IS_LOCKED, false).apply()
            false
        }
    }

    /**
     * 紧急解锁（用于防锁机）
     * 通过连续点击触发
     */
    private var lastClickTimestamps = mutableListOf<Long>()

    fun handleEmergencyUnlockAttempt(): Boolean {
        val now = System.currentTimeMillis()
        
        // 清理超过窗口期的点击记录
        lastClickTimestamps.removeAll { now - it > EMERGENCY_UNLOCK_WINDOW_MS }
        
        // 添加当前点击
        lastClickTimestamps.add(now)
        
        // 检查是否达到连续点击次数
        if (lastClickTimestamps.size >= EMERGENCY_UNLOCK_CLICKS) {
            lastClickTimestamps.clear()
            return unlockDevice()
        }
        
        return false
    }

    /**
     * 获取紧急解锁进度（用于UI显示）
     */
    fun getEmergencyUnlockProgress(): Int {
        val now = System.currentTimeMillis()
        lastClickTimestamps.removeAll { now - it > EMERGENCY_UNLOCK_WINDOW_MS }
        return lastClickTimestamps.size
    }

    private var savedRingerMode: Int = AudioManager.RINGER_MODE_NORMAL

    private fun muteAudio() {
        savedRingerMode = audioManager.ringerMode
        try {
            @Suppress("MuteRingerMode")
            audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun unmuteAudio() {
        try {
            audioManager.ringerMode = savedRingerMode
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
