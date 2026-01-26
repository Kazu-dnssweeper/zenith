package com.iterio.app.data.repository

import androidx.room.withTransaction
import com.iterio.app.config.AppConfig
import com.iterio.app.data.local.IterioDatabase
import com.iterio.app.data.local.dao.SettingsDao
import com.iterio.app.data.local.entity.SettingsEntity
import com.iterio.app.domain.model.PomodoroSettings
import com.iterio.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDao: SettingsDao,
    private val database: IterioDatabase
) : SettingsRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun getPomodoroSettingsFlow(): Flow<PomodoroSettings> {
        return settingsDao.getAllSettings().map { settings ->
            buildPomodoroSettings(settings)
        }
    }

    override suspend fun getPomodoroSettings(): PomodoroSettings {
        return PomodoroSettings(
            workDurationMinutes = getSetting(
                SettingsEntity.KEY_WORK_DURATION_MINUTES,
                SettingsEntity.DEFAULT_WORK_DURATION
            ).toIntOrNull() ?: 25,
            shortBreakMinutes = getSetting(
                SettingsEntity.KEY_SHORT_BREAK_MINUTES,
                SettingsEntity.DEFAULT_SHORT_BREAK
            ).toIntOrNull() ?: 5,
            longBreakMinutes = getSetting(
                SettingsEntity.KEY_LONG_BREAK_MINUTES,
                SettingsEntity.DEFAULT_LONG_BREAK
            ).toIntOrNull() ?: 15,
            cyclesBeforeLongBreak = getSetting(
                SettingsEntity.KEY_CYCLES_BEFORE_LONG_BREAK,
                SettingsEntity.DEFAULT_CYCLES
            ).toIntOrNull() ?: 4,
            focusModeEnabled = getSetting(
                SettingsEntity.KEY_FOCUS_MODE_ENABLED,
                "true"
            ).toBoolean(),
            focusModeStrict = getSetting(
                SettingsEntity.KEY_FOCUS_MODE_STRICT,
                "false"
            ).toBoolean(),
            autoLoopEnabled = getSetting(
                SettingsEntity.KEY_AUTO_LOOP_ENABLED,
                "false"
            ).toBoolean(),
            reviewEnabled = getSetting(
                SettingsEntity.KEY_REVIEW_ENABLED,
                "true"
            ).toBoolean(),
            reviewIntervals = parseReviewIntervals(
                getSetting(
                    SettingsEntity.KEY_REVIEW_INTERVALS,
                    SettingsEntity.DEFAULT_REVIEW_INTERVALS
                )
            ),
            defaultReviewCount = getSetting(
                SettingsEntity.KEY_DEFAULT_REVIEW_COUNT,
                SettingsEntity.DEFAULT_REVIEW_COUNT
            ).toIntOrNull() ?: 2,
            notificationsEnabled = getSetting(
                SettingsEntity.KEY_NOTIFICATIONS_ENABLED,
                "true"
            ).toBoolean()
        )
    }

    override suspend fun updatePomodoroSettings(settings: PomodoroSettings) {
        database.withTransaction {
            setSetting(SettingsEntity.KEY_WORK_DURATION_MINUTES, settings.workDurationMinutes.toString())
            setSetting(SettingsEntity.KEY_SHORT_BREAK_MINUTES, settings.shortBreakMinutes.toString())
            setSetting(SettingsEntity.KEY_LONG_BREAK_MINUTES, settings.longBreakMinutes.toString())
            setSetting(SettingsEntity.KEY_CYCLES_BEFORE_LONG_BREAK, settings.cyclesBeforeLongBreak.toString())
            setSetting(SettingsEntity.KEY_FOCUS_MODE_ENABLED, settings.focusModeEnabled.toString())
            setSetting(SettingsEntity.KEY_FOCUS_MODE_STRICT, settings.focusModeStrict.toString())
            setSetting(SettingsEntity.KEY_AUTO_LOOP_ENABLED, settings.autoLoopEnabled.toString())
            setSetting(SettingsEntity.KEY_REVIEW_ENABLED, settings.reviewEnabled.toString())
            setSetting(SettingsEntity.KEY_REVIEW_INTERVALS, json.encodeToString(settings.reviewIntervals))
            setSetting(SettingsEntity.KEY_DEFAULT_REVIEW_COUNT, settings.defaultReviewCount.toString())
            setSetting(SettingsEntity.KEY_NOTIFICATIONS_ENABLED, settings.notificationsEnabled.toString())
        }
    }

    override suspend fun getSetting(key: String, defaultValue: String): String {
        return settingsDao.getSettingValue(key) ?: defaultValue.also {
            settingsDao.setSetting(key, it)
        }
    }

    override suspend fun setSetting(key: String, value: String) {
        settingsDao.setSetting(key, value)
    }

    private fun buildPomodoroSettings(settings: List<SettingsEntity>): PomodoroSettings {
        val settingsMap = settings.associateBy { it.key }
        return PomodoroSettings(
            workDurationMinutes = settingsMap[SettingsEntity.KEY_WORK_DURATION_MINUTES]?.value?.toIntOrNull() ?: 25,
            shortBreakMinutes = settingsMap[SettingsEntity.KEY_SHORT_BREAK_MINUTES]?.value?.toIntOrNull() ?: 5,
            longBreakMinutes = settingsMap[SettingsEntity.KEY_LONG_BREAK_MINUTES]?.value?.toIntOrNull() ?: 15,
            cyclesBeforeLongBreak = settingsMap[SettingsEntity.KEY_CYCLES_BEFORE_LONG_BREAK]?.value?.toIntOrNull() ?: 4,
            focusModeEnabled = settingsMap[SettingsEntity.KEY_FOCUS_MODE_ENABLED]?.value?.toBoolean() ?: true,
            focusModeStrict = settingsMap[SettingsEntity.KEY_FOCUS_MODE_STRICT]?.value?.toBoolean() ?: false,
            autoLoopEnabled = settingsMap[SettingsEntity.KEY_AUTO_LOOP_ENABLED]?.value?.toBoolean() ?: false,
            reviewEnabled = settingsMap[SettingsEntity.KEY_REVIEW_ENABLED]?.value?.toBoolean() ?: true,
            reviewIntervals = parseReviewIntervals(
                settingsMap[SettingsEntity.KEY_REVIEW_INTERVALS]?.value ?: SettingsEntity.DEFAULT_REVIEW_INTERVALS
            ),
            defaultReviewCount = settingsMap[SettingsEntity.KEY_DEFAULT_REVIEW_COUNT]?.value?.toIntOrNull() ?: 2,
            notificationsEnabled = settingsMap[SettingsEntity.KEY_NOTIFICATIONS_ENABLED]?.value?.toBoolean() ?: true
        )
    }

    private fun parseReviewIntervals(jsonString: String): List<Int> {
        return try {
            json.decodeFromString<List<Int>>(jsonString)
        } catch (e: SerializationException) {
            Timber.w(e, "Failed to parse review intervals JSON: %s", jsonString)
            AppConfig.Premium.PREMIUM_REVIEW_INTERVALS
        } catch (e: IllegalArgumentException) {
            Timber.w(e, "Invalid review intervals format: %s", jsonString)
            AppConfig.Premium.PREMIUM_REVIEW_INTERVALS
        }
    }

    // 許可アプリ設定
    override suspend fun getAllowedApps(): List<String> {
        val jsonString = getSetting(
            SettingsEntity.KEY_ALLOWED_APPS,
            SettingsEntity.DEFAULT_ALLOWED_APPS
        )
        return parseAllowedApps(jsonString)
    }

    override suspend fun setAllowedApps(packages: List<String>) {
        val jsonString = json.encodeToString(packages)
        setSetting(SettingsEntity.KEY_ALLOWED_APPS, jsonString)
    }

    override fun getAllowedAppsFlow(): Flow<List<String>> {
        return settingsDao.getSettingFlow(SettingsEntity.KEY_ALLOWED_APPS).map { value ->
            parseAllowedApps(value ?: SettingsEntity.DEFAULT_ALLOWED_APPS)
        }
    }

    private fun parseAllowedApps(jsonString: String): List<String> {
        return try {
            json.decodeFromString<List<String>>(jsonString)
        } catch (e: SerializationException) {
            Timber.w(e, "Failed to parse allowed apps JSON: %s", jsonString)
            emptyList()
        } catch (e: IllegalArgumentException) {
            Timber.w(e, "Invalid allowed apps format: %s", jsonString)
            emptyList()
        }
    }

    // 言語設定
    override suspend fun getLanguage(): String {
        return getSetting(SettingsEntity.KEY_LANGUAGE, SettingsEntity.DEFAULT_LANGUAGE)
    }

    override suspend fun setLanguage(languageCode: String) {
        setSetting(SettingsEntity.KEY_LANGUAGE, languageCode)
    }

    override fun getLanguageFlow(): Flow<String> {
        return settingsDao.getSettingFlow(SettingsEntity.KEY_LANGUAGE).map { value ->
            value ?: SettingsEntity.DEFAULT_LANGUAGE
        }
    }

    // BGM設定
    override suspend fun getBgmTrackId(): String? {
        val value = getSetting(SettingsEntity.KEY_BGM_TRACK_ID, SettingsEntity.DEFAULT_BGM_TRACK_ID)
        return value.ifEmpty { null }
    }

    override suspend fun setBgmTrackId(trackId: String?) {
        setSetting(SettingsEntity.KEY_BGM_TRACK_ID, trackId ?: "")
    }

    override fun getBgmTrackIdFlow(): Flow<String?> {
        return settingsDao.getSettingFlow(SettingsEntity.KEY_BGM_TRACK_ID).map { value ->
            val trackId = value ?: SettingsEntity.DEFAULT_BGM_TRACK_ID
            trackId.ifEmpty { null }
        }
    }

    override suspend fun getBgmVolume(): Float {
        val value = getSetting(SettingsEntity.KEY_BGM_VOLUME, SettingsEntity.DEFAULT_BGM_VOLUME)
        return value.toFloatOrNull()?.coerceIn(0f, 1f) ?: 0.5f
    }

    override suspend fun setBgmVolume(volume: Float) {
        val clampedVolume = volume.coerceIn(0f, 1f)
        setSetting(SettingsEntity.KEY_BGM_VOLUME, clampedVolume.toString())
    }

    override fun getBgmVolumeFlow(): Flow<Float> {
        return settingsDao.getSettingFlow(SettingsEntity.KEY_BGM_VOLUME).map { value ->
            (value ?: SettingsEntity.DEFAULT_BGM_VOLUME).toFloatOrNull()?.coerceIn(0f, 1f) ?: 0.5f
        }
    }

    override suspend fun getBgmAutoPlay(): Boolean {
        return getSetting(SettingsEntity.KEY_BGM_AUTO_PLAY, SettingsEntity.DEFAULT_BGM_AUTO_PLAY).toBoolean()
    }

    override suspend fun setBgmAutoPlay(enabled: Boolean) {
        setSetting(SettingsEntity.KEY_BGM_AUTO_PLAY, enabled.toString())
    }

    override fun getBgmAutoPlayFlow(): Flow<Boolean> {
        return settingsDao.getSettingFlow(SettingsEntity.KEY_BGM_AUTO_PLAY).map { value ->
            (value ?: SettingsEntity.DEFAULT_BGM_AUTO_PLAY).toBoolean()
        }
    }
}
