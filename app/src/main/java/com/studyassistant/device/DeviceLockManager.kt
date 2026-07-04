package com.studyassistant.device

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.PowerManager
import android.view.WindowManager
import androidx.activity.ComponentActivity

/**
 * Manages device locking (keyguard) and audio muting during focus sessions.
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

    /** Returns true if device admin is currently enabled */
    val isDeviceAdminActive: Boolean
        get() = devicePolicyManager.isAdminActive(adminComponent)

    /** Returns true if device admin permission is available */
    val isDeviceAdminAvailable: Boolean
        get() = devicePolicyManager.isProvisioningAllowed(
            DevicePolicyManager.DEVICE_ADMIN_FEATURE_ALL_DEVICES
        )

    /**
     * Enables device admin. Should be called before lockDevice().
     * This launches the admin enable intent.
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
     * @return true if lock was applied successfully
     */
    fun lockDevice(): Boolean {
        return try {
            if (isDeviceAdminActive) {
                // Disable keyguard - user cannot unlock without going through admin
                devicePolicyManager.setKeyguardDisabled(adminComponent, true)

                // Dismiss the keyguard if currently showing
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    activity.setShowWhenLocked(true)
                    activity.setTurnScreenOn(true)
                }

                // Wake up the device
                val powerManager = activity.getSystemService(Context.POWER_SERVICE) as PowerManager
                val wakeLock = powerManager.newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
                            PowerManager.ACQUIRE_CAUSES_WAKEUP or
                            PowerManager.ON_AFTER_RELEASE,
                    "StudyAssistant::FocusWakeLock"
                )
                wakeLock.acquire(10 * 60 * 1000L) // 10 min max

                // Mute audio
                muteAudio()

                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Unlocks the device (re-enables keyguard) when focus session ends.
     * @return true if unlock was applied successfully
     */
    fun unlockDevice(): Boolean {
        return try {
            if (isDeviceAdminActive) {
                // Re-enable keyguard
                devicePolicyManager.setKeyguardDisabled(adminComponent, false)

                // Restore audio
                unmuteAudio()

                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private var savedRingerMode: Int = AudioManager.RINGER_MODE_NORMAL

    private fun muteAudio() {
        savedRingerMode = audioManager.ringerMode
        try {
            audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
        } catch (e: Exception) {
            // Some devices may not allow this without special permissions
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
