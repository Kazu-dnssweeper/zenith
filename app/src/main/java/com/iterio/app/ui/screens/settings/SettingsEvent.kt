package com.iterio.app.ui.screens.settings

/**
 * 設定画面のイベント
 *
 * ViewModel への全ての UI イベントを統一的に扱う sealed class
 */
sealed class SettingsEvent {
    /**
     * 通知のオン/オフ切り替え
     */
    data class ToggleNotifications(val enabled: Boolean) : SettingsEvent()

    /**
     * 復習間隔機能のオン/オフ切り替え
     */
    data class ToggleReviewIntervals(val enabled: Boolean) : SettingsEvent()

    /**
     * 作業時間を更新
     */
    data class UpdateWorkDuration(val minutes: Int) : SettingsEvent()

    /**
     * 短い休憩時間を更新
     */
    data class UpdateShortBreak(val minutes: Int) : SettingsEvent()

    /**
     * 長い休憩時間を更新
     */
    data class UpdateLongBreak(val minutes: Int) : SettingsEvent()

    /**
     * サイクル数を更新
     */
    data class UpdateCycles(val cycles: Int) : SettingsEvent()

    /**
     * フォーカスモードのオン/オフ切り替え
     */
    data class ToggleFocusMode(val enabled: Boolean) : SettingsEvent()

    /**
     * 厳格フォーカスモードのオン/オフ切り替え (Premium)
     */
    data class ToggleFocusModeStrict(val strict: Boolean) : SettingsEvent()

    /**
     * 自動ループのオン/オフ切り替え (Premium)
     */
    data class ToggleAutoLoop(val enabled: Boolean) : SettingsEvent()

    /**
     * トライアルを開始
     */
    data object StartTrial : SettingsEvent()

    /**
     * 言語を変更
     */
    data class UpdateLanguage(val languageCode: String) : SettingsEvent()
}
