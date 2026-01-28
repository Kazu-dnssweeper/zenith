package com.iterio.app.widget

import com.iterio.app.service.TimerPhase

data class WidgetState(
    val todayStudyMinutes: Int = 0,
    val currentStreak: Int = 0,
    val timerPhase: TimerPhase = TimerPhase.IDLE,
    val timeRemainingSeconds: Int = 0,
    val isTimerRunning: Boolean = false,
    val isPremium: Boolean = false,
    val pendingReviewCount: Int = 0,
    val todayTasks: List<WidgetTaskItem> = emptyList()
)
