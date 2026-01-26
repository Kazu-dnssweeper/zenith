package com.iterio.app.ui.screens.stats

import app.cash.turbine.test
import com.iterio.app.domain.model.SubscriptionStatus
import com.iterio.app.domain.model.SubscriptionType
import com.iterio.app.domain.repository.DailyStatsRepository
import com.iterio.app.domain.repository.StudySessionRepository
import com.iterio.app.testutil.CoroutineTestRule
import com.iterio.app.ui.premium.PremiumManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

/**
 * StatsViewModel のユニットテスト
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModelTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var dailyStatsRepository: DailyStatsRepository
    private lateinit var studySessionRepository: StudySessionRepository
    private lateinit var premiumManager: PremiumManager
    private val subscriptionStatusFlow = MutableStateFlow(SubscriptionStatus())

    @Before
    fun setup() {
        dailyStatsRepository = mockk()
        studySessionRepository = mockk()
        premiumManager = mockk()

        // Default mocks
        every { premiumManager.subscriptionStatus } returns subscriptionStatusFlow
        coEvery { studySessionRepository.getTotalMinutesForDay(any()) } returns 0
        coEvery { studySessionRepository.getTotalCyclesForDay(any()) } returns 0
        coEvery { dailyStatsRepository.getCurrentStreak() } returns 0
        coEvery { dailyStatsRepository.getMaxStreak() } returns 0
        coEvery { dailyStatsRepository.getTotalMinutesBetweenDates(any(), any()) } returns 0
        coEvery { studySessionRepository.getSessionCount() } returns 0
    }

    private fun createViewModel() = StatsViewModel(
        dailyStatsRepository = dailyStatsRepository,
        studySessionRepository = studySessionRepository,
        premiumManager = premiumManager
    )

    @Test
    fun `initial state is loading`() = runTest {
        val vm = createViewModel()

        vm.uiState.test {
            val initial = awaitItem()
            assertTrue(initial.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loads today minutes`() = runTest {
        coEvery { studySessionRepository.getTotalMinutesForDay(any()) } returns 90

        val vm = createViewModel()
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(90, state.todayMinutes)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loads today sessions`() = runTest {
        coEvery { studySessionRepository.getTotalCyclesForDay(any()) } returns 3

        val vm = createViewModel()
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(3, state.todaySessions)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loads current streak`() = runTest {
        coEvery { dailyStatsRepository.getCurrentStreak() } returns 14

        val vm = createViewModel()
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(14, state.currentStreak)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loads max streak`() = runTest {
        coEvery { dailyStatsRepository.getMaxStreak() } returns 30

        val vm = createViewModel()
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(30, state.maxStreak)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loads this week minutes`() = runTest {
        // First call is for week, second for month, etc.
        coEvery { dailyStatsRepository.getTotalMinutesBetweenDates(any(), any()) } returnsMany listOf(
            300, // week
            1200, // month
            3600 // last 30 days for average
        )

        val vm = createViewModel()
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(300, state.thisWeekMinutes)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loads total sessions count`() = runTest {
        coEvery { studySessionRepository.getSessionCount() } returns 150

        val vm = createViewModel()
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(150, state.totalSessions)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `calculates average daily minutes`() = runTest {
        // 900 minutes over 30 days = 30 average
        coEvery { dailyStatsRepository.getTotalMinutesBetweenDates(any(), any()) } returnsMany listOf(
            0, 0, 900
        )

        val vm = createViewModel()
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(30, state.averageDailyMinutes)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loads weekly data with 7 days`() = runTest {
        coEvery { dailyStatsRepository.getTotalMinutesBetweenDates(any(), any()) } returns 60

        val vm = createViewModel()
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(7, state.weeklyData.size)
            assertEquals("月", state.weeklyData[0].dayOfWeek)
            assertEquals("日", state.weeklyData[6].dayOfWeek)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `sets loading false after data load`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refresh reloads all data`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        coEvery { studySessionRepository.getTotalMinutesForDay(any()) } returns 200
        vm.refresh()
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(200, state.todayMinutes)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isPremium reflects subscription status`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.isPremium.test {
            assertFalse(awaitItem())

            subscriptionStatusFlow.value = SubscriptionStatus(
                type = SubscriptionType.MONTHLY,
                expiresAt = LocalDateTime.now().plusMonths(1)
            )
            assertTrue(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `startTrial calls premiumManager`() = runTest {
        coEvery { premiumManager.startTrial() } returns Unit

        val vm = createViewModel()
        advanceUntilIdle()

        vm.startTrial()
        advanceUntilIdle()

        coVerify { premiumManager.startTrial() }
    }
}
