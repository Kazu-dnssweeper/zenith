package com.iterio.app.domain.usecase

import com.iterio.app.domain.common.DomainError
import com.iterio.app.domain.common.Result
import com.iterio.app.domain.model.PomodoroSettings
import com.iterio.app.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * UpdatePomodoroSettingsUseCase のユニットテスト
 */
class UpdatePomodoroSettingsUseCaseTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: UpdatePomodoroSettingsUseCase

    @Before
    fun setup() {
        settingsRepository = mockk(relaxed = true)
        useCase = UpdatePomodoroSettingsUseCase(settingsRepository)
    }

    @Test
    fun `updates work duration`() = runTest {
        val current = PomodoroSettings(workDurationMinutes = 25)
        coEvery { settingsRepository.getPomodoroSettings() } returns Result.Success(current)

        val settingsSlot = slot<PomodoroSettings>()
        coEvery { settingsRepository.updatePomodoroSettings(capture(settingsSlot)) } returns Result.Success(Unit)

        useCase.updateWorkDuration(30)

        assertEquals(30, settingsSlot.captured.workDurationMinutes)
    }

    @Test
    fun `updates short break duration`() = runTest {
        val current = PomodoroSettings(shortBreakMinutes = 5)
        coEvery { settingsRepository.getPomodoroSettings() } returns Result.Success(current)

        val settingsSlot = slot<PomodoroSettings>()
        coEvery { settingsRepository.updatePomodoroSettings(capture(settingsSlot)) } returns Result.Success(Unit)

        useCase.updateShortBreak(10)

        assertEquals(10, settingsSlot.captured.shortBreakMinutes)
    }

    @Test
    fun `updates long break duration`() = runTest {
        val current = PomodoroSettings(longBreakMinutes = 15)
        coEvery { settingsRepository.getPomodoroSettings() } returns Result.Success(current)

        val settingsSlot = slot<PomodoroSettings>()
        coEvery { settingsRepository.updatePomodoroSettings(capture(settingsSlot)) } returns Result.Success(Unit)

        useCase.updateLongBreak(20)

        assertEquals(20, settingsSlot.captured.longBreakMinutes)
    }

    @Test
    fun `updates cycles before long break`() = runTest {
        val current = PomodoroSettings(cyclesBeforeLongBreak = 4)
        coEvery { settingsRepository.getPomodoroSettings() } returns Result.Success(current)

        val settingsSlot = slot<PomodoroSettings>()
        coEvery { settingsRepository.updatePomodoroSettings(capture(settingsSlot)) } returns Result.Success(Unit)

        useCase.updateCycles(6)

        assertEquals(6, settingsSlot.captured.cyclesBeforeLongBreak)
    }

    @Test
    fun `toggles focus mode`() = runTest {
        val current = PomodoroSettings(focusModeEnabled = false)
        coEvery { settingsRepository.getPomodoroSettings() } returns Result.Success(current)

        val settingsSlot = slot<PomodoroSettings>()
        coEvery { settingsRepository.updatePomodoroSettings(capture(settingsSlot)) } returns Result.Success(Unit)

        useCase.toggleFocusMode(true)

        assertTrue(settingsSlot.captured.focusModeEnabled)
    }

    @Test
    fun `toggles auto loop`() = runTest {
        val current = PomodoroSettings(autoLoopEnabled = false)
        coEvery { settingsRepository.getPomodoroSettings() } returns Result.Success(current)

        val settingsSlot = slot<PomodoroSettings>()
        coEvery { settingsRepository.updatePomodoroSettings(capture(settingsSlot)) } returns Result.Success(Unit)

        useCase.toggleAutoLoop(true)

        assertTrue(settingsSlot.captured.autoLoopEnabled)
    }

    @Test
    fun `toggles review feature`() = runTest {
        val current = PomodoroSettings(reviewEnabled = true)
        coEvery { settingsRepository.getPomodoroSettings() } returns Result.Success(current)

        val settingsSlot = slot<PomodoroSettings>()
        coEvery { settingsRepository.updatePomodoroSettings(capture(settingsSlot)) } returns Result.Success(Unit)

        useCase.toggleReview(false)

        assertFalse(settingsSlot.captured.reviewEnabled)
    }

    @Test
    fun `toggles notifications`() = runTest {
        val current = PomodoroSettings(notificationsEnabled = true)
        coEvery { settingsRepository.getPomodoroSettings() } returns Result.Success(current)

        val settingsSlot = slot<PomodoroSettings>()
        coEvery { settingsRepository.updatePomodoroSettings(capture(settingsSlot)) } returns Result.Success(Unit)

        useCase.toggleNotifications(false)

        assertFalse(settingsSlot.captured.notificationsEnabled)
    }

    @Test
    fun `updates full settings`() = runTest {
        val newSettings = PomodoroSettings(
            workDurationMinutes = 30,
            shortBreakMinutes = 10,
            longBreakMinutes = 20,
            cyclesBeforeLongBreak = 6
        )
        coEvery { settingsRepository.updatePomodoroSettings(newSettings) } returns Result.Success(Unit)

        useCase.updateSettings(newSettings)

        coVerify { settingsRepository.updatePomodoroSettings(newSettings) }
    }

    @Test
    fun `preserves other settings when updating single field`() = runTest {
        val current = PomodoroSettings(
            workDurationMinutes = 25,
            shortBreakMinutes = 5,
            longBreakMinutes = 15,
            focusModeEnabled = true,
            reviewEnabled = true
        )
        coEvery { settingsRepository.getPomodoroSettings() } returns Result.Success(current)

        val settingsSlot = slot<PomodoroSettings>()
        coEvery { settingsRepository.updatePomodoroSettings(capture(settingsSlot)) } returns Result.Success(Unit)

        useCase.updateWorkDuration(30)

        assertEquals(5, settingsSlot.captured.shortBreakMinutes)
        assertEquals(15, settingsSlot.captured.longBreakMinutes)
        assertTrue(settingsSlot.captured.focusModeEnabled)
        assertTrue(settingsSlot.captured.reviewEnabled)
    }

    @Test
    fun `returns success on update`() = runTest {
        val current = PomodoroSettings()
        coEvery { settingsRepository.getPomodoroSettings() } returns Result.Success(current)
        coEvery { settingsRepository.updatePomodoroSettings(any()) } returns Result.Success(Unit)

        val result = useCase.updateWorkDuration(30)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `returns failure on error`() = runTest {
        coEvery { settingsRepository.getPomodoroSettings() } returns Result.Failure(DomainError.DatabaseError("DB error"))

        val result = useCase.updateWorkDuration(30)

        assertTrue(result.isFailure)
    }
}
