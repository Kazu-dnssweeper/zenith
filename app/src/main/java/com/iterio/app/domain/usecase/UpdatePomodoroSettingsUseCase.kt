package com.iterio.app.domain.usecase

import com.iterio.app.domain.common.DomainError
import com.iterio.app.domain.common.Result
import com.iterio.app.domain.model.PomodoroSettings
import com.iterio.app.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * ポモドーロ設定を更新する UseCase
 *
 * 個別のフィールド更新と、設定全体の更新をサポート
 */
class UpdatePomodoroSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    /**
     * 作業時間を更新
     */
    suspend fun updateWorkDuration(minutes: Int): Result<Unit, DomainError> {
        return updateSingleField { it.copy(workDurationMinutes = minutes) }
    }

    /**
     * 短い休憩時間を更新
     */
    suspend fun updateShortBreak(minutes: Int): Result<Unit, DomainError> {
        return updateSingleField { it.copy(shortBreakMinutes = minutes) }
    }

    /**
     * 長い休憩時間を更新
     */
    suspend fun updateLongBreak(minutes: Int): Result<Unit, DomainError> {
        return updateSingleField { it.copy(longBreakMinutes = minutes) }
    }

    /**
     * 長い休憩までのサイクル数を更新
     */
    suspend fun updateCycles(cycles: Int): Result<Unit, DomainError> {
        return updateSingleField { it.copy(cyclesBeforeLongBreak = cycles) }
    }

    /**
     * フォーカスモードのトグル
     */
    suspend fun toggleFocusMode(enabled: Boolean): Result<Unit, DomainError> {
        return updateSingleField { it.copy(focusModeEnabled = enabled) }
    }

    /**
     * オートループのトグル
     */
    suspend fun toggleAutoLoop(enabled: Boolean): Result<Unit, DomainError> {
        return updateSingleField { it.copy(autoLoopEnabled = enabled) }
    }

    /**
     * レビュー機能のトグル
     */
    suspend fun toggleReview(enabled: Boolean): Result<Unit, DomainError> {
        return updateSingleField { it.copy(reviewEnabled = enabled) }
    }

    /**
     * 通知のトグル
     */
    suspend fun toggleNotifications(enabled: Boolean): Result<Unit, DomainError> {
        return updateSingleField { it.copy(notificationsEnabled = enabled) }
    }

    /**
     * 設定全体を更新
     */
    suspend fun updateSettings(settings: PomodoroSettings): Result<Unit, DomainError> {
        return settingsRepository.updatePomodoroSettings(settings)
    }

    /**
     * 単一フィールドを更新するヘルパー
     */
    private suspend fun updateSingleField(
        transform: (PomodoroSettings) -> PomodoroSettings
    ): Result<Unit, DomainError> {
        return settingsRepository.getPomodoroSettings().flatMap { current ->
            val updated = transform(current)
            settingsRepository.updatePomodoroSettings(updated)
        }
    }
}
