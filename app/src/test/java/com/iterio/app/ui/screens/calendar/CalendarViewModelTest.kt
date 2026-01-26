package com.iterio.app.ui.screens.calendar

import app.cash.turbine.test
import com.iterio.app.domain.model.DailyStats
import com.iterio.app.domain.model.ReviewTask
import com.iterio.app.domain.model.ScheduleType
import com.iterio.app.domain.model.SubscriptionStatus
import com.iterio.app.domain.model.SubscriptionType
import com.iterio.app.domain.model.Task
import com.iterio.app.domain.repository.DailyStatsRepository
import com.iterio.app.domain.repository.ReviewTaskRepository
import com.iterio.app.domain.repository.TaskRepository
import com.iterio.app.testutil.CoroutineTestRule
import com.iterio.app.ui.premium.PremiumManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

/**
 * CalendarViewModel のユニットテスト
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var dailyStatsRepository: DailyStatsRepository
    private lateinit var taskRepository: TaskRepository
    private lateinit var reviewTaskRepository: ReviewTaskRepository
    private lateinit var premiumManager: PremiumManager
    private val subscriptionStatusFlow = MutableStateFlow(SubscriptionStatus())

    @Before
    fun setup() {
        dailyStatsRepository = mockk()
        taskRepository = mockk()
        reviewTaskRepository = mockk()
        premiumManager = mockk()

        // Default mocks
        every { premiumManager.subscriptionStatus } returns subscriptionStatusFlow
        every { dailyStatsRepository.getStatsBetweenDates(any(), any()) } returns flowOf(emptyList())
        coEvery { taskRepository.getTaskCountByDateRange(any(), any()) } returns emptyMap()
        coEvery { reviewTaskRepository.getTaskCountByDateRange(any(), any()) } returns emptyMap()
    }

    private fun createViewModel() = CalendarViewModel(
        dailyStatsRepository = dailyStatsRepository,
        taskRepository = taskRepository,
        reviewTaskRepository = reviewTaskRepository,
        premiumManager = premiumManager
    )

    @Test
    fun `initial state has current month`() = runTest {
        val vm = createViewModel()

        vm.uiState.test {
            val initial = awaitItem()
            assertEquals(YearMonth.now(), initial.currentMonth)
            cancelAndIgnoreRemainingEvents()
        }
    }

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
    fun `loads daily stats for current month`() = runTest {
        val today = LocalDate.now()
        val stats = listOf(
            DailyStats(date = today, totalStudyMinutes = 60, sessionCount = 2, subjectBreakdown = emptyMap()),
            DailyStats(date = today.minusDays(1), totalStudyMinutes = 90, sessionCount = 3, subjectBreakdown = emptyMap())
        )
        every { dailyStatsRepository.getStatsBetweenDates(any(), any()) } returns flowOf(stats)

        val vm = createViewModel()
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.dailyStats.size)
            assertFalse(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loads task count by date`() = runTest {
        val today = LocalDate.now()
        val regularCounts = mapOf(today to 3, today.plusDays(1) to 2)
        val reviewCounts = mapOf(today to 1)
        coEvery { taskRepository.getTaskCountByDateRange(any(), any()) } returns regularCounts
        coEvery { reviewTaskRepository.getTaskCountByDateRange(any(), any()) } returns reviewCounts

        val vm = createViewModel()
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            // today: 3 regular + 1 review = 4
            assertEquals(4, state.taskCountByDate[today])
            // tomorrow: 2 regular
            assertEquals(2, state.taskCountByDate[today.plusDays(1)])
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `previousMonth decrements month`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        val expectedMonth = YearMonth.now().minusMonths(1)
        vm.previousMonth()
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(expectedMonth, state.currentMonth)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `nextMonth increments month`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        val expectedMonth = YearMonth.now().plusMonths(1)
        vm.nextMonth()
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(expectedMonth, state.currentMonth)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selectDate sets selected date and loads tasks`() = runTest {
        val date = LocalDate.now()
        val tasks = listOf(
            Task(id = 1L, groupId = 1L, name = "Task 1", scheduleType = ScheduleType.SPECIFIC)
        )
        val reviewTasks = listOf(
            ReviewTask(id = 1L, studySessionId = 1L, taskId = 1L, scheduledDate = date, reviewNumber = 1)
        )
        coEvery { taskRepository.getTasksForDate(date) } returns tasks
        every { reviewTaskRepository.getAllTasksForDate(date) } returns flowOf(reviewTasks)

        val vm = createViewModel()
        advanceUntilIdle()

        vm.selectDate(date)
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(date, state.selectedDate)
            assertEquals(1, state.selectedDateTasks.size)
            assertEquals(1, state.selectedDateReviewTasks.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearSelection resets selection`() = runTest {
        val date = LocalDate.now()
        coEvery { taskRepository.getTasksForDate(date) } returns emptyList()
        every { reviewTaskRepository.getAllTasksForDate(date) } returns flowOf(emptyList())

        val vm = createViewModel()
        advanceUntilIdle()

        vm.selectDate(date)
        advanceUntilIdle()
        vm.clearSelection()
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertNull(state.selectedDate)
            assertTrue(state.selectedDateTasks.isEmpty())
            assertTrue(state.selectedDateReviewTasks.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleReviewTaskComplete marks task as completed`() = runTest {
        coEvery { reviewTaskRepository.markAsCompleted(any()) } returns Unit

        val vm = createViewModel()
        advanceUntilIdle()

        vm.toggleReviewTaskComplete(1L, true)
        advanceUntilIdle()

        coVerify { reviewTaskRepository.markAsCompleted(1L) }
    }

    @Test
    fun `toggleReviewTaskComplete marks task as incomplete`() = runTest {
        coEvery { reviewTaskRepository.markAsIncomplete(any()) } returns Unit

        val vm = createViewModel()
        advanceUntilIdle()

        vm.toggleReviewTaskComplete(1L, false)
        advanceUntilIdle()

        coVerify { reviewTaskRepository.markAsIncomplete(1L) }
    }

    @Test
    fun `isPremium reflects subscription status`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.isPremium.test {
            assertFalse(awaitItem())

            subscriptionStatusFlow.value = SubscriptionStatus(
                type = SubscriptionType.YEARLY,
                expiresAt = LocalDateTime.now().plusYears(1)
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
