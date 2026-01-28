package com.iterio.app.widget.actions

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.iterio.app.service.TimerPhase
import com.iterio.app.service.TimerService
import com.iterio.app.widget.IterioWidgetStateHelper

class ToggleTimerActionCallback : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val prefs = context.getSharedPreferences(TIMER_PREFS_NAME, Context.MODE_PRIVATE)
        val isRunning = prefs.getBoolean(KEY_IS_RUNNING, false)
        val phaseOrdinal = prefs.getInt(KEY_TIMER_PHASE, TimerPhase.IDLE.ordinal)
        val phase = TimerPhase.entries.getOrElse(phaseOrdinal) { TimerPhase.IDLE }

        if (phase == TimerPhase.IDLE) return

        if (isRunning) {
            TimerService.pauseTimer(context)
        } else {
            TimerService.resumeTimer(context)
        }

        IterioWidgetStateHelper.updateWidget(context)
    }

    companion object {
        private const val TIMER_PREFS_NAME = "iterio_widget_timer_prefs"
        private const val KEY_IS_RUNNING = "is_running"
        private const val KEY_TIMER_PHASE = "timer_phase"
    }
}
