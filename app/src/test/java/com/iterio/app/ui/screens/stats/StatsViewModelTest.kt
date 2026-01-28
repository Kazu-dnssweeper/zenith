package com.iterio.app.ui.screens.stats

import app.cash.turbine.test
import com.iterio.app.domain.common.Result
import com.iterio.app.domain.model.SubscriptionStatus
import com.iterio.app.domain.model.SubscriptionType
import com.iterio.app.domain.repository.DailyStatsRepository
import com.iterio.app.domain.repository.DayStats
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
import java.time.DayOfWeek
import java.time.LocalDate
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
        coEvery { studySessionRepository.getTotalMinutesForDay(any()) } returns Result.Success(0)
        coEvery { studySessionRepository.getTotalCyclesForDay(any()) } returns Result.Success(0)
        coEvery { dailyStatsRepository.getCurrentStreak() } returns Result.Success(0)
        coEvery { dailyStatsRepository.getMaxStreak() } returns Result.Success(0)
        coEvery { dailyStatsRepository.getTotalMinutesBetweenDates(any(), any()) } returns Result.Success(0)
        coEvery { studySessionRepository.getSessionCount() } returns Result.Success(0)
        coEvery { dailyStatsRepository.getWeeklyData(any()) } returns Result.Success(emptyList())
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
        coEvery { studySessionRepository.getTotalMinutesForDay(any()) } returns Result.Success(90)

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
        coEvery { studySessionRepository.getTotalCyclesForDay(any()) } returns Result.Success(3)

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
        coEvery { dailyStatsRepository.getCurrentStreak() } returns Result.Success(14)

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
        coEvery { dailyStatsRepository.getMaxStreak() } returns Result.Success(30)

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
            Result.Success(300), // week
            Result.Success(1200), // month
            Result.Success(3600) // last 30 days for average
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
        coEvery { studySessionRepository.getSessionCount() } returns Result.Success(150)

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
            Result.Success(0), Result.Success(0), Result.Success(900)
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
        val weekStart = LocalDate.now().with(DayOfWeek.MONDAY)
        val weeklyData = (0..6).map { dayOffset ->
            DayStats(
                dayOfWeek = listOf("月", "火", "水", "木", "金", "土", "日")[dayOffset],
                date = weekStart.plusDays(dayOffset.toLong()),
                minutes = 60
            )
        }
        coEvery { dailyStatsRepository.getWeeklyData(any()) } returns Result.Success(weeklyData)

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

        coEvery { studySessionRepository.getTotalMinutesForDay(any()) } returns Result.Success(200)
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

    @Test
    fun `loadStats sets all fields correctly with parallel execution`() = runTest {
        coEvery { studySessionRepository.getTotalMinutesForDay(any()) } returns Result.Success(45)
        coEvery { studySessionRepository.getTotalCyclesForDay(any()) } returns Result.Success(3)
        coEvery { dailyStatsRepository.getCurrentStreak() } returns Result.Success(7)
        coEvery { dailyStatsRepository.getMaxStreak() } returns Result.Success(14)
        coEvery { studySessionRepository.getSessionCount() } returns Result.Success(100)
        coEvery { dailyStatsRepository.getTotalMinutesBetweenDates(any(), any()) } returnsMany listOf(
            Result.Success(200),  // week
            Result.Success(800),  // month
            Result.Success(600)   // last 30 days
        )
        val weekStart = LocalDate.now().with(DayOfWeek.MONDAY)
        val weeklyData = (0..6).map { dayOffset ->
            DayStats(
                dayOfWeek = listOf("月", "火", "水", "木", "金", "土", "日")[dayOffset],
                date = weekStart.plusDays(dayOffset.toLong()),
                minutes = 30
            )
        }
        coEvery { dailyStatsRepository.getWeeklyData(any()) } returns Result.Success(weeklyData)

        val vm = createViewModel()
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(45, state.todayMinutes)
            assertEquals(3, state.todaySessions)
            assertEquals(7, state.currentStreak)
            assertEquals(14, state.maxStreak)
            assertEquals(100, state.totalSessions)
            assertEquals(200, state.thisWeekMinutes)
            assertEquals(800, state.thisMonthMinutes)
            assertEquals(20, state.averageDailyMinutes) // 600 / 30
            assertEquals(7, state.weeklyData.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ========== エラーパス テスト ===========

    @Test
    fun `todayMinutes defaults to 0 on failure`() = runTest {
        coEvery { studySessionRepository.getTotalMinutesForDay(any()) } returns
            Result.Failure(com.iterio.app.domain.common.DomainError.DatabaseError("DB error"))

        val vm = createViewModel()
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(0, state.todayMinutes)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `currentStreak defaults to 0 on failure`() = runTest {
        coEvery { dailyStatsRepository.getCurrentStreak() } returns
            Result.Failure(com.iterio.app.domain.common.DomainError.DatabaseError("DB error"))

        val vm = createViewModel()
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(0, state.currentStreak)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `maxStreak defaults to 0 on failure`() = runTest {
        coEvery { dailyStatsRepository.getMaxStreak() } returns
            Result.Failure(com.iterio.app.domain.common.DomainError.DatabaseError("DB error"))

        val vm = createViewModel()
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(0, state.maxStreak)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `totalSessions defaults to 0 on failure`() = runTest {
        coEvery { studySessionRepository.getSessionCount() } returns
            Result.Failure(com.iterio.app.domain.common.DomainError.DatabaseError("DB error"))

        val vm = createViewModel()
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(0, state.totalSessions)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `thisWeekMinutes defaults to 0 on failure`() = runTest {
        coEvery { dailyStatsRepository.getTotalMinutesBetweenDates(any(), any()) } returns
            Result.Failure(com.iterio.app.domain.common.DomainError.DatabaseError("DB error"))

        val vm = createViewModel()
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(0, state.thisWeekMinutes)
            assertEquals(0, state.thisMonthMinutes)
            assertEquals(0, state.averageDailyMinutes)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
