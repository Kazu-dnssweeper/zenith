package com.iterio.app.domain.repository

import com.iterio.app.domain.common.DomainError
import com.iterio.app.domain.common.Result
import com.iterio.app.domain.model.PomodoroSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getPomodoroSettingsFlow(): Flow<PomodoroSettings>
    suspend fun getPomodoroSettings(): Result<PomodoroSettings, DomainError>
    suspend fun updatePomodoroSettings(settings: PomodoroSettings): Result<Unit, DomainError>
    suspend fun getSetting(key: String, defaultValue: String): Result<String, DomainError>
    suspend fun setSetting(key: String, value: String): Result<Unit, DomainError>

    // 許可アプリ設定
    suspend fun getAllowedApps(): Result<List<String>, DomainError>
    suspend fun setAllowedApps(packages: List<String>): Result<Unit, DomainError>
    fun getAllowedAppsFlow(): Flow<List<String>>

    // 言語設定
    suspend fun getLanguage(): Result<String, DomainError>
    suspend fun setLanguage(languageCode: String): Result<Unit, DomainError>
    fun getLanguageFlow(): Flow<String>

    // BGM設定
    suspend fun getBgmTrackId(): Result<String?, DomainError>
    suspend fun setBgmTrackId(trackId: String?): Result<Unit, DomainError>
    fun getBgmTrackIdFlow(): Flow<String?>

    suspend fun getBgmVolume(): Result<Float, DomainError>
    suspend fun setBgmVolume(volume: Float): Result<Unit, DomainError>
    fun getBgmVolumeFlow(): Flow<Float>

    suspend fun getBgmAutoPlay(): Result<Boolean, DomainError>
    suspend fun setBgmAutoPlay(enabled: Boolean): Result<Unit, DomainError>
    fun getBgmAutoPlayFlow(): Flow<Boolean>
}
