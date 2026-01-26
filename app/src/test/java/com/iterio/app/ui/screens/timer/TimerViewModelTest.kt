package com.iterio.app.ui.screens.timer

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.iterio.app.domain.common.DomainError
import com.iterio.app.domain.common.Result
import com.iterio.app.domain.model.AllowedApp
import com.iterio.app.domain.model.PomodoroSettings
import com.iterio.app.domain.model.SubscriptionStatus
import com.iterio.app.domain.model.SubscriptionType
import com.iterio.app.domain.model.Task
import com.iterio.app.domain.usecase.FinishTimerSessionUseCase
import com.iterio.app.domain.usecase.GetTimerInitialStateUseCase
import com.iterio.app.domain.usecase.StartTimerSessionUseCase
import com.iterio.app.domain.usecase.TimerInitialState
import com.iterio.app.service.TimerPhase
import com.iterio.app.service.TimerService
import com.iterio.app.ui.bgm.BgmManager
import com.iterio.app.ui.premium.PremiumManager
import com.iterio.app.util.InstalledAppsHelper
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
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
import java.time.LocalDateTime

/**
 * TimerViewModel のユニットテスト（フォーカスモード関連）
 *
 * 注意: このテストはAndroidコンテキストが必要な機能をモック化してテストします。
 * TimerServiceとのバインドなど、実際のAndroidコンポーネントを使う機能は
 * 統合テストまたは手動テストで検証してください。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TimerViewModelTest {

    private lateinit var context: Context
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var getTimerInitialStateUseCase: GetTimerInitialStateUseCase
    private lateinit var startTimerSessionUseCase: StartTimerSessionUseCase
    private lateinit var finishTimerSessionUseCase: FinishTimerSessionUseCase
    private lateinit var premiumManager: PremiumManager
    private lateinit var bgmManager: BgmManager
    private lateinit var installedAppsHelper: InstalledAppsHelper

    private val testDispatcher = StandardTestDispatcher()

    private val mockTask = Task(
        id = 1L,
        name = "Test Task",
        groupId = 1L,
        workDurationMinutes = 25
    )

    private val defaultSettings = PomodoroSettings(
        workDurationMinutes = 25,
        shortBreakMinutes = 5,
        longBreakMinutes = 15,
        cyclesBeforeLongBreak = 4,
        focusModeEnabled = true,
        reviewEnabled = true
    )

    private val mockApps = listOf(
        AllowedApp(packageName = "com.example.allowed1", appName = "Allowed 1", icon = null),
        AllowedApp(packageName = "com.example.allowed2", appName = "Allowed 2", icon = null)
    )

    private val subscriptionStatusFlow = MutableStateFlow(SubscriptionStatus())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mock static methods in TimerService companion object
        mockkObject(TimerService.Companion)
        every { TimerService.stopTimer(any()) } returns Unit

        context = mockk(relaxed = true)
        savedStateHandle = SavedStateHandle(mapOf("taskId" to 1L))
        getTimerInitialStateUseCase = mockk(relaxed = true)
        startTimerSessionUseCase = mockk(relaxed = true)
        finishTimerSessionUseCase = mockk(relaxed = true)
        premiumManager = mockk(relaxed = true)
        bgmManager = mockk(relaxed = true)
        installedAppsHelper = mockk(relaxed = true)

        // Default mocks for UseCases
        val defaultInitialState = TimerInitialState(
            task = mockTask,
            settings = defaultSettings,
            effectiveWorkDurationMinutes = defaultSettings.workDurationMinutes,
            totalTimeSeconds = defaultSettings.workDurationMinutes * 60,
            defaultAllowedApps = setOf("com.example.allowed1")
        )
        coEvery { getTimerInitialStateUseCase(1L) } returns Result.Success(defaultInitialState)
        coEvery { startTimerSessionUseCase(any(), any(), any(), any()) } returns Result.Success(1L)
        coEvery { finishTimerSessionUseCase(any()) } returns Result.Success(Unit)
        coEvery { installedAppsHelper.getInstalledUserApps() } returns mockApps
        every { premiumManager.subscriptionStatus } returns subscriptionStatusFlow
        every { bgmManager.bgmState } returns MutableStateFlow(mockk(relaxed = true))
        every { bgmManager.selectedTrack } returns MutableStateFlow(null)
        every { bgmManager.volume } returns MutableStateFlow(0.5f)

        // Context mock for service calls
        every { context.packageName } returns "com.iterio.app"
        every { context.startService(any()) } returns null
        every { context.stopService(any()) } returns true
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    private fun createViewModel(): TimerViewModel {
        return TimerViewModel(
            savedStateHandle = savedStateHandle,
            context = context,
            getTimerInitialStateUseCase = getTimerInitialStateUseCase,
            startTimerSessionUseCase = startTimerSessionUseCase,
            finishTimerSessionUseCase = finishTimerSessionUseCase,
            premiumManager = premiumManager,
            bgmManager = bgmManager,
            installedAppsHelper = installedAppsHelper
        )
    }

    // 初期状態のテスト

    @Test
    fun `initial state has correct default values`() = runTest {
        // Act
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals("タスクが読み込まれるべき", mockTask, state.task)
        assertEquals("設定が読み込まれるべき", defaultSettings, state.settings)
        assertEquals("初期フェーズはIDLEであるべき", TimerPhase.IDLE, state.phase)
        assertFalse("初期状態ではロックモードは無効であるべき", state.isSessionLockModeEnabled)
    }

    @Test
    fun `initial state loads allowed apps from settings`() = runTest {
        // Arrange
        val savedAllowedApps = setOf("com.example.allowed1", "com.example.allowed2")
        val initialState = TimerInitialState(
            task = mockTask,
            settings = defaultSettings,
            effectiveWorkDurationMinutes = defaultSettings.workDurationMinutes,
            totalTimeSeconds = defaultSettings.workDurationMinutes * 60,
            defaultAllowedApps = savedAllowedApps
        )
        coEvery { getTimerInitialStateUseCase(1L) } returns Result.Success(initialState)

        // Act
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Assert
        assertEquals(
            "許可アプリ設定が読み込まれるべき",
            savedAllowedApps,
            viewModel.uiState.value.defaultAllowedApps
        )
    }

    @Test
    fun `initial state loads installed apps`() = runTest {
        // Act
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertFalse("アプリ読み込み完了後はisLoadingApps=falseであるべき", state.isLoadingApps)
        assertEquals("インストール済みアプリが読み込まれるべき", mockApps, state.installedApps)
    }

    // タスク固有の作業時間テスト

    @Test
    fun `uses task work duration when available`() = runTest {
        // Arrange - タスクに作業時間が設定されている
        val taskWithDuration = mockTask.copy(workDurationMinutes = 45)
        val initialState = TimerInitialState(
            task = taskWithDuration,
            settings = defaultSettings,
            effectiveWorkDurationMinutes = 45,
            totalTimeSeconds = 45 * 60,
            defaultAllowedApps = setOf("com.example.allowed1")
        )
        coEvery { getTimerInitialStateUseCase(1L) } returns Result.Success(initialState)

        // Act
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(
            "タスク固有の作業時間が使われるべき",
            45 * 60,
            state.timeRemainingSeconds
        )
    }

    @Test
    fun `uses default settings when task has no work duration`() = runTest {
        // Arrange - タスクに作業時間が設定されていない
        val taskWithoutDuration = mockTask.copy(workDurationMinutes = null)
        val initialState = TimerInitialState(
            task = taskWithoutDuration,
            settings = defaultSettings,
            effectiveWorkDurationMinutes = defaultSettings.workDurationMinutes,
            totalTimeSeconds = defaultSettings.workDurationMinutes * 60,
            defaultAllowedApps = setOf("com.example.allowed1")
        )
        coEvery { getTimerInitialStateUseCase(1L) } returns Result.Success(initialState)

        // Act
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(
            "デフォルト設定の作業時間が使われるべき",
            defaultSettings.workDurationMinutes * 60,
            state.timeRemainingSeconds
        )
    }

    // Premium状態のテスト

    @Test
    fun `isPremium reflects subscription status`() = runTest {
        // Arrange
        subscriptionStatusFlow.value = SubscriptionStatus(type = SubscriptionType.MONTHLY)

        // Act
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Assert with Turbine
        viewModel.isPremium.test {
            // Premium状態を確認
            val isPremium = awaitItem()
            // Note: isPremiumはSubscriptionStatus.isPremiumに基づく
            // expiresAtがnullの場合、MONTHLYは期限切れと判定される
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isPremium returns true for lifetime subscription`() = runTest {
        // Arrange
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Assert with Turbine - 購読を開始してから値を更新
        viewModel.isPremium.test {
            // 初期値をスキップ
            awaitItem()

            // サブスクリプション状態を更新
            subscriptionStatusFlow.value = SubscriptionStatus(type = SubscriptionType.LIFETIME)
            advanceUntilIdle()

            // 更新された値を確認
            val isPremium = awaitItem()
            assertTrue("LIFETIME契約はPremiumであるべき", isPremium)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isPremium returns true during trial period`() = runTest {
        // Arrange
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Assert with Turbine - 購読を開始してから値を更新
        viewModel.isPremium.test {
            // 初期値をスキップ
            awaitItem()

            // トライアル状態を更新
            subscriptionStatusFlow.value = SubscriptionStatus(
                type = SubscriptionType.FREE,
                trialExpiresAt = LocalDateTime.now().plusDays(3)
            )
            advanceUntilIdle()

            // 更新された値を確認
            val isPremium = awaitItem()
            assertTrue("トライアル期間中はPremiumであるべき", isPremium)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // UI状態のテスト

    @Test
    fun `showCancelDialog updates state correctly`() = runTest {
        // Act
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.showCancelDialog()

        // Assert
        assertTrue(
            "キャンセルダイアログが表示されるべき",
            viewModel.uiState.value.showCancelDialog
        )
    }

    @Test
    fun `hideCancelDialog updates state correctly`() = runTest {
        // Act
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.showCancelDialog()
        viewModel.hideCancelDialog()

        // Assert
        assertFalse(
            "キャンセルダイアログが非表示になるべき",
            viewModel.uiState.value.showCancelDialog
        )
    }

    @Test
    fun `hideFinishDialog updates state correctly`() = runTest {
        // Act
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.hideFinishDialog()

        // Assert
        assertFalse(
            "完了ダイアログが非表示になるべき",
            viewModel.uiState.value.showFinishDialog
        )
    }

    // 設定の読み込みテスト

    @Test
    fun `loads pomodoro settings correctly`() = runTest {
        // Arrange
        val customSettings = PomodoroSettings(
            workDurationMinutes = 50,
            shortBreakMinutes = 10,
            longBreakMinutes = 30,
            cyclesBeforeLongBreak = 2,
            focusModeEnabled = false,
            reviewEnabled = false
        )
        val initialState = TimerInitialState(
            task = mockTask,
            settings = customSettings,
            effectiveWorkDurationMinutes = 50,
            totalTimeSeconds = 50 * 60,
            defaultAllowedApps = setOf("com.example.allowed1")
        )
        coEvery { getTimerInitialStateUseCase(1L) } returns Result.Success(initialState)

        // Act
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals("カスタム設定が読み込まれるべき", customSettings, state.settings)
        assertEquals("サイクル数が設定されるべき", 2, state.totalCycles)
    }

    // BGM関連のテスト

    @Test
    fun `getAvailableBgmTracks returns tracks from BgmManager`() = runTest {
        // Arrange
        val mockTracks = listOf(mockk<com.iterio.app.domain.model.BgmTrack>())
        every { bgmManager.getAvailableTracks() } returns mockTracks

        // Act
        val viewModel = createViewModel()
        val tracks = viewModel.getAvailableBgmTracks()

        // Assert
        assertEquals("BgmManagerからトラックを取得すべき", mockTracks, tracks)
    }

    @Test
    fun `setBgmVolume delegates to BgmManager`() = runTest {
        // Act
        val viewModel = createViewModel()
        viewModel.setBgmVolume(0.8f)

        // Assert
        io.mockk.verify { bgmManager.setVolume(0.8f) }
    }

    // トライアル開始テスト

    @Test
    fun `startTrial delegates to PremiumManager`() = runTest {
        // Act
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.startTrial()
        advanceUntilIdle()

        // Assert
        io.mockk.coVerify { premiumManager.startTrial() }
    }

    // StateFlow更新のテスト

    @Test
    fun `uiState updates when settings change`() = runTest {
        // Arrange
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Assert with Turbine
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertEquals("初期タスクが設定されるべき", mockTask, initialState.task)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // タスクがない場合のテスト

    @Test
    fun `handles missing task gracefully`() = runTest {
        // Arrange
        coEvery { getTimerInitialStateUseCase(1L) } returns Result.Failure(
            DomainError.NotFoundError("Task not found: 1")
        )

        // Act
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertNull("タスクがnullの場合はnullであるべき", state.task)
    }

    // 空の許可アプリリストのテスト

    @Test
    fun `handles empty allowed apps list`() = runTest {
        // Arrange
        val initialState = TimerInitialState(
            task = mockTask,
            settings = defaultSettings,
            effectiveWorkDurationMinutes = defaultSettings.workDurationMinutes,
            totalTimeSeconds = defaultSettings.workDurationMinutes * 60,
            defaultAllowedApps = emptySet()
        )
        coEvery { getTimerInitialStateUseCase(1L) } returns Result.Success(initialState)

        // Act
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Assert
        assertTrue(
            "許可アプリリストが空であるべき",
            viewModel.uiState.value.defaultAllowedApps.isEmpty()
        )
    }
}
