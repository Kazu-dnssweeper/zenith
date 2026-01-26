package com.iterio.app.data.repository

import app.cash.turbine.test
import com.iterio.app.data.local.dao.DateReviewTaskCount
import com.iterio.app.data.local.dao.ReviewTaskDao
import com.iterio.app.data.local.entity.ReviewTaskEntity
import com.iterio.app.data.local.entity.ReviewTaskWithDetails
import com.iterio.app.data.mapper.ReviewTaskMapper
import com.iterio.app.domain.common.Result
import com.iterio.app.domain.model.ReviewTask
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * ReviewTaskRepositoryImpl のユニットテスト
 */
class ReviewTaskRepositoryImplTest {

    private lateinit var reviewTaskDao: ReviewTaskDao
    private lateinit var mapper: ReviewTaskMapper
    private lateinit var repository: ReviewTaskRepositoryImpl

    @Before
    fun setup() {
        reviewTaskDao = mockk()
        mapper = ReviewTaskMapper()
        repository = ReviewTaskRepositoryImpl(reviewTaskDao, mapper)
    }

    @Test
    fun `insert returns id from dao`() = runTest {
        val task = createReviewTask(id = 0)
        coEvery { reviewTaskDao.insert(any()) } returns 42L

        val result = repository.insert(task)

        assertTrue(result is Result.Success)
        assertEquals(42L, (result as Result.Success).value)
        coVerify { reviewTaskDao.insert(any()) }
    }

    @Test
    fun `insertAll calls dao`() = runTest {
        val tasks = listOf(
            createReviewTask(id = 0, reviewNumber = 1),
            createReviewTask(id = 0, reviewNumber = 2)
        )
        coEvery { reviewTaskDao.insertAll(any()) } returns Unit

        val result = repository.insertAll(tasks)

        assertTrue(result is Result.Success)
        coVerify { reviewTaskDao.insertAll(match { it.size == 2 }) }
    }

    @Test
    fun `update calls dao`() = runTest {
        val task = createReviewTask(id = 1)
        coEvery { reviewTaskDao.update(any()) } returns Unit

        val result = repository.update(task)

        assertTrue(result is Result.Success)
        coVerify { reviewTaskDao.update(any()) }
    }

    @Test
    fun `delete calls dao`() = runTest {
        val task = createReviewTask(id = 1)
        coEvery { reviewTaskDao.delete(any()) } returns Unit

        val result = repository.delete(task)

        assertTrue(result is Result.Success)
        coVerify { reviewTaskDao.delete(any()) }
    }

    @Test
    fun `getById returns task when exists`() = runTest {
        val entity = createEntity(id = 1)
        coEvery { reviewTaskDao.getById(1) } returns entity

        val result = repository.getById(1)

        assertTrue(result is Result.Success)
        val task = (result as Result.Success).value
        assertNotNull(task)
        assertEquals(1L, task?.id)
    }

    @Test
    fun `getById returns null when not exists`() = runTest {
        coEvery { reviewTaskDao.getById(999) } returns null

        val result = repository.getById(999)

        assertTrue(result is Result.Success)
        assertNull((result as Result.Success).value)
    }

