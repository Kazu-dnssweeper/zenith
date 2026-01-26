package com.iterio.app.fakes

import com.iterio.app.domain.common.DomainError
import com.iterio.app.domain.common.Result
import com.iterio.app.domain.model.PomodoroSettings
import com.iterio.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

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

    override suspend fun getPomodoroSettings(): Result<PomodoroSettings, DomainError> =
        Result.Success(pomodoroSettings.value)

    override suspend fun updatePomodoroSettings(settings: PomodoroSettings): Result<Unit, DomainError> {
        pomodoroSettings.value = settings
        return Result.Success(Unit)
    }

    override suspend fun getSetting(key: String, defaultValue: String): Result<String, DomainError> =
        Result.Success(settings.value[key] ?: defaultValue)

    override suspend fun setSetting(key: String, value: String): Result<Unit, DomainError> {
        settings.value = settings.value + (key to value)
        return Result.Success(Unit)
    }

    override suspend fun getAllowedApps(): Result<List<String>, DomainError> =
        Result.Success(allowedApps.value)

    override suspend fun setAllowedApps(packages: List<String>): Result<Unit, DomainError> {
        allowedApps.value = packages
        return Result.Success(Unit)
    }

    override fun getAllowedAppsFlow(): Flow<List<String>> = allowedApps

    override suspend fun getLanguage(): Result<String, DomainError> =
        Result.Success(language.value)

    override suspend fun setLanguage(languageCode: String): Result<Unit, DomainError> {
        language.value = languageCode
        return Result.Success(Unit)
    }

    override fun getLanguageFlow(): Flow<String> = language

    // BGM settings
    override suspend fun getBgmTrackId(): Result<String?, DomainError> =
        Result.Success(bgmTrackId.value)

    override suspend fun setBgmTrackId(trackId: String?): Result<Unit, DomainError> {
        bgmTrackId.value = trackId
        return Result.Success(Unit)
    }

    override fun getBgmTrackIdFlow(): Flow<String?> = bgmTrackId

    override suspend fun getBgmVolume(): Result<Float, DomainError> =
        Result.Success(bgmVolume.value)

    override suspend fun setBgmVolume(volume: Float): Result<Unit, DomainError> {
        bgmVolume.value = volume.coerceIn(0f, 1f)
        return Result.Success(Unit)
    }

    override fun getBgmVolumeFlow(): Flow<Float> = bgmVolume

    override suspend fun getBgmAutoPlay(): Result<Boolean, DomainError> =
        Result.Success(bgmAutoPlay.value)

    override suspend fun setBgmAutoPlay(enabled: Boolean): Result<Unit, DomainError> {
        bgmAutoPlay.value = enabled
        return Result.Success(Unit)
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
