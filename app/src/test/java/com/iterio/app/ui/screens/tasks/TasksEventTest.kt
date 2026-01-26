package com.iterio.app.ui.screens.tasks

import com.iterio.app.domain.model.ScheduleType
import com.iterio.app.domain.model.SubjectGroup
import com.iterio.app.domain.model.Task
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

/**
 * TasksEvent のユニットテスト
 */
class TasksEventTest {

    private val mockGroup = SubjectGroup(id = 1L, name = "Math", colorHex = "#FF0000")
    private val mockTask = Task(id = 1L, groupId = 1L, name = "Study")

    // Group selection events

    @Test
    fun `SelectGroup contains group`() {
        val event = TasksEvent.SelectGroup(mockGroup)
        assertEquals(mockGroup, event.group)
    }

    // Dialog visibility events

    @Test
    fun `ShowAddGroupDialog is a singleton object`() {
        val event1 = TasksEvent.ShowAddGroupDialog
        val event2 = TasksEvent.ShowAddGroupDialog
        assertSame(event1, event2)
    }

    @Test
    fun `HideAddGroupDialog is a singleton object`() {
        assertSame(TasksEvent.HideAddGroupDialog, TasksEvent.HideAddGroupDialog)
    }

    @Test
    fun `ShowEditGroupDialog contains group`() {
        val event = TasksEvent.ShowEditGroupDialog(mockGroup)
        assertEquals(mockGroup, event.group)
    }

    @Test
    fun `HideEditGroupDialog is a singleton object`() {
        assertSame(TasksEvent.HideEditGroupDialog, TasksEvent.HideEditGroupDialog)
    }

    @Test
    fun `ShowAddTaskDialog is a singleton object`() {
        assertSame(TasksEvent.ShowAddTaskDialog, TasksEvent.ShowAddTaskDialog)
    }

    @Test
    fun `HideAddTaskDialog is a singleton object`() {
        assertSame(TasksEvent.HideAddTaskDialog, TasksEvent.HideAddTaskDialog)
    }

    @Test
    fun `ShowEditTaskDialog contains task`() {
        val event = TasksEvent.ShowEditTaskDialog(mockTask)
        assertEquals(mockTask, event.task)
    }

    @Test
    fun `HideEditTaskDialog is a singleton object`() {
        assertSame(TasksEvent.HideEditTaskDialog, TasksEvent.HideEditTaskDialog)
    }

    // Adding schedule state events

    @Test
    fun `UpdateAddingScheduleType contains type`() {
        val event = TasksEvent.UpdateAddingScheduleType(ScheduleType.REPEAT)
        assertEquals(ScheduleType.REPEAT, event.type)
    }

    @Test
    fun `UpdateAddingRepeatDays contains days set`() {
        val days = setOf(1, 3, 5)
        val event = TasksEvent.UpdateAddingRepeatDays(days)
        assertEquals(days, event.days)
    }

    @Test
    fun `UpdateAddingDeadlineDate contains nullable date`() {
        val date = LocalDate.of(2026, 2, 15)
        val event = TasksEvent.UpdateAddingDeadlineDate(date)
        assertEquals(date, event.date)

        val nullEvent = TasksEvent.UpdateAddingDeadlineDate(null)
        assertNull(nullEvent.date)
    }

    @Test
    fun `UpdateAddingSpecificDate contains nullable date`() {
        val date = LocalDate.of(2026, 3, 20)
        val event = TasksEvent.UpdateAddingSpecificDate(date)
        assertEquals(date, event.date)
    }

    // Editing schedule state events

    @Test
    fun `UpdateEditingScheduleType contains type`() {
        val event = TasksEvent.UpdateEditingScheduleType(ScheduleType.SPECIFIC)
        assertEquals(ScheduleType.SPECIFIC, event.type)
    }

    @Test
    fun `UpdateEditingRepeatDays contains days set`() {
        val days = setOf(2, 4)
        val event = TasksEvent.UpdateEditingRepeatDays(days)
        assertEquals(days, event.days)
    }

    @Test
    fun `UpdateEditingDeadlineDate contains nullable date`() {
        val date = LocalDate.of(2026, 4, 10)
        val event = TasksEvent.UpdateEditingDeadlineDate(date)
        assertEquals(date, event.date)
    }

