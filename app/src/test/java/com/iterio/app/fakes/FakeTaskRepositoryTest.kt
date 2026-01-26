package com.iterio.app.fakes

import app.cash.turbine.test
import com.iterio.app.domain.model.ScheduleType
import com.iterio.app.testutil.CoroutineTestRule
import com.iterio.app.testutil.TestDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class FakeTaskRepositoryTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var repository: FakeTaskRepository

    @Before
    fun setup() {
        repository = FakeTaskRepository()
    }

    @Test
    fun `getAllActiveTasks returns empty flow initially`() = runTest {
        repository.getAllActiveTasks().test {
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `insertTask adds task and returns id`() = runTest {
        val task = TestDataFactory.createTask(name = "Study Math")

        val id = repository.insertTask(task)

        assertEquals(1L, id)
        val saved = repository.getTaskById(id)
        assertNotNull(saved)
        assertEquals("Study Math", saved?.name)
    }

    @Test
    fun `getTasksByGroup filters by groupId`() = runTest {
        repository.insertTask(TestDataFactory.createTask(name = "Task1", groupId = 1L))
        repository.insertTask(TestDataFactory.createTask(name = "Task2", groupId = 2L))
        repository.insertTask(TestDataFactory.createTask(name = "Task3", groupId = 1L))

        repository.getTasksByGroup(1L).test {
            val tasks = awaitItem()
            assertEquals(2, tasks.size)
            assertTrue(tasks.all { it.groupId == 1L })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAllActiveTasks excludes inactive tasks`() = runTest {
        repository.insertTask(TestDataFactory.createTask(name = "Active", isActive = true))
        repository.insertTask(TestDataFactory.createTask(name = "Inactive", isActive = false))

        repository.getAllActiveTasks().test {
            val tasks = awaitItem()
            assertEquals(1, tasks.size)
            assertEquals("Active", tasks[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateTask modifies existing task`() = runTest {
        val id = repository.insertTask(TestDataFactory.createTask(name = "Original"))
        val task = repository.getTaskById(id)!!

        repository.updateTask(task.copy(name = "Updated"))

        val updated = repository.getTaskById(id)
        assertEquals("Updated", updated?.name)
    }

    @Test
    fun `deleteTask removes task`() = runTest {
        val id = repository.insertTask(TestDataFactory.createTask(name = "ToDelete"))
        val task = repository.getTaskById(id)!!

        repository.deleteTask(task)

        assertNull(repository.getTaskById(id))
    }

    @Test
    fun `deactivateTask sets isActive to false`() = runTest {
        val id = repository.insertTask(TestDataFactory.createTask(name = "ToDeactivate", isActive = true))

        repository.deactivateTask(id)

        val task = repository.getTaskById(id)
        assertFalse(task!!.isActive)
    }

    @Test
    fun `updateProgress updates task progress fields`() = runTest {
        val id = repository.insertTask(TestDataFactory.createTask(name = "Task"))

        repository.updateProgress(id, note = "Good progress", percent = 50, goal = "Finish chapter")

        val task = repository.getTaskById(id)
        assertEquals("Good progress", task?.progressNote)
        assertEquals(50, task?.progressPercent)
        assertEquals("Finish chapter", task?.nextGoal)
    }

    @Test
    fun `getTodayScheduledTasks returns repeat tasks for today`() = runTest {
        val today = LocalDate.of(2026, 1, 23) // Friday = 5

        // Task repeats on Mon, Wed, Fri
        repository.insertTask(TestDataFactory.createRepeatTask(
            name = "Repeat MWF",
            repeatDays = setOf(1, 3, 5)
        ))
        // Task repeats on Tue, Thu
        repository.insertTask(TestDataFactory.createRepeatTask(
            name = "Repeat TuTh",
            repeatDays = setOf(2, 4)
        ))

        repository.getTodayScheduledTasks(today).test {
            val tasks = awaitItem()
            assertEquals(1, tasks.size)
            assertEquals("Repeat MWF", tasks[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getTodayScheduledTasks returns specific date tasks`() = runTest {
        val today = LocalDate.of(2026, 1, 23)

        repository.insertTask(TestDataFactory.createSpecificDateTask(
            name = "Today Task",
            specificDate = today
        ))
        repository.insertTask(TestDataFactory.createSpecificDateTask(
            name = "Tomorrow Task",
            specificDate = today.plusDays(1)
        ))

        repository.getTodayScheduledTasks(today).test {
            val tasks = awaitItem()
            assertEquals(1, tasks.size)
            assertEquals("Today Task", tasks[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateLastStudiedAt updates timestamp`() = runTest {
        val id = repository.insertTask(TestDataFactory.createTask(name = "Task"))
        val studiedAt = LocalDateTime.of(2026, 1, 23, 10, 30)

        repository.updateLastStudiedAt(id, studiedAt)

        val task = repository.getTaskById(id)
        assertEquals(studiedAt, task?.lastStudiedAt)
    }

    @Test
    fun `getUpcomingDeadlineTasks returns tasks within date range`() = runTest {
        val startDate = LocalDate.of(2026, 1, 20)
        val endDate = LocalDate.of(2026, 1, 25)

        repository.insertTask(TestDataFactory.createDeadlineTask(
            name = "In Range",
            deadlineDate = LocalDate.of(2026, 1, 23)
        ))
        repository.insertTask(TestDataFactory.createDeadlineTask(
            name = "Before Range",
            deadlineDate = LocalDate.of(2026, 1, 15)
        ))
        repository.insertTask(TestDataFactory.createDeadlineTask(
            name = "After Range",
            deadlineDate = LocalDate.of(2026, 1, 30)
        ))

        repository.getUpcomingDeadlineTasks(startDate, endDate).test {
            val tasks = awaitItem()
            assertEquals(1, tasks.size)
            assertEquals("In Range", tasks[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getTasksForDate returns tasks scheduled for that date`() = runTest {
        val date = LocalDate.of(2026, 1, 23) // Friday = 5

        repository.insertTask(TestDataFactory.createRepeatTask(repeatDays = setOf(5)))
        repository.insertTask(TestDataFactory.createSpecificDateTask(specificDate = date))
        repository.insertTask(TestDataFactory.createDeadlineTask(deadlineDate = date))
        repository.insertTask(TestDataFactory.createTask(scheduleType = ScheduleType.NONE))

        val tasks = repository.getTasksForDate(date)
        assertEquals(3, tasks.size)
    }

    @Test
    fun `getTaskCountByDateRange returns correct counts`() = runTest {
        // Jan 5, 2026 is Monday, Jan 9, 2026 is Friday
        val startDate = LocalDate.of(2026, 1, 5)  // Monday
        val endDate = LocalDate.of(2026, 1, 9)    // Friday

        // Repeats Mon, Wed, Fri
        repository.insertTask(TestDataFactory.createRepeatTask(repeatDays = setOf(1, 3, 5)))
        // Specific date on 7th (Wednesday)
        repository.insertTask(TestDataFactory.createSpecificDateTask(specificDate = LocalDate.of(2026, 1, 7)))

        val counts = repository.getTaskCountByDateRange(startDate, endDate)

        assertEquals(1, counts[LocalDate.of(2026, 1, 5)])  // Monday: 1 repeat
        assertNull(counts[LocalDate.of(2026, 1, 6)])        // Tuesday: 0
        assertEquals(2, counts[LocalDate.of(2026, 1, 7)])  // Wednesday: 1 repeat + 1 specific
        assertNull(counts[LocalDate.of(2026, 1, 8)])        // Thursday: 0
        assertEquals(1, counts[LocalDate.of(2026, 1, 9)])  // Friday: 1 repeat
    }

    @Test
    fun `clear removes all data`() = runTest {
        repository.insertTask(TestDataFactory.createTask(name = "Task1"))
        repository.insertTask(TestDataFactory.createTask(name = "Task2"))

        repository.clear()

        assertTrue(repository.getTasksSnapshot().isEmpty())
    }

    @Test
    fun `setTasks replaces all data`() = runTest {
        repository.insertTask(TestDataFactory.createTask(name = "Old"))

        val newTasks = TestDataFactory.createTasks(3)
        repository.setTasks(newTasks)

        assertEquals(3, repository.getTasksSnapshot().size)
    }
}
