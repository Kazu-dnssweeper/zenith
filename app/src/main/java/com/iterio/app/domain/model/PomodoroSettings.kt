package com.iterio.app.domain.model

import com.iterio.app.config.AppConfig

data class PomodoroSettings(
    val workDurationMinutes: Int = AppConfig.Timer.DEFAULT_WORK_MINUTES,
    val shortBreakMinutes: Int = AppConfig.Timer.DEFAULT_SHORT_BREAK_MINUTES,
    val longBreakMinutes: Int = AppConfig.Timer.DEFAULT_LONG_BREAK_MINUTES,
    val cyclesBeforeLongBreak: Int = AppConfig.Timer.DEFAULT_CYCLES,
    val focusModeEnabled: Boolean = false,
    val focusModeStrict: Boolean = false, // true = complete lock, false = emergency unlock allowed
    val autoLoopEnabled: Boolean = false, // Premium限定: ポモドーロサイクル完了後も自動で次のサイクルを開始
    val reviewEnabled: Boolean = true,
    val reviewIntervals: List<Int> = AppConfig.Premium.PREMIUM_REVIEW_INTERVALS, // days
    val defaultReviewCount: Int = AppConfig.Premium.DEFAULT_REVIEW_COUNT_FREE, // デフォルト復習回数
    val notificationsEnabled: Boolean = true
)