    @Test
    fun `UpdateEditingSpecificDate contains nullable date`() {
        val event = TasksEvent.UpdateEditingSpecificDate(null)
        assertNull(event.date)
    }

    // CRUD events

    @Test
    fun `AddGroup contains name and colorHex`() {
        val event = TasksEvent.AddGroup("Physics", "#00FF00")
        assertEquals("Physics", event.name)
        assertEquals("#00FF00", event.colorHex)
    }

    @Test
    fun `UpdateGroup contains group`() {
        val event = TasksEvent.UpdateGroup(mockGroup)
        assertEquals(mockGroup, event.group)
    }

    @Test
    fun `DeleteGroup contains group`() {
        val event = TasksEvent.DeleteGroup(mockGroup)
        assertEquals(mockGroup, event.group)
    }

    @Test
    fun `AddTask contains all parameters`() {
        val event = TasksEvent.AddTask(
            name = "Homework",
            workDurationMinutes = 30,
            scheduleType = ScheduleType.REPEAT,
            repeatDays = setOf(1, 2, 3),
            deadlineDate = LocalDate.of(2026, 5, 1),
            specificDate = null
        )
        assertEquals("Homework", event.name)
        assertEquals(30, event.workDurationMinutes)
        assertEquals(ScheduleType.REPEAT, event.scheduleType)
        assertEquals(setOf(1, 2, 3), event.repeatDays)
        assertEquals(LocalDate.of(2026, 5, 1), event.deadlineDate)
        assertNull(event.specificDate)
    }

    @Test
    fun `AddTask has default values`() {
        val event = TasksEvent.AddTask(name = "Quick task", workDurationMinutes = null)
        assertEquals("Quick task", event.name)
        assertNull(event.workDurationMinutes)
        assertEquals(ScheduleType.NONE, event.scheduleType)
        assertTrue(event.repeatDays.isEmpty())
        assertNull(event.deadlineDate)
        assertNull(event.specificDate)
    }

    @Test
    fun `UpdateTask contains task`() {
        val event = TasksEvent.UpdateTask(mockTask)
        assertEquals(mockTask, event.task)
    }

    @Test
    fun `DeleteTask contains task`() {
        val event = TasksEvent.DeleteTask(mockTask)
        assertEquals(mockTask, event.task)
    }

    @Test
    fun `UpdateTaskProgress contains all parameters`() {
        val event = TasksEvent.UpdateTaskProgress(
            taskId = 5L,
            note = "Good progress",
            percent = 75,
            goal = "Finish chapter 3"
        )
        assertEquals(5L, event.taskId)
        assertEquals("Good progress", event.note)
        assertEquals(75, event.percent)
        assertEquals("Finish chapter 3", event.goal)
    }

    // Exhaustive event check

    @Test
    fun `all events are TasksEvent subtypes`() {
        val events: List<TasksEvent> = listOf(
            TasksEvent.SelectGroup(mockGroup),
            TasksEvent.ShowAddGroupDialog,
            TasksEvent.HideAddGroupDialog,
            TasksEvent.ShowEditGroupDialog(mockGroup),
            TasksEvent.HideEditGroupDialog,
            TasksEvent.ShowAddTaskDialog,
            TasksEvent.HideAddTaskDialog,
            TasksEvent.ShowEditTaskDialog(mockTask),
            TasksEvent.HideEditTaskDialog,
            TasksEvent.UpdateAddingScheduleType(ScheduleType.NONE),
            TasksEvent.UpdateAddingRepeatDays(emptySet()),
            TasksEvent.UpdateAddingDeadlineDate(null),
            TasksEvent.UpdateAddingSpecificDate(null),
            TasksEvent.UpdateEditingScheduleType(ScheduleType.NONE),
            TasksEvent.UpdateEditingRepeatDays(emptySet()),
            TasksEvent.UpdateEditingDeadlineDate(null),
            TasksEvent.UpdateEditingSpecificDate(null),
            TasksEvent.AddGroup("Test", "#000000"),
            TasksEvent.UpdateGroup(mockGroup),
            TasksEvent.DeleteGroup(mockGroup),
            TasksEvent.AddTask("Task", null),
            TasksEvent.UpdateTask(mockTask),
            TasksEvent.DeleteTask(mockTask),
            TasksEvent.UpdateTaskProgress(1L, null, null, null)
        )
        assertEquals(24, events.size)
    }
}
