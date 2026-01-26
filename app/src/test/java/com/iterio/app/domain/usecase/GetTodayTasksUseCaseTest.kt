package com.iterio.app.domain.usecase

import com.iterio.app.domain.model.ReviewTask
import com.iterio.app.domain.model.ScheduleType
import com.iterio.app.domain.model.Task
import com.iterio.app.domain.repository.ReviewTaskRepository
import com.iterio.app.domain.repository.TaskRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

/**
 * GetTodayTasksUseCase のユニットテスト
 */
class GetTodayTasksUseCaseTest {

    private lateinit var taskRepository: TaskRepository
    private lateinit var reviewTaskRepository: ReviewTaskRepository
    private lateinit var useCase: GetTodayTasksUseCase

    @Before
    fun setup() {
        taskRepository = mockk()
        reviewTaskRepository = mockk()
        useCase = GetTodayTasksUseCase(taskRepository, reviewTaskRepository)
    }

    @Test
    fun `returns scheduled tasks for today`() = runTest {
        val today = LocalDate.now()
        val tasks = listOf(
            Task(id = 1L, groupId = 1L, name = "Task 1", scheduleType = ScheduleType.REPEAT),
            Task(id = 2L, groupId = 1L, name = "Task 2", scheduleType = ScheduleType.SPECIFIC)
        )
        every { taskRepository.getTodayScheduledTasks(today) } returns flowOf(tasks)
        every { reviewTaskRepository.getOverdueAndTodayTasks(today) } returns flowOf(emptyList())

        val result = useCase(today).first()

        assertEquals(2, result.scheduledTasks.size)
    }

    @Test
    fun `returns pending review tasks`() = runTest {
        val today = LocalDate.now()
        val reviewTasks = listOf(
            ReviewTask(id = 1L, studySessionId = 1L, taskId = 1L, scheduledDate = today, reviewNumber = 1),
            ReviewTask(id = 2L, studySessionId = 1L, taskId = 1L, scheduledDate = today.minusDays(1), reviewNumber = 2)
        )
        every { taskRepository.getTodayScheduledTasks(today) } returns flowOf(emptyList())
        every { reviewTaskRepository.getOverdueAndTodayTasks(today) } returns flowOf(reviewTasks)

        val result = useCase(today).first()

        assertEquals(2, result.reviewTasks.size)
    }

    @Test
    fun `returns both tasks and reviews when present`() = runTest {
        val today = LocalDate.now()
        val tasks = listOf(
            Task(id = 1L, groupId = 1L, name = "Study Math")
        )
        val reviews = listOf(
            ReviewTask(id = 1L, studySessionId = 1L, taskId = 1L, scheduledDate = today, reviewNumber = 1)
        )
        every { taskRepository.getTodayScheduledTasks(today) } returns flowOf(tasks)
        every { reviewTaskRepository.getOverdueAndTodayTasks(today) } returns flowOf(reviews)

        val result = useCase(today).first()

        assertEquals(1, result.scheduledTasks.size)
        assertEquals(1, result.reviewTasks.size)
    }

    @Test
    fun `returns empty lists when no tasks`() = runTest {
        val today = LocalDate.now()
        every { taskRepository.getTodayScheduledTasks(today) } returns flowOf(emptyList())
        every { reviewTaskRepository.getOverdueAndTodayTasks(today) } returns flowOf(emptyList())

        val result = useCase(today).first()

        assertTrue(result.scheduledTasks.isEmpty())
        assertTrue(result.reviewTasks.isEmpty())
    }

    @Test
    fun `calculates total task count correctly`() = runTest {
        val today = LocalDate.now()
        val tasks = listOf(
            Task(id = 1L, groupId = 1L, name = "Task 1"),
            Task(id = 2L, groupId = 1L, name = "Task 2")
        )
        val reviews = listOf(
            ReviewTask(id = 1L, studySessionId = 1L, taskId = 1L, scheduledDate = today, reviewNumber = 1),
            ReviewTask(id = 2L, studySessionId = 1L, taskId = 2L, scheduledDate = today, reviewNumber = 1),
            ReviewTask(id = 3L, studySessionId = 1L, taskId = 3L, scheduledDate = today, reviewNumber = 1)
        )
        every { taskRepository.getTodayScheduledTasks(today) } returns flowOf(tasks)
        every { reviewTaskRepository.getOverdueAndTodayTasks(today) } returns flowOf(reviews)

        val result = useCase(today).first()

        assertEquals(5, result.totalCount)
    }

    @Test
    fun `uses today when no date provided`() = runTest {
        val today = LocalDate.now()
        every { taskRepository.getTodayScheduledTasks(today) } returns flowOf(emptyList())
        every { reviewTaskRepository.getOverdueAndTodayTasks(today) } returns flowOf(emptyList())

        useCase().first()

        // Should not throw - uses default today
    }
}
