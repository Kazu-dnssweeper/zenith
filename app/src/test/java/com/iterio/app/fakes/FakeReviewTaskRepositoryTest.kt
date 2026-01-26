package com.iterio.app.fakes

import com.iterio.app.domain.common.Result
import com.iterio.app.domain.model.ReviewTask
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * FakeReviewTaskRepository のテスト
 */
class FakeReviewTaskRepositoryTest {

    private lateinit var repository: FakeReviewTaskRepository

    @Before
    fun setup() {
        repository = FakeReviewTaskRepository()
    }

    // Insert Tests

    @Test
    fun `insert returns incremented id`() = runTest {
        val task1 = createReviewTask(studySessionId = 1L, taskId = 1L)
        val task2 = createReviewTask(studySessionId = 2L, taskId = 2L)

        val id1 = (repository.insert(task1) as Result.Success).value
        val id2 = (repository.insert(task2) as Result.Success).value

        assertEquals(1L, id1)
        assertEquals(2L, id2)
    }

    @Test
    fun `insertAll adds all tasks`() = runTest {
        val tasks = listOf(
            createReviewTask(studySessionId = 1L, taskId = 1L),
            createReviewTask(studySessionId = 1L, taskId = 1L),
            createReviewTask(studySessionId = 1L, taskId = 1L)
        )

        repository.insertAll(tasks)
        val allTasks = repository.getTasksForSession(1L).first()

        assertEquals(3, allTasks.size)
    }

    // Get Tests

    @Test
    fun `getById returns null for non-existent id`() = runTest {
        val result = (repository.getById(999L) as Result.Success).value
        assertNull(result)
    }

    @Test
    fun `getById returns task when exists`() = runTest {
        val id = (repository.insert(createReviewTask(studySessionId = 1L, taskId = 1L)) as Result.Success).value

        val task = (repository.getById(id) as Result.Success).value

        assertNotNull(task)
        assertEquals(1L, task?.taskId)
    }

    @Test
    fun `getTasksForSession returns tasks for specific session`() = runTest {
        repository.insert(createReviewTask(studySessionId = 1L, taskId = 1L))
        repository.insert(createReviewTask(studySessionId = 1L, taskId = 2L))
        repository.insert(createReviewTask(studySessionId = 2L, taskId = 3L))

        val tasks = repository.getTasksForSession(1L).first()

        assertEquals(2, tasks.size)
    }

    @Test
    fun `getTasksForTask returns tasks for specific task`() = runTest {
        repository.insert(createReviewTask(studySessionId = 1L, taskId = 1L))
        repository.insert(createReviewTask(studySessionId = 2L, taskId = 1L))
        repository.insert(createReviewTask(studySessionId = 3L, taskId = 2L))

        val tasks = repository.getTasksForTask(1L).first()

        assertEquals(2, tasks.size)
    }

    @Test
    fun `getPendingTasksForDate returns pending tasks for date`() = runTest {
        val today = LocalDate.now()

        repository.insert(createReviewTask(studySessionId = 1L, taskId = 1L, scheduledDate = today))
        repository.insert(createReviewTask(studySessionId = 2L, taskId = 2L, scheduledDate = today))
        repository.insert(createReviewTask(studySessionId = 3L, taskId = 3L, scheduledDate = today.plusDays(1)))

        val tasks = repository.getPendingTasksForDate(today).first()

        assertEquals(2, tasks.size)
    }

    @Test
    fun `getAllTasksForDate returns all tasks including completed`() = runTest {
        val today = LocalDate.now()

        val id1 = (repository.insert(createReviewTask(studySessionId = 1L, taskId = 1L, scheduledDate = today)) as Result.Success).value
        repository.insert(createReviewTask(studySessionId = 2L, taskId = 2L, scheduledDate = today))

        repository.markAsCompleted(id1)

        val tasks = repository.getAllTasksForDate(today).first()

        assertEquals(2, tasks.size)
    }

    @Test
    fun `getOverdueAndTodayTasks returns overdue and today tasks`() = runTest {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val tomorrow = today.plusDays(1)

        repository.insert(createReviewTask(studySessionId = 1L, taskId = 1L, scheduledDate = yesterday))
        repository.insert(createReviewTask(studySessionId = 2L, taskId = 2L, scheduledDate = today))
        repository.insert(createReviewTask(studySessionId = 3L, taskId = 3L, scheduledDate = tomorrow))

        val tasks = repository.getOverdueAndTodayTasks(today).first()

        assertEquals(2, tasks.size)
    }

