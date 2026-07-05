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

        val displayHours: Int get() = remainingSeconds / 3600
        val displayMinutes: Int get() = (remainingSeconds % 3600) / 60
        val displaySeconds: Int get() = remainingSeconds % 60

        val displayTime: String
            get() = if (displayHours > 0) {
                String.format("%02d:%02d:%02d", displayHours, displayMinutes, displaySeconds)
            } else {
                String.format("%02d:%02d", displayMinutes, displaySeconds)
            }
    }

    /** MathChallenge: user is solving math problems to unlock early */
    data class MathChallenge(
        val currentProblem: MathProblem,
        val correctCount: Int,
        val targetCount: Int,
        val userAnswer: String = "",
        val showWrongFeedback: Boolean = false
    ) : TimerState()

    /** Finished: countdown reached zero */
    data object Finished : TimerState()
}

/** Preset durations in minutes */
val PRESET_DURATIONS = listOf(25, 45, 60, 90)
