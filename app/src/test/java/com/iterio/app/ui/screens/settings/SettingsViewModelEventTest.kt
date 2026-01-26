package com.iterio.app.ui.screens.settings

import com.iterio.app.domain.common.Result
import com.iterio.app.domain.model.PomodoroSettings
import com.iterio.app.domain.model.SubscriptionStatus
import com.iterio.app.domain.repository.ReviewTaskRepository
import com.iterio.app.domain.repository.SettingsRepository
import com.iterio.app.domain.usecase.UpdatePomodoroSettingsUseCase
import com.iterio.app.ui.bgm.BgmManager
import com.iterio.app.ui.premium.PremiumManager
import com.iterio.app.util.LocaleManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * SettingsViewModel の onEvent テスト
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelEventTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var updatePomodoroSettingsUseCase: UpdatePomodoroSettingsUseCase
    private lateinit var premiumManager: PremiumManager
    private lateinit var localeManager: LocaleManager
    private lateinit var reviewTaskRepository: ReviewTaskRepository
    private lateinit var bgmManager: BgmManager
    private lateinit var viewModel: SettingsViewModel

    private val testDispatcher = StandardTestDispatcher()
    private val subscriptionStatusFlow = MutableStateFlow(SubscriptionStatus())

    // BGM-related StateFlows
    private val selectedTrackFlow = MutableStateFlow<com.iterio.app.domain.model.BgmTrack?>(null)
    private val volumeFlow = MutableStateFlow(0.5f)
    private val autoPlayFlow = MutableStateFlow(true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        settingsRepository = mockk(relaxed = true)
        updatePomodoroSettingsUseCase = mockk(relaxed = true)
        premiumManager = mockk(relaxed = true)
        localeManager = mockk()
        reviewTaskRepository = mockk(relaxed = true)
        bgmManager = mockk(relaxed = true)

        coEvery { settingsRepository.getPomodoroSettings() } returns Result.Success(PomodoroSettings())
        coEvery { settingsRepository.getAllowedApps() } returns Result.Success(emptyList<String>())
        coEvery { updatePomodoroSettingsUseCase.updateSettings(any()) } returns Result.Success(Unit)
        every { premiumManager.subscriptionStatus } returns subscriptionStatusFlow
        coEvery { premiumManager.isPremium() } returns false
        every { localeManager.getCurrentLanguage() } returns "ja"
        coEvery { reviewTaskRepository.getTotalCount() } returns Result.Success(0)
        coEvery { reviewTaskRepository.getIncompleteCount() } returns Result.Success(0)
        every { bgmManager.selectedTrack } returns selectedTrackFlow
        every { bgmManager.volume } returns volumeFlow
        every { bgmManager.autoPlayEnabled } returns autoPlayFlow

        viewModel = SettingsViewModel(
            settingsRepository = settingsRepository,
            updatePomodoroSettingsUseCase = updatePomodoroSettingsUseCase,
            premiumManager = premiumManager,
            localeManager = localeManager,
            reviewTaskRepository = reviewTaskRepository,
            bgmManager = bgmManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onEvent ToggleNotifications updates state`() = runTest {
        advanceUntilIdle()

        viewModel.onEvent(SettingsEvent.ToggleNotifications(false))
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.notificationsEnabled)
    }

    @Test
    fun `onEvent ToggleReviewIntervals updates state`() = runTest {
        advanceUntilIdle()

        viewModel.onEvent(SettingsEvent.ToggleReviewIntervals(false))
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.reviewIntervalsEnabled)
    }

    @Test
    fun `onEvent UpdateWorkDuration updates state`() = runTest {
        advanceUntilIdle()

        viewModel.onEvent(SettingsEvent.UpdateWorkDuration(50))
        advanceUntilIdle()

        assertEquals(50, viewModel.uiState.value.workDurationMinutes)
    }

    @Test
    fun `onEvent UpdateShortBreak updates state`() = runTest {
        advanceUntilIdle()

        viewModel.onEvent(SettingsEvent.UpdateShortBreak(10))
        advanceUntilIdle()

        assertEquals(10, viewModel.uiState.value.shortBreakMinutes)
    }

    @Test
    fun `onEvent UpdateLongBreak updates state`() = runTest {
        advanceUntilIdle()

        viewModel.onEvent(SettingsEvent.UpdateLongBreak(30))
        advanceUntilIdle()

        assertEquals(30, viewModel.uiState.value.longBreakMinutes)
    }

    @Test
    fun `onEvent UpdateCycles updates state`() = runTest {
        advanceUntilIdle()

        viewModel.onEvent(SettingsEvent.UpdateCycles(6))
        advanceUntilIdle()

        assertEquals(6, viewModel.uiState.value.cyclesBeforeLongBreak)
    }

    @Test
    fun `onEvent ToggleFocusMode updates state`() = runTest {
        advanceUntilIdle()

        viewModel.onEvent(SettingsEvent.ToggleFocusMode(false))
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.focusModeEnabled)
    }

    @Test
    fun `onEvent ToggleFocusModeStrict updates state`() = runTest {
        advanceUntilIdle()

        viewModel.onEvent(SettingsEvent.ToggleFocusModeStrict(true))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.focusModeStrict)
    }

    @Test
    fun `onEvent ToggleAutoLoop updates state`() = runTest {
        advanceUntilIdle()

        viewModel.onEvent(SettingsEvent.ToggleAutoLoop(true))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.autoLoopEnabled)
    }

    @Test
    fun `onEvent StartTrial calls premiumManager`() = runTest {
        advanceUntilIdle()

        viewModel.onEvent(SettingsEvent.StartTrial)
        advanceUntilIdle()

        coVerify { premiumManager.startTrial() }
    }
}
