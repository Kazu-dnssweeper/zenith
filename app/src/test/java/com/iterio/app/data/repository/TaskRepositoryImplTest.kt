package com.iterio.app.data.repository

import app.cash.turbine.test
import com.iterio.app.data.local.dao.DateTaskCount
import com.iterio.app.data.local.dao.TaskDao
import com.iterio.app.data.local.entity.TaskEntity
import com.iterio.app.data.mapper.TaskMapper
import com.iterio.app.domain.common.Result
import com.iterio.app.domain.model.ScheduleType
import com.iterio.app.domain.model.Task
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * TaskRepositoryImpl のユニットテスト
 */
class TaskRepositoryImplTest {

    private lateinit var taskDao: TaskDao
    private lateinit var mapper: TaskMapper
    private lateinit var repository: TaskRepositoryImpl

    @Before
    fun setup() {
        taskDao = mockk()
        mapper = TaskMapper()
        repository = TaskRepositoryImpl(taskDao, mapper)
    }

    @Test
    fun `getTasksByGroup returns tasks filtered by group`() = runTest {
        val entities = listOf(
            createTaskEntity(id = 1, groupId = 1, name = "Task 1"),
            createTaskEntity(id = 2, groupId = 1, name = "Task 2")
        )
        every { taskDao.getTasksByGroup(1) } returns flowOf(entities)

        repository.getTasksByGroup(1).test {
            val tasks = awaitItem()
            assertEquals(2, tasks.size)
            assertEquals("Task 1", tasks[0].name)
            assertEquals("Task 2", tasks[1].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAllActiveTasks returns all active tasks`() = runTest {
        val entities = listOf(
            createTaskEntity(id = 1, name = "Active Task 1"),
            createTaskEntity(id = 2, name = "Active Task 2")
        )
        every { taskDao.getAllActiveTasks() } returns flowOf(entities)

        repository.getAllActiveTasks().test {
            val tasks = awaitItem()
            assertEquals(2, tasks.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getTaskById returns task when exists`() = runTest {
        val entity = createTaskEntity(id = 1, name = "Test Task")
        coEvery { taskDao.getTaskById(1) } returns entity

        val result = repository.getTaskById(1)

        assertTrue(result.isSuccess)
        val task = (result as Result.Success).value
        assertNotNull(task)
        assertEquals("Test Task", task?.name)
    }

    @Test
    fun `getTaskById returns null when not exists`() = runTest {
        coEvery { taskDao.getTaskById(999) } returns null

        val result = repository.getTaskById(999)

        assertTrue(result.isSuccess)
        val task = (result as Result.Success).value
        assertNull(task)
    }

    @Test
    fun `insertTask calls dao and returns id`() = runTest {
        val task = createTask(id = 0, name = "New Task")
        coEvery { taskDao.insertTask(any()) } returns 42L

        val result = repository.insertTask(task)

        assertTrue(result.isSuccess)
        assertEquals(42L, (result as Result.Success).value)
        coVerify { taskDao.insertTask(any()) }
    }

    @Test
    fun `updateTask calls dao with updated entity`() = runTest {
        val task = createTask(id = 1, name = "Updated Task")
        coEvery { taskDao.updateTask(any()) } returns Unit

        repository.updateTask(task)

        coVerify { taskDao.updateTask(any()) }
    }

    @Test
    fun `deleteTask calls dao`() = runTest {
        val task = createTask(id = 1, name = "To Delete")
        coEvery { taskDao.deleteTask(any()) } returns Unit

        repository.deleteTask(task)

        coVerify { taskDao.deleteTask(any()) }
    }

    @Test
    fun `deactivateTask calls dao`() = runTest {
        coEvery { taskDao.deactivateTask(1) } returns Unit

        repository.deactivateTask(1)

        coVerify { taskDao.deactivateTask(1) }
    }

    @Test
    fun `updateProgress calls dao with correct parameters`() = runTest {
        val updatedAtSlot = slot<LocalDateTime>()
        coEvery { taskDao.updateProgress(1, "Note", 50, "Goal", capture(updatedAtSlot)) } returns Unit

        repository.updateProgress(1, "Note", 50, "Goal")

        coVerify { taskDao.updateProgress(1, "Note", 50, "Goal", any()) }
        assertNotNull(updatedAtSlot.captured)
    }

    @Test
    fun `getTodayScheduledTasks returns tasks for today`() = runTest {
        val today = LocalDate.now()
        val entities = listOf(
            createTaskEntity(id = 1, name = "Today Task", scheduleType = "repeat")
        )
        every { taskDao.getTodayScheduledTasks(any(), any()) } returns flowOf(entities)

        repository.getTodayScheduledTasks(today).test {
            val tasks = awaitItem()
            assertEquals(1, tasks.size)
            assertEquals("Today Task", tasks[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateLastStudiedAt calls dao`() = runTest {
        val studiedAt = LocalDateTime.now()
        coEvery { taskDao.updateLastStudiedAt(1, studiedAt) } returns Unit

        repository.updateLastStudiedAt(1, studiedAt)

        coVerify { taskDao.updateLastStudiedAt(1, studiedAt) }
    }

    @Test
    fun `getUpcomingDeadlineTasks returns deadline tasks`() = runTest {
        val startDate = LocalDate.now()
        val endDate = startDate.plusDays(7)
        val entities = listOf(
            createTaskEntity(id = 1, name = "Deadline Task", scheduleType = "deadline", deadlineDate = endDate)
        )
        every { taskDao.getUpcomingDeadlineTasks(any(), any()) } returns flowOf(entities)

        repository.getUpcomingDeadlineTasks(startDate, endDate).test {
            val tasks = awaitItem()
            assertEquals(1, tasks.size)
            assertEquals("Deadline Task", tasks[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getTasksForDate returns tasks for specific date`() = runTest {
        val date = LocalDate.now()
        val entities = listOf(
            createTaskEntity(id = 1, name = "Specific Date Task", scheduleType = "specific")
        )
        coEvery { taskDao.getTasksForDate(any(), any()) } returns entities

        val result = repository.getTasksForDate(date)

        assertTrue(result.isSuccess)
        val tasks = (result as Result.Success).value
        assertEquals(1, tasks.size)
        assertEquals("Specific Date Task", tasks[0].name)
    }

    @Test
    fun `getTaskCountByDateRange combines deadline and repeat tasks`() = runTest {
        val startDate = LocalDate.now()
        val endDate = startDate.plusDays(7)
        val dateCounts = listOf(
            DateTaskCount(startDate.toString(), 2),
            DateTaskCount(startDate.plusDays(1).toString(), 1)
        )
        val repeatTasks = listOf(
            createTaskEntity(id = 1, scheduleType = "repeat", repeatDays = "1,3,5")
        )
        coEvery { taskDao.getTaskCountByDateRange(any(), any()) } returns dateCounts
        coEvery { taskDao.getRepeatTasks() } returns repeatTasks

        val result = repository.getTaskCountByDateRange(startDate, endDate)

        assertTrue(result.isSuccess)
        val counts = (result as Result.Success).value
        // Should have counts for various dates
        assertTrue(counts.isNotEmpty())
    }

    @Test
    fun `getTaskCountByDateRange handles empty results`() = runTest {
        val startDate = LocalDate.now()
        val endDate = startDate.plusDays(7)
        coEvery { taskDao.getTaskCountByDateRange(any(), any()) } returns emptyList()
        coEvery { taskDao.getRepeatTasks() } returns emptyList()

        val result = repository.getTaskCountByDateRange(startDate, endDate)

        assertTrue(result.isSuccess)
        val counts = (result as Result.Success).value
        assertTrue(counts.isEmpty())
    }

    // ==================== Helpers ====================

    private fun createTaskEntity(
        id: Long = 0,
        groupId: Long = 1,
        name: String = "Test Task",
        scheduleType: String? = null,
        repeatDays: String? = null,
        deadlineDate: LocalDate? = null,
        specificDate: LocalDate? = null,
        isActive: Boolean = true
    ) = TaskEntity(
        id = id,
        groupId = groupId,
        name = name,
        scheduleType = scheduleType,
        repeatDays = repeatDays,
        deadlineDate = deadlineDate,
        specificDate = specificDate,
        isActive = isActive,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )

    private fun createTask(
        id: Long = 0,
        groupId: Long = 1,
        name: String = "Test Task",
        scheduleType: ScheduleType = ScheduleType.NONE
    ) = Task(
        id = id,
        groupId = groupId,
        name = name,
        scheduleType = scheduleType,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )
}
