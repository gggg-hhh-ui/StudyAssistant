package com.studyassistant.domain.model

/**
 * Represents the current state of the focus timer.
 */
sealed class TimerState {
    /** Idle: user is selecting a duration */
    data object Idle : TimerState()

    /** Running: timer is counting down */
    data class Running(
        val totalSeconds: Int,
        val remainingSeconds: Int
    ) : TimerState() {
        val progress: Float
            get() = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds else 0f

        val displayMinutes: Int get() = remainingSeconds / 60
        val displaySeconds: Int get() = remainingSeconds % 60
    }

    /** Finished: countdown reached zero */
    data object Finished : TimerState()
}

/** Preset durations in minutes */
val PRESET_DURATIONS = listOf(25, 45, 60, 90)
