package com.iterio.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    val key: String,
    val value: String
) {
    companion object {
        // Timer settings
        const val KEY_WORK_DURATION_MINUTES = "work_duration_minutes"
        const val KEY_SHORT_BREAK_MINUTES = "short_break_minutes"
        const val KEY_LONG_BREAK_MINUTES = "long_break_minutes"
        const val KEY_CYCLES_BEFORE_LONG_BREAK = "cycles_before_long_break"

        // Focus mode settings
        const val KEY_FOCUS_MODE_ENABLED = "focus_mode_enabled"
        const val KEY_FOCUS_MODE_STRICT = "focus_mode_strict" // true = complete lock, false = emergency unlock allowed
        const val KEY_AUTO_LOOP_ENABLED = "auto_loop_enabled" // Premium限定: 自動ループ
        const val KEY_ALLOWED_APPS = "allowed_apps" // フォーカスモード中に許可するアプリ一覧

        // Review settings
        const val KEY_REVIEW_ENABLED = "review_enabled"
        const val KEY_REVIEW_INTERVALS = "review_intervals" // JSON array of days: [1, 3, 7, 14, 30, 60]
        const val KEY_DEFAULT_REVIEW_COUNT = "default_review_count" // デフォルト復習回数

        // Notification settings
        const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        const val KEY_REVIEW_REMINDER_TIME = "review_reminder_time" // HH:mm format

        // Language settings
        const val KEY_LANGUAGE = "language" // "ja" or "en"

        // BGM settings
        const val KEY_BGM_TRACK_ID = "bgm_track_id"
        const val KEY_BGM_VOLUME = "bgm_volume"
        const val KEY_BGM_AUTO_PLAY = "bgm_auto_play"

        // Default values
        const val DEFAULT_LANGUAGE = "ja" // Japanese as default
        const val DEFAULT_WORK_DURATION = "25"
        const val DEFAULT_SHORT_BREAK = "5"
        const val DEFAULT_LONG_BREAK = "15"
        const val DEFAULT_CYCLES = "4"
        const val DEFAULT_REVIEW_INTERVALS = "[1, 3, 7, 14, 30, 60]"
        const val DEFAULT_REVIEW_COUNT = "2"
        const val DEFAULT_ALLOWED_APPS = "[]"

        // BGM default values
        const val DEFAULT_BGM_TRACK_ID = ""   // 未選択
        const val DEFAULT_BGM_VOLUME = "0.5"  // 50%
        const val DEFAULT_BGM_AUTO_PLAY = "true"
    }
}
