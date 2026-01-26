package com.iterio.app.ui.screens.home

import app.cash.turbine.test
import com.iterio.app.domain.model.ReviewTask
import com.iterio.app.domain.model.ScheduleType
import com.iterio.app.domain.model.Task
import com.iterio.app.domain.repository.DailyStatsRepository
import com.iterio.app.domain.repository.DayStats
import com.iterio.app.domain.repository.ReviewTaskRepository
import com.iterio.app.domain.repository.StudySessionRepository
import com.iterio.app.domain.repository.TaskRepository
import com.iterio.app.domain.usecase.GetTodayTasksUseCase
import com.iterio.app.domain.usecase.TodayTasksResult
import com.iterio.app.testutil.CoroutineTestRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

/**
 * HomeViewModel のユニットテスト
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var taskRepository: TaskRepository
    private lateinit var studySessionRepository: StudySessionRepository
    private lateinit var dailyStatsRepository: DailyStatsRepository
    private lateinit var reviewTaskRepository: ReviewTaskRepository
    private lateinit var getTodayTasksUseCase: GetTodayTasksUseCase
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        taskRepository = mockk()
        studySessionRepository = mockk()
        dailyStatsRepository = mockk()
        reviewTaskRepository = mockk()
        getTodayTasksUseCase = mockk()

        // Default mocks
        val today = LocalDate.now()
        coEvery { studySessionRepository.getTotalMinutesForDay(any()) } returns 0
        coEvery { studySessionRepository.getTotalCyclesForDay(any()) } returns 0
        coEvery { dailyStatsRepository.getCurrentStreak() } returns 0
        coEvery { dailyStatsRepository.getWeeklyData(any()) } returns emptyList()
        every { taskRepository.getUpcomingDeadlineTasks(any(), any()) } returns flowOf(emptyList())
        every { getTodayTasksUseCase(any()) } returns flowOf(
            TodayTasksResult(emptyList(), emptyList())
        )
    }

    private fun createViewModel() = HomeViewModel(
        taskRepository = taskRepository,
        studySessionRepository = studySessionRepository,
        dailyStatsRepository = dailyStatsRepository,
        reviewTaskRepository = reviewTaskRepository,
        getTodayTasksUseCase = getTodayTasksUseCase
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
    fun `loads today minutes correctly`() = runTest {
        coEvery { studySessionRepository.getTotalMinutesForDay(any()) } returns 120

        val vm = createViewModel()
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(120, state.todayMinutes)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loads today cycles correctly`() = runTest {
        coEvery { studySessionRepository.getTotalCyclesForDay(any()) } returns 4

        val vm = createViewModel()
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(4, state.todayCycles)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loads current streak correctly`() = runTest {
        coEvery { dailyStatsRepository.getCurrentStreak() } returns 7

        val vm = createViewModel()
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(7, state.currentStreak)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loads today scheduled tasks`() = runTest {
        val tasks = listOf(
            Task(id = 1L, groupId = 1L, name = "Math Study", scheduleType = ScheduleType.REPEAT),
            Task(id = 2L, groupId = 1L, name = "English", scheduleType = ScheduleType.SPECIFIC)
        )
        every { getTodayTasksUseCase(any()) } returns flowOf(
            TodayTasksResult(tasks, emptyList())
        )

        val vm = createViewModel()
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.todayScheduledTasks.size)
            assertFalse(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loads today review tasks`() = runTest {
        val reviewTasks = listOf(
            ReviewTask(id = 1L, studySessionId = 1L, taskId = 1L, scheduledDate = LocalDate.now(), reviewNumber = 1),
            ReviewTask(id = 2L, studySessionId = 2L, taskId = 2L, scheduledDate = LocalDate.now(), reviewNumber = 2)
        )
        every { getTodayTasksUseCase(any()) } returns flowOf(
            TodayTasksResult(emptyList(), reviewTasks)
        )

        val vm = createViewModel()
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.todayReviewTasks.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loads weekly data`() = runTest {
        val weeklyData = listOf(
            DayStats("月", LocalDate.now(), 60),
            DayStats("火", LocalDate.now().plusDays(1), 90)
        )
        coEvery { dailyStatsRepository.getWeeklyData(any()) } returns weeklyData

        val vm = createViewModel()
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.weeklyData.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loads upcoming deadline tasks`() = runTest {
        val tasks = listOf(
            Task(id = 1L, groupId = 1L, name = "Report", scheduleType = ScheduleType.DEADLINE, deadlineDate = LocalDate.now().plusDays(3))
        )
        every { taskRepository.getUpcomingDeadlineTasks(any(), any()) } returns flowOf(tasks)

        val vm = createViewModel()
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.upcomingDeadlineTasks.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadHomeData refreshes all data`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        coEvery { studySessionRepository.getTotalMinutesForDay(any()) } returns 180
        vm.loadHomeData()
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(180, state.todayMinutes)
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
}