    // Update Tests

    @Test
    fun `update updates existing task`() = runTest {
        val id = (repository.insert(createReviewTask(studySessionId = 1L, taskId = 1L)) as Result.Success).value

        val task = (repository.getById(id) as Result.Success).value!!
        repository.update(task.copy(taskName = "Updated"))

        val updated = (repository.getById(id) as Result.Success).value
        assertEquals("Updated", updated?.taskName)
    }

    @Test
    fun `markAsCompleted marks task as completed`() = runTest {
        val id = (repository.insert(createReviewTask(studySessionId = 1L, taskId = 1L)) as Result.Success).value

        repository.markAsCompleted(id)

        val task = (repository.getById(id) as Result.Success).value
        assertTrue(task?.isCompleted ?: false)
        assertNotNull(task?.completedAt)
    }

    @Test
    fun `markAsIncomplete marks task as incomplete`() = runTest {
        val id = (repository.insert(createReviewTask(studySessionId = 1L, taskId = 1L)) as Result.Success).value
        repository.markAsCompleted(id)

        repository.markAsIncomplete(id)

        val task = (repository.getById(id) as Result.Success).value
        assertFalse(task?.isCompleted ?: true)
        assertNull(task?.completedAt)
    }

    @Test
    fun `reschedule changes scheduled date`() = runTest {
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)
        val id = (repository.insert(createReviewTask(studySessionId = 1L, taskId = 1L, scheduledDate = today)) as Result.Success).value

        repository.reschedule(id, tomorrow)

        val task = (repository.getById(id) as Result.Success).value
        assertEquals(tomorrow, task?.scheduledDate)
    }

    // Delete Tests

    @Test
    fun `delete removes task`() = runTest {
        val id = (repository.insert(createReviewTask(studySessionId = 1L, taskId = 1L)) as Result.Success).value

        val task = (repository.getById(id) as Result.Success).value!!
        repository.delete(task)

        assertNull((repository.getById(id) as Result.Success).value)
    }

    @Test
    fun `deleteTasksForSession removes all tasks for session`() = runTest {
        repository.insert(createReviewTask(studySessionId = 1L, taskId = 1L))
        repository.insert(createReviewTask(studySessionId = 1L, taskId = 2L))
        repository.insert(createReviewTask(studySessionId = 2L, taskId = 3L))

        repository.deleteTasksForSession(1L)

        val session1Tasks = repository.getTasksForSession(1L).first()
        val session2Tasks = repository.getTasksForSession(2L).first()

        assertEquals(0, session1Tasks.size)
        assertEquals(1, session2Tasks.size)
    }

    @Test
    fun `deleteTasksForTask removes all tasks for task`() = runTest {
        repository.insert(createReviewTask(studySessionId = 1L, taskId = 1L))
        repository.insert(createReviewTask(studySessionId = 2L, taskId = 1L))
        repository.insert(createReviewTask(studySessionId = 3L, taskId = 2L))

        repository.deleteTasksForTask(1L)

        val task1Reviews = repository.getTasksForTask(1L).first()
        val task2Reviews = repository.getTasksForTask(2L).first()

        assertEquals(0, task1Reviews.size)
        assertEquals(1, task2Reviews.size)
    }

    @Test
    fun `getPendingTaskCountForDate returns correct count`() = runTest {
        val today = LocalDate.now()

        val id1 = (repository.insert(createReviewTask(studySessionId = 1L, taskId = 1L, scheduledDate = today)) as Result.Success).value
        repository.insert(createReviewTask(studySessionId = 2L, taskId = 2L, scheduledDate = today))
        repository.insert(createReviewTask(studySessionId = 3L, taskId = 3L, scheduledDate = today))

        repository.markAsCompleted(id1)

        val count = (repository.getPendingTaskCountForDate(today) as Result.Success).value

        assertEquals(2, count)
    }

    // Helper function
    private fun createReviewTask(
        studySessionId: Long,
        taskId: Long,
        scheduledDate: LocalDate = LocalDate.now(),
        reviewNumber: Int = 1
    ) = ReviewTask(
        studySessionId = studySessionId,
        taskId = taskId,
        scheduledDate = scheduledDate,
        reviewNumber = reviewNumber
    )
}