    @Test
    fun `getTasksForSession returns flow of tasks`() = runTest {
        val entities = listOf(
            createEntity(id = 1, reviewNumber = 1),
            createEntity(id = 2, reviewNumber = 2)
        )
        every { reviewTaskDao.getTasksForSession(100) } returns flowOf(entities)

        repository.getTasksForSession(100).test {
            val tasks = awaitItem()
            assertEquals(2, tasks.size)
            assertEquals(1, tasks[0].reviewNumber)
            assertEquals(2, tasks[1].reviewNumber)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getTasksForTask returns flow of tasks`() = runTest {
        val entities = listOf(createEntity(id = 1))
        every { reviewTaskDao.getTasksForTask(50) } returns flowOf(entities)

        repository.getTasksForTask(50).test {
            val tasks = awaitItem()
            assertEquals(1, tasks.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getPendingTasksForDate returns tasks with details`() = runTest {
        val date = LocalDate.now()
        val detailsList = listOf(
            createDetailsEntity(id = 1, taskName = "Math", groupName = "Science")
        )
        every { reviewTaskDao.getPendingTasksForDateWithDetails(date) } returns flowOf(detailsList)

        repository.getPendingTasksForDate(date).test {
            val tasks = awaitItem()
            assertEquals(1, tasks.size)
            assertEquals("Math", tasks[0].taskName)
            assertEquals("Science", tasks[0].groupName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAllTasksForDate returns tasks with details`() = runTest {
        val date = LocalDate.now()
        val detailsList = listOf(
            createDetailsEntity(id = 1),
            createDetailsEntity(id = 2)
        )
        every { reviewTaskDao.getAllTasksForDateWithDetails(date) } returns flowOf(detailsList)

        repository.getAllTasksForDate(date).test {
            val tasks = awaitItem()
            assertEquals(2, tasks.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getOverdueAndTodayTasks returns tasks`() = runTest {
        val date = LocalDate.now()
        val detailsList = listOf(createDetailsEntity(id = 1))
        every { reviewTaskDao.getOverdueAndTodayTasksWithDetails(date) } returns flowOf(detailsList)

        repository.getOverdueAndTodayTasks(date).test {
            val tasks = awaitItem()
            assertEquals(1, tasks.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getPendingTaskCountForDate returns count`() = runTest {
        val date = LocalDate.now()
        coEvery { reviewTaskDao.getPendingTaskCountForDate(date) } returns 5

        val result = repository.getPendingTaskCountForDate(date)

        assertTrue(result is Result.Success)
        assertEquals(5, (result as Result.Success).value)
    }

    @Test
    fun `markAsCompleted calls dao`() = runTest {
        coEvery { reviewTaskDao.markAsCompleted(1, any()) } returns Unit

        val result = repository.markAsCompleted(1)

        assertTrue(result is Result.Success)
        coVerify { reviewTaskDao.markAsCompleted(1, any()) }
    }

    @Test
    fun `markAsIncomplete calls dao`() = runTest {
        coEvery { reviewTaskDao.markAsIncomplete(1) } returns Unit

        val result = repository.markAsIncomplete(1)

        assertTrue(result is Result.Success)
        coVerify { reviewTaskDao.markAsIncomplete(1) }
    }

    @Test
    fun `reschedule calls dao`() = runTest {
        val newDate = LocalDate.now().plusDays(3)
        coEvery { reviewTaskDao.reschedule(1, newDate) } returns Unit

        val result = repository.reschedule(1, newDate)

        assertTrue(result is Result.Success)
        coVerify { reviewTaskDao.reschedule(1, newDate) }
    }

    @Test
    fun `deleteTasksForSession calls dao`() = runTest {
        coEvery { reviewTaskDao.deleteTasksForSession(100) } returns Unit

        val result = repository.deleteTasksForSession(100)

        assertTrue(result is Result.Success)
        coVerify { reviewTaskDao.deleteTasksForSession(100) }
    }

    @Test
    fun `deleteTasksForTask calls dao`() = runTest {
        coEvery { reviewTaskDao.deleteTasksForTask(50) } returns Unit

        val result = repository.deleteTasksForTask(50)

        assertTrue(result is Result.Success)
        coVerify { reviewTaskDao.deleteTasksForTask(50) }
    }

    @Test
    fun `getTaskCountByDateRange returns map`() = runTest {
        val startDate = LocalDate.now()
        val endDate = startDate.plusDays(7)
        val counts = listOf(
            DateReviewTaskCount(startDate, 2),
            DateReviewTaskCount(startDate.plusDays(1), 3)
        )
        coEvery { reviewTaskDao.getTaskCountByDateRange(startDate, endDate) } returns counts

        val result = repository.getTaskCountByDateRange(startDate, endDate)

        assertTrue(result is Result.Success)
        val map = (result as Result.Success).value
        assertEquals(2, map.size)
        assertEquals(2, map[startDate])
        assertEquals(3, map[startDate.plusDays(1)])
    }

    @Test
    fun `getTotalCount returns count`() = runTest {
        coEvery { reviewTaskDao.getTotalCount() } returns 10

        val result = repository.getTotalCount()

        assertTrue(result is Result.Success)
        assertEquals(10, (result as Result.Success).value)
    }

    @Test
    fun `getIncompleteCount returns count`() = runTest {
        coEvery { reviewTaskDao.getIncompleteCount() } returns 5

        val result = repository.getIncompleteCount()

        assertTrue(result is Result.Success)
        assertEquals(5, (result as Result.Success).value)
    }

    @Test
    fun `deleteAll calls dao`() = runTest {
        coEvery { reviewTaskDao.deleteAll() } returns Unit

        val result = repository.deleteAll()

        assertTrue(result is Result.Success)
        coVerify { reviewTaskDao.deleteAll() }
    }

    @Test
    fun `getAllWithDetails returns flow of tasks`() = runTest {
        val detailsList = listOf(createDetailsEntity(id = 1))
        every { reviewTaskDao.getAllWithDetails() } returns flowOf(detailsList)

        repository.getAllWithDetails().test {
            val tasks = awaitItem()
            assertEquals(1, tasks.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getTaskCountByDateRange returns empty map when no data`() = runTest {
        val startDate = LocalDate.now()
        val endDate = startDate.plusDays(7)
        coEvery { reviewTaskDao.getTaskCountByDateRange(startDate, endDate) } returns emptyList()

        val result = repository.getTaskCountByDateRange(startDate, endDate)

        assertTrue(result is Result.Success)
        assertTrue((result as Result.Success).value.isEmpty())
    }

    // ==================== Helpers ====================

    private fun createEntity(
        id: Long = 0,
        studySessionId: Long = 100,
        taskId: Long = 50,
        scheduledDate: LocalDate = LocalDate.now(),
        reviewNumber: Int = 1,
        isCompleted: Boolean = false
    ) = ReviewTaskEntity(
        id = id,
        studySessionId = studySessionId,
        taskId = taskId,
        scheduledDate = scheduledDate,
        reviewNumber = reviewNumber,
        isCompleted = isCompleted,
        completedAt = null,
        createdAt = LocalDateTime.now()
    )

    private fun createDetailsEntity(
        id: Long = 0,
        studySessionId: Long = 100,
        taskId: Long = 50,
        scheduledDate: LocalDate = LocalDate.now(),
        reviewNumber: Int = 1,
        isCompleted: Boolean = false,
        taskName: String = "Test Task",
        groupName: String = "Test Group"
    ) = ReviewTaskWithDetails(
        id = id,
        studySessionId = studySessionId,
        taskId = taskId,
        scheduledDate = scheduledDate,
        reviewNumber = reviewNumber,
        isCompleted = isCompleted,
        completedAt = null,
        createdAt = LocalDateTime.now(),
        taskName = taskName,
        groupName = groupName
    )

    private fun createReviewTask(
        id: Long = 0,
        studySessionId: Long = 100,
        taskId: Long = 50,
        scheduledDate: LocalDate = LocalDate.now(),
        reviewNumber: Int = 1
    ) = ReviewTask(
        id = id,
        studySessionId = studySessionId,
        taskId = taskId,
        scheduledDate = scheduledDate,
        reviewNumber = reviewNumber,
        createdAt = LocalDateTime.now()
    )
}
