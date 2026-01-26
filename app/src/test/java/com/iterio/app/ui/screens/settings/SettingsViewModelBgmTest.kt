package com.iterio.app.ui.screens.settings

import app.cash.turbine.test
import com.iterio.app.domain.common.Result
import com.iterio.app.domain.model.BgmTrack
import com.iterio.app.domain.model.BgmTracks
import com.iterio.app.domain.model.PomodoroSettings
import com.iterio.app.domain.model.SubscriptionStatus
import com.iterio.app.domain.repository.ReviewTaskRepository
import com.iterio.app.domain.repository.SettingsRepository
import com.iterio.app.domain.usecase.UpdatePomodoroSettingsUseCase
import com.iterio.app.ui.bgm.BgmManager
import com.iterio.app.ui.premium.PremiumManager
import com.iterio.app.util.LocaleManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * SettingsViewModel の BGM 関連機能テスト
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelBgmTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var updatePomodoroSettingsUseCase: UpdatePomodoroSettingsUseCase
    private lateinit var premiumManager: PremiumManager
    private lateinit var localeManager: LocaleManager
    private lateinit var reviewTaskRepository: ReviewTaskRepository
    private lateinit var bgmManager: BgmManager

    private val testDispatcher = StandardTestDispatcher()

    // Store StateFlows at class level to ensure they're properly captured by mocks
    private val subscriptionStatusFlow = MutableStateFlow(SubscriptionStatus())
    private var selectedTrackFlow = MutableStateFlow<BgmTrack?>(null)
    private var volumeFlow = MutableStateFlow(0.5f)
    private var autoPlayFlow = MutableStateFlow(true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Reset flows to defaults
        selectedTrackFlow = MutableStateFlow(null)
        volumeFlow = MutableStateFlow(0.5f)
        autoPlayFlow = MutableStateFlow(true)

        settingsRepository = mockk(relaxed = true)
        updatePomodoroSettingsUseCase = mockk(relaxed = true)
        premiumManager = mockk(relaxed = true)
        localeManager = mockk()
        reviewTaskRepository = mockk(relaxed = true)
        bgmManager = mockk(relaxed = true)

        // Default mock responses
        coEvery { settingsRepository.getPomodoroSettings() } returns Result.Success(PomodoroSettings())
        coEvery { settingsRepository.getAllowedApps() } returns Result.Success(emptyList<String>())
        every { premiumManager.subscriptionStatus } returns subscriptionStatusFlow
        coEvery { premiumManager.isPremium() } returns false
        every { localeManager.getCurrentLanguage() } returns "ja"
        coEvery { reviewTaskRepository.getTotalCount() } returns Result.Success(0)
        coEvery { reviewTaskRepository.getIncompleteCount() } returns Result.Success(0)

        // BGM mock responses
        every { bgmManager.selectedTrack } returns selectedTrackFlow
        every { bgmManager.volume } returns volumeFlow
        every { bgmManager.autoPlayEnabled } returns autoPlayFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): SettingsViewModel {
        return SettingsViewModel(
            settingsRepository = settingsRepository,
            updatePomodoroSettingsUseCase = updatePomodoroSettingsUseCase,
            premiumManager = premiumManager,
            localeManager = localeManager,
            reviewTaskRepository = reviewTaskRepository,
            bgmManager = bgmManager
        )
    }

    @Test
    fun `initial state loads BGM settings from BgmManager`() = runTest {
        // Arrange
        val track = BgmTracks.all.first()
        selectedTrackFlow = MutableStateFlow(track)
        volumeFlow = MutableStateFlow(0.7f)
        autoPlayFlow = MutableStateFlow(false)
        every { bgmManager.selectedTrack } returns selectedTrackFlow
        every { bgmManager.volume } returns volumeFlow
        every { bgmManager.autoPlayEnabled } returns autoPlayFlow

        // Act
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals("BGMトラックIDが正しくロードされるべき", track.id, state.bgmTrackId)
        assertEquals("音量が正しくロードされるべき", 0.7f, state.bgmVolume)
        assertFalse("自動再生設定が正しくロードされるべき", state.bgmAutoPlayEnabled)
    }

    @Test
    fun `updateBgmVolume updates state and calls BgmManager`() = runTest {
        // Arrange
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Act
        viewModel.updateBgmVolume(0.8f)
        advanceUntilIdle()

        // Assert
        assertEquals("音量が更新されるべき", 0.8f, viewModel.uiState.value.bgmVolume)
        verify { bgmManager.setVolume(0.8f) }
    }

    @Test
    fun `toggleBgmAutoPlay updates state and calls BgmManager`() = runTest {
        // Arrange
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Act
        viewModel.toggleBgmAutoPlay(false)
        advanceUntilIdle()

        // Assert
        assertFalse("自動再生がOFFになるべき", viewModel.uiState.value.bgmAutoPlayEnabled)
        verify { bgmManager.setAutoPlay(false) }
    }

    @Test
    fun `selectBgmTrack updates state and closes dialog`() = runTest {
        // Arrange
        val viewModel = createViewModel()
        advanceUntilIdle()

        // ダイアログを開く
        viewModel.showBgmSelector()
        advanceUntilIdle()
        assertTrue("ダイアログが開いているべき", viewModel.uiState.value.showBgmSelectorDialog)

        // Act
        val track = BgmTracks.all.first()
        viewModel.selectBgmTrack(track)
        advanceUntilIdle()

        // Assert
        assertEquals("トラックIDが更新されるべき", track.id, viewModel.uiState.value.bgmTrackId)
        assertFalse("ダイアログが閉じるべき", viewModel.uiState.value.showBgmSelectorDialog)
        verify { bgmManager.selectTrack(track) }
    }

    @Test
    fun `selectBgmTrack with null clears trackId`() = runTest {
        // Arrange
        val track = BgmTracks.all.first()
        selectedTrackFlow = MutableStateFlow(track)
        every { bgmManager.selectedTrack } returns selectedTrackFlow
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Act
        viewModel.selectBgmTrack(null)
        advanceUntilIdle()

        // Assert
        assertNull("トラックIDがnullになるべき", viewModel.uiState.value.bgmTrackId)
        verify { bgmManager.selectTrack(null) }
    }

    @Test
    fun `showBgmSelector opens dialog`() = runTest {
        // Arrange
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertFalse("初期状態ではダイアログは閉じている", viewModel.uiState.value.showBgmSelectorDialog)

        // Act
        viewModel.showBgmSelector()
        advanceUntilIdle()

        // Assert
        assertTrue("ダイアログが開くべき", viewModel.uiState.value.showBgmSelectorDialog)
    }

    @Test
    fun `hideBgmSelector closes dialog`() = runTest {
        // Arrange
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.showBgmSelector()
        advanceUntilIdle()
        assertTrue("ダイアログが開いている", viewModel.uiState.value.showBgmSelectorDialog)

        // Act
        viewModel.hideBgmSelector()
        advanceUntilIdle()

        // Assert
        assertFalse("ダイアログが閉じるべき", viewModel.uiState.value.showBgmSelectorDialog)
    }

    @Test
    fun `uiState emits BGM state changes`() = runTest {
        // Arrange
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Assert with Turbine
        viewModel.uiState.test {
            val initial = awaitItem()
            assertEquals("初期音量は0.5f", 0.5f, initial.bgmVolume)

            // Act
            viewModel.updateBgmVolume(0.3f)
            advanceUntilIdle()

            val updated = awaitItem()
            assertEquals("音量が更新される", 0.3f, updated.bgmVolume)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
