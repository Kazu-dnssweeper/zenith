package com.iterio.app.fakes

import com.iterio.app.domain.model.PomodoroSettings
import com.iterio.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * テスト用の SettingsRepository 実装
 */
class FakeSettingsRepository : SettingsRepository {

    private val pomodoroSettings = MutableStateFlow(PomodoroSettings())
    private val settings = MutableStateFlow<Map<String, String>>(emptyMap())
    private val allowedApps = MutableStateFlow<List<String>>(emptyList())
    private val language = MutableStateFlow("ja")
    private val bgmTrackId = MutableStateFlow<String?>(null)
    private val bgmVolume = MutableStateFlow(0.5f)
    private val bgmAutoPlay = MutableStateFlow(true)

    override fun getPomodoroSettingsFlow(): Flow<PomodoroSettings> = pomodoroSettings

    override suspend fun getPomodoroSettings(): PomodoroSettings = pomodoroSettings.value

    override suspend fun updatePomodoroSettings(settings: PomodoroSettings) {
        pomodoroSettings.value = settings
    }

    override suspend fun getSetting(key: String, defaultValue: String): String =
        settings.value[key] ?: defaultValue

    override suspend fun setSetting(key: String, value: String) {
        settings.value = settings.value + (key to value)
    }

    override suspend fun getAllowedApps(): List<String> = allowedApps.value

    override suspend fun setAllowedApps(packages: List<String>) {
        allowedApps.value = packages
    }

    override fun getAllowedAppsFlow(): Flow<List<String>> = allowedApps

    override suspend fun getLanguage(): String = language.value

    override suspend fun setLanguage(languageCode: String) {
        language.value = languageCode
    }

    override fun getLanguageFlow(): Flow<String> = language

    // BGM settings
    override suspend fun getBgmTrackId(): String? = bgmTrackId.value

    override suspend fun setBgmTrackId(trackId: String?) {
        bgmTrackId.value = trackId
    }

    override fun getBgmTrackIdFlow(): Flow<String?> = bgmTrackId

    override suspend fun getBgmVolume(): Float = bgmVolume.value

    override suspend fun setBgmVolume(volume: Float) {
        bgmVolume.value = volume.coerceIn(0f, 1f)
    }

    override fun getBgmVolumeFlow(): Flow<Float> = bgmVolume

    override suspend fun getBgmAutoPlay(): Boolean = bgmAutoPlay.value

    override suspend fun setBgmAutoPlay(enabled: Boolean) {
        bgmAutoPlay.value = enabled
    }

    override fun getBgmAutoPlayFlow(): Flow<Boolean> = bgmAutoPlay

    // Test helpers
    fun clear() {
        pomodoroSettings.value = PomodoroSettings()
        settings.value = emptyMap()
        allowedApps.value = emptyList()
        language.value = "ja"
        bgmTrackId.value = null
        bgmVolume.value = 0.5f
        bgmAutoPlay.value = true
    }
}
