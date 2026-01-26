package com.iterio.app.data.repository

import app.cash.turbine.test
import com.iterio.app.data.local.IterioDatabase
import com.iterio.app.data.local.dao.SettingsDao
import com.iterio.app.data.local.entity.SettingsEntity
import androidx.room.withTransaction
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * SettingsRepositoryImpl のユニットテスト
 */
class SettingsRepositoryImplTest {

    private lateinit var settingsDao: SettingsDao
    private lateinit var database: IterioDatabase
    private lateinit var repository: SettingsRepositoryImpl

    @Before
    fun setup() {
        settingsDao = mockk()
        database = mockk(relaxed = true)
        repository = SettingsRepositoryImpl(settingsDao, database)
    }

    // ==================== PomodoroSettings Tests ====================

    @Test
    fun `getPomodoroSettings returns default values when no settings exist`() = runTest {
        coEvery { settingsDao.getSettingValue(any()) } returns null
        coEvery { settingsDao.setSetting(any(), any()) } returns Unit

        val settings = repository.getPomodoroSettings()

        assertEquals(25, settings.workDurationMinutes)
        assertEquals(5, settings.shortBreakMinutes)
        assertEquals(15, settings.longBreakMinutes)
        assertEquals(4, settings.cyclesBeforeLongBreak)
        assertTrue(settings.focusModeEnabled)
        assertFalse(settings.focusModeStrict)
        assertFalse(settings.autoLoopEnabled)
        assertTrue(settings.reviewEnabled)
        assertTrue(settings.notificationsEnabled)
    }

    @Test
    fun `getPomodoroSettings returns stored values`() = runTest {
        coEvery { settingsDao.getSettingValue(SettingsEntity.KEY_WORK_DURATION_MINUTES) } returns "30"
        coEvery { settingsDao.getSettingValue(SettingsEntity.KEY_SHORT_BREAK_MINUTES) } returns "10"
        coEvery { settingsDao.getSettingValue(SettingsEntity.KEY_LONG_BREAK_MINUTES) } returns "20"
        coEvery { settingsDao.getSettingValue(SettingsEntity.KEY_CYCLES_BEFORE_LONG_BREAK) } returns "3"
        coEvery { settingsDao.getSettingValue(SettingsEntity.KEY_FOCUS_MODE_ENABLED) } returns "false"
        coEvery { settingsDao.getSettingValue(SettingsEntity.KEY_FOCUS_MODE_STRICT) } returns "true"
        coEvery { settingsDao.getSettingValue(SettingsEntity.KEY_AUTO_LOOP_ENABLED) } returns "true"
        coEvery { settingsDao.getSettingValue(SettingsEntity.KEY_REVIEW_ENABLED) } returns "false"
        coEvery { settingsDao.getSettingValue(SettingsEntity.KEY_REVIEW_INTERVALS) } returns "[1, 3, 7]"
        coEvery { settingsDao.getSettingValue(SettingsEntity.KEY_DEFAULT_REVIEW_COUNT) } returns "3"
        coEvery { settingsDao.getSettingValue(SettingsEntity.KEY_NOTIFICATIONS_ENABLED) } returns "false"

        val settings = repository.getPomodoroSettings()

        assertEquals(30, settings.workDurationMinutes)
        assertEquals(10, settings.shortBreakMinutes)
        assertEquals(20, settings.longBreakMinutes)
        assertEquals(3, settings.cyclesBeforeLongBreak)
        assertFalse(settings.focusModeEnabled)
        assertTrue(settings.focusModeStrict)
        assertTrue(settings.autoLoopEnabled)
        assertFalse(settings.reviewEnabled)
        assertEquals(listOf(1, 3, 7), settings.reviewIntervals)
        assertEquals(3, settings.defaultReviewCount)
        assertFalse(settings.notificationsEnabled)
    }

