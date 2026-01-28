package com.iterio.app.widget.actions

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.iterio.app.service.TimerPhase
import com.iterio.app.service.TimerService
import com.iterio.app.widget.IterioWidgetStateHelper

class StopTimerActionCallback : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        TimerService.stopTimer(context)

        // Clear timer prefs
        val prefs = context.getSharedPreferences(TIMER_PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putInt(KEY_TIMER_PHASE, TimerPhase.IDLE.ordinal)
            .putInt(KEY_TIME_REMAINING, 0)
            .putBoolean(KEY_IS_RUNNING, false)
            .apply()

        IterioWidgetStateHelper.updateWidget(context)
    }

    companion object {
        private const val TIMER_PREFS_NAME = "iterio_widget_timer_prefs"
        private const val KEY_TIMER_PHASE = "timer_phase"
        private const val KEY_TIME_REMAINING = "time_remaining"
        private const val KEY_IS_RUNNING = "is_running"
    }
}
