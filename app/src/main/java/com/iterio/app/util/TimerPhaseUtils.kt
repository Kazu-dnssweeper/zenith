package com.iterio.app.util

import android.content.Context
import com.iterio.app.R
import com.iterio.app.service.TimerPhase

/**
 * Utility functions for timer phase display text.
 */
object TimerPhaseUtils {

    /**
     * Get display text for a timer phase using string resources.
     */
    fun getPhaseDisplayText(context: Context, phase: TimerPhase, isCompleted: Boolean = false): String {
        return when (phase) {
            TimerPhase.WORK -> context.getString(R.string.notification_phase_work)
            TimerPhase.SHORT_BREAK -> context.getString(R.string.notification_phase_short_break)
            TimerPhase.LONG_BREAK -> context.getString(R.string.notification_phase_long_break)
            TimerPhase.IDLE -> if (isCompleted) {
                context.getString(R.string.notification_phase_completed)
            } else {
                context.getString(R.string.notification_phase_ready)
            }
        }
    }
}