    @Test
    fun `getPomodoroSettingsFlow emits updates`() = runTest {
        val settingsList = listOf(
            SettingsEntity(SettingsEntity.KEY_WORK_DURATION_MINUTES, "45"),
            SettingsEntity(SettingsEntity.KEY_SHORT_BREAK_MINUTES, "10")
        )
        every { settingsDao.getAllSettings() } returns flowOf(settingsList)

        repository.getPomodoroSettingsFlow().test {
            val settings = awaitItem()
            assertEquals(45, settings.workDurationMinutes)
            assertEquals(10, settings.shortBreakMinutes)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updatePomodoroSettings saves all values`() = runTest {
        coEvery { settingsDao.setSetting(any(), any()) } returns Unit

        // Mock the withTransaction extension function
        mockkStatic("androidx.room.RoomDatabaseKt")
        val lambdaSlot = slot<suspend () -> Unit>()
        coEvery { database.withTransaction(capture(lambdaSlot)) } answers {
            kotlinx.coroutines.runBlocking { lambdaSlot.captured.invoke() }
        }

        val settings = com.iterio.app.domain.model.PomodoroSettings(
            workDurationMinutes = 50,
            shortBreakMinutes = 15,
            longBreakMinutes = 30,
            cyclesBeforeLongBreak = 5,
            focusModeEnabled = false,
            focusModeStrict = true,
            autoLoopEnabled = true,
            reviewEnabled = false,
            reviewIntervals = listOf(1, 2, 3),
            defaultReviewCount = 4,
            notificationsEnabled = false
        )

        repository.updatePomodoroSettings(settings)

        coVerify { settingsDao.setSetting(SettingsEntity.KEY_WORK_DURATION_MINUTES, "50") }
        coVerify { settingsDao.setSetting(SettingsEntity.KEY_SHORT_BREAK_MINUTES, "15") }
        coVerify { settingsDao.setSetting(SettingsEntity.KEY_LONG_BREAK_MINUTES, "30") }
        coVerify { settingsDao.setSetting(SettingsEntity.KEY_CYCLES_BEFORE_LONG_BREAK, "5") }
        coVerify { settingsDao.setSetting(SettingsEntity.KEY_FOCUS_MODE_ENABLED, "false") }
        coVerify { settingsDao.setSetting(SettingsEntity.KEY_FOCUS_MODE_STRICT, "true") }
        coVerify { settingsDao.setSetting(SettingsEntity.KEY_AUTO_LOOP_ENABLED, "true") }
        coVerify { settingsDao.setSetting(SettingsEntity.KEY_REVIEW_ENABLED, "false") }
        coVerify { settingsDao.setSetting(SettingsEntity.KEY_DEFAULT_REVIEW_COUNT, "4") }
        coVerify { settingsDao.setSetting(SettingsEntity.KEY_NOTIFICATIONS_ENABLED, "false") }
    }

    // ==================== Generic Settings Tests ====================

    @Test
    fun `getSetting returns stored value`() = runTest {
        coEvery { settingsDao.getSettingValue("test_key") } returns "test_value"

        val result = repository.getSetting("test_key", "default")

        assertEquals("test_value", result)
    }

    @Test
    fun `getSetting returns default and saves when not exists`() = runTest {
        coEvery { settingsDao.getSettingValue("test_key") } returns null
        coEvery { settingsDao.setSetting("test_key", "default_value") } returns Unit

        val result = repository.getSetting("test_key", "default_value")

        assertEquals("default_value", result)
        coVerify { settingsDao.setSetting("test_key", "default_value") }
    }

    @Test
    fun `setSetting calls dao`() = runTest {
        coEvery { settingsDao.setSetting("key", "value") } returns Unit

        repository.setSetting("key", "value")

        coVerify { settingsDao.setSetting("key", "value") }
    }

    // ==================== Allowed Apps Tests ====================

    @Test
    fun `getAllowedApps returns empty list by default`() = runTest {
        coEvery { settingsDao.getSettingValue(SettingsEntity.KEY_ALLOWED_APPS) } returns null
        coEvery { settingsDao.setSetting(any(), any()) } returns Unit

        val apps = repository.getAllowedApps()

        assertTrue(apps.isEmpty())
    }

    @Test
    fun `getAllowedApps returns stored packages`() = runTest {
        coEvery { settingsDao.getSettingValue(SettingsEntity.KEY_ALLOWED_APPS) } returns """["com.example.app1", "com.example.app2"]"""

        val apps = repository.getAllowedApps()

        assertEquals(2, apps.size)
        assertEquals("com.example.app1", apps[0])
        assertEquals("com.example.app2", apps[1])
    }

    @Test
    fun `setAllowedApps saves packages as JSON`() = runTest {
        coEvery { settingsDao.setSetting(any(), any()) } returns Unit

        repository.setAllowedApps(listOf("com.example.app"))

        coVerify { settingsDao.setSetting(SettingsEntity.KEY_ALLOWED_APPS, match { it.contains("com.example.app") }) }
    }

    @Test
    fun `getAllowedAppsFlow emits updates`() = runTest {
        every { settingsDao.getSettingFlow(SettingsEntity.KEY_ALLOWED_APPS) } returns flowOf("""["com.test"]""")

        repository.getAllowedAppsFlow().test {
            val apps = awaitItem()
            assertEquals(1, apps.size)
            assertEquals("com.test", apps[0])
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== Language Tests ====================

    @Test
    fun `getLanguage returns default ja`() = runTest {
        coEvery { settingsDao.getSettingValue(SettingsEntity.KEY_LANGUAGE) } returns null
        coEvery { settingsDao.setSetting(any(), any()) } returns Unit

        val language = repository.getLanguage()

        assertEquals("ja", language)
    }

    @Test
    fun `getLanguage returns stored value`() = runTest {
        coEvery { settingsDao.getSettingValue(SettingsEntity.KEY_LANGUAGE) } returns "en"

        val language = repository.getLanguage()

        assertEquals("en", language)
    }

    @Test
    fun `setLanguage saves value`() = runTest {
        coEvery { settingsDao.setSetting(any(), any()) } returns Unit

        repository.setLanguage("en")

        coVerify { settingsDao.setSetting(SettingsEntity.KEY_LANGUAGE, "en") }
    }

    @Test
    fun `getLanguageFlow emits updates`() = runTest {
        every { settingsDao.getSettingFlow(SettingsEntity.KEY_LANGUAGE) } returns flowOf("en")

        repository.getLanguageFlow().test {
            val language = awaitItem()
            assertEquals("en", language)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== BGM Tests ====================

    @Test
    fun `getBgmTrackId returns null when empty`() = runTest {
        coEvery { settingsDao.getSettingValue(SettingsEntity.KEY_BGM_TRACK_ID) } returns ""

        val trackId = repository.getBgmTrackId()

        assertNull(trackId)
    }

    @Test
    fun `getBgmTrackId returns stored value`() = runTest {
        coEvery { settingsDao.getSettingValue(SettingsEntity.KEY_BGM_TRACK_ID) } returns "track_1"

        val trackId = repository.getBgmTrackId()

        assertEquals("track_1", trackId)
    }

    @Test
    fun `setBgmTrackId saves value`() = runTest {
        coEvery { settingsDao.setSetting(any(), any()) } returns Unit

        repository.setBgmTrackId("track_2")

        coVerify { settingsDao.setSetting(SettingsEntity.KEY_BGM_TRACK_ID, "track_2") }
    }

    @Test
    fun `setBgmTrackId saves empty when null`() = runTest {
        coEvery { settingsDao.setSetting(any(), any()) } returns Unit

        repository.setBgmTrackId(null)

        coVerify { settingsDao.setSetting(SettingsEntity.KEY_BGM_TRACK_ID, "") }
    }

    @Test
    fun `getBgmVolume returns default half`() = runTest {
        coEvery { settingsDao.getSettingValue(SettingsEntity.KEY_BGM_VOLUME) } returns null
        coEvery { settingsDao.setSetting(any(), any()) } returns Unit

        val volume = repository.getBgmVolume()

        assertEquals(0.5f, volume, 0.001f)
    }

    @Test
    fun `getBgmVolume clamps value to 0-1 range`() = runTest {
        coEvery { settingsDao.getSettingValue(SettingsEntity.KEY_BGM_VOLUME) } returns "1.5"

        val volume = repository.getBgmVolume()

        assertEquals(1f, volume, 0.001f)
    }

    @Test
    fun `setBgmVolume clamps value`() = runTest {
        coEvery { settingsDao.setSetting(any(), any()) } returns Unit

        repository.setBgmVolume(1.5f)

        coVerify { settingsDao.setSetting(SettingsEntity.KEY_BGM_VOLUME, "1.0") }
    }

    @Test
    fun `getBgmAutoPlay returns default true`() = runTest {
        coEvery { settingsDao.getSettingValue(SettingsEntity.KEY_BGM_AUTO_PLAY) } returns null
        coEvery { settingsDao.setSetting(any(), any()) } returns Unit

        val autoPlay = repository.getBgmAutoPlay()

        assertTrue(autoPlay)
    }

    @Test
    fun `setBgmAutoPlay saves value`() = runTest {
        coEvery { settingsDao.setSetting(any(), any()) } returns Unit

        repository.setBgmAutoPlay(false)

        coVerify { settingsDao.setSetting(SettingsEntity.KEY_BGM_AUTO_PLAY, "false") }
    }

    @Test
    fun `getBgmAutoPlayFlow emits updates`() = runTest {
        every { settingsDao.getSettingFlow(SettingsEntity.KEY_BGM_AUTO_PLAY) } returns flowOf("false")

        repository.getBgmAutoPlayFlow().test {
            val autoPlay = awaitItem()
            assertFalse(autoPlay)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getBgmVolumeFlow emits updates`() = runTest {
        every { settingsDao.getSettingFlow(SettingsEntity.KEY_BGM_VOLUME) } returns flowOf("0.75")

        repository.getBgmVolumeFlow().test {
            val volume = awaitItem()
            assertEquals(0.75f, volume, 0.001f)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getBgmTrackIdFlow emits updates`() = runTest {
        every { settingsDao.getSettingFlow(SettingsEntity.KEY_BGM_TRACK_ID) } returns flowOf("track_123")

        repository.getBgmTrackIdFlow().test {
            val trackId = awaitItem()
            assertEquals("track_123", trackId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAllowedApps returns empty list for malformed JSON`() = runTest {
        coEvery { settingsDao.getSettingValue(SettingsEntity.KEY_ALLOWED_APPS) } returns "not valid json"

        val apps = repository.getAllowedApps()

        assertTrue(apps.isEmpty())
    }
}
