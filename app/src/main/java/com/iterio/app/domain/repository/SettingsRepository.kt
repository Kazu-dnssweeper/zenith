package com.iterio.app.domain.repository

import com.iterio.app.domain.model.PomodoroSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getPomodoroSettingsFlow(): Flow<PomodoroSettings>
    suspend fun getPomodoroSettings(): PomodoroSettings
    suspend fun updatePomodoroSettings(settings: PomodoroSettings)
    suspend fun getSetting(key: String, defaultValue: String): String
    suspend fun setSetting(key: String, value: String)

    // 許可アプリ設定
    suspend fun getAllowedApps(): List<String>
    suspend fun setAllowedApps(packages: List<String>)
    fun getAllowedAppsFlow(): Flow<List<String>>

    // 言語設定
    suspend fun getLanguage(): String
    suspend fun setLanguage(languageCode: String)
    fun getLanguageFlow(): Flow<String>

    // BGM設定
    suspend fun getBgmTrackId(): String?
    suspend fun setBgmTrackId(trackId: String?)
    fun getBgmTrackIdFlow(): Flow<String?>

    suspend fun getBgmVolume(): Float
    suspend fun setBgmVolume(volume: Float)
    fun getBgmVolumeFlow(): Flow<Float>

    suspend fun getBgmAutoPlay(): Boolean
    suspend fun setBgmAutoPlay(enabled: Boolean)
    fun getBgmAutoPlayFlow(): Flow<Boolean>
}
