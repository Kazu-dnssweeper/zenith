package com.iterio.app.fakes

import com.iterio.app.config.AppConfig
import com.iterio.app.domain.common.Result
import com.iterio.app.domain.model.PomodoroSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * FakeSettingsRepository のテスト
 */
class FakeSettingsRepositoryTest {

    private lateinit var repository: FakeSettingsRepository

    @Before
    fun setup() {
        repository = FakeSettingsRepository()
    }

    // PomodoroSettings Tests

    @Test
    fun `getPomodoroSettings returns default settings initially`() = runTest {
        val settings = (repository.getPomodoroSettings() as Result.Success).value

        assertEquals(AppConfig.Timer.DEFAULT_WORK_MINUTES, settings.workDurationMinutes)
        assertEquals(AppConfig.Timer.DEFAULT_SHORT_BREAK_MINUTES, settings.shortBreakMinutes)
        assertEquals(AppConfig.Timer.DEFAULT_LONG_BREAK_MINUTES, settings.longBreakMinutes)
        assertEquals(AppConfig.Timer.DEFAULT_CYCLES, settings.cyclesBeforeLongBreak)
    }

    @Test
    fun `updatePomodoroSettings persists settings`() = runTest {
        val newSettings = PomodoroSettings(
            workDurationMinutes = 50,
            shortBreakMinutes = 10,
            longBreakMinutes = 30,
            cyclesBeforeLongBreak = 6
        )

        repository.updatePomodoroSettings(newSettings)
        val retrieved = (repository.getPomodoroSettings() as Result.Success).value

        assertEquals(50, retrieved.workDurationMinutes)
        assertEquals(10, retrieved.shortBreakMinutes)
        assertEquals(30, retrieved.longBreakMinutes)
        assertEquals(6, retrieved.cyclesBeforeLongBreak)
    }

    @Test
    fun `getPomodoroSettingsFlow emits updates`() = runTest {
        val initial = repository.getPomodoroSettingsFlow().first()
        assertEquals(AppConfig.Timer.DEFAULT_WORK_MINUTES, initial.workDurationMinutes)

        val newSettings = PomodoroSettings(workDurationMinutes = 45)
        repository.updatePomodoroSettings(newSettings)

        val updated = repository.getPomodoroSettingsFlow().first()
        assertEquals(45, updated.workDurationMinutes)
    }

    // Generic Setting Tests

    @Test
    fun `getSetting returns default when not set`() = runTest {
        val result = (repository.getSetting("test_key", "default_value") as Result.Success).value
        assertEquals("default_value", result)
    }

    @Test
    fun `setSetting persists value`() = runTest {
        repository.setSetting("test_key", "test_value")

        val result = (repository.getSetting("test_key", "default") as Result.Success).value
        assertEquals("test_value", result)
    }

    @Test
    fun `setSetting overwrites existing value`() = runTest {
        repository.setSetting("test_key", "value1")
        repository.setSetting("test_key", "value2")

        val result = (repository.getSetting("test_key", "default") as Result.Success).value
        assertEquals("value2", result)
    }

    // Allowed Apps Tests

    @Test
    fun `getAllowedApps returns empty list initially`() = runTest {
        val apps = (repository.getAllowedApps() as Result.Success).value
        assertTrue(apps.isEmpty())
    }

    @Test
    fun `setAllowedApps persists apps`() = runTest {
        val packages = listOf("com.app.one", "com.app.two", "com.app.three")

        repository.setAllowedApps(packages)
        val result = (repository.getAllowedApps() as Result.Success).value

        assertEquals(3, result.size)
        assertTrue(result.containsAll(packages))
    }

    @Test
    fun `setAllowedApps overwrites existing apps`() = runTest {
        repository.setAllowedApps(listOf("com.app.one", "com.app.two"))
        repository.setAllowedApps(listOf("com.app.three"))

        val result = (repository.getAllowedApps() as Result.Success).value

        assertEquals(1, result.size)
        assertEquals("com.app.three", result[0])
    }

    @Test
    fun `getAllowedAppsFlow emits updates`() = runTest {
        val initial = repository.getAllowedAppsFlow().first()
        assertTrue(initial.isEmpty())

        repository.setAllowedApps(listOf("com.app.one"))
        val updated = repository.getAllowedAppsFlow().first()

        assertEquals(1, updated.size)
        assertEquals("com.app.one", updated[0])
    }

    // Edge Cases

    @Test
    fun `updatePomodoroSettings preserves other fields`() = runTest {
        val settings = PomodoroSettings(
            workDurationMinutes = 30,
            focusModeEnabled = true,
            focusModeStrict = true,
            autoLoopEnabled = true
        )

        repository.updatePomodoroSettings(settings)
        val retrieved = (repository.getPomodoroSettings() as Result.Success).value

        assertTrue(retrieved.focusModeEnabled)
        assertTrue(retrieved.focusModeStrict)
        assertTrue(retrieved.autoLoopEnabled)
    }
}
