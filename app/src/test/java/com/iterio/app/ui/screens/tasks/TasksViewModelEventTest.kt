package com.iterio.app.ui.screens.tasks

import com.iterio.app.domain.common.Result
import com.iterio.app.domain.model.PomodoroSettings
import com.iterio.app.domain.model.ScheduleType
import com.iterio.app.domain.model.SubjectGroup
import com.iterio.app.domain.model.SubscriptionStatus
import com.iterio.app.domain.model.Task
import com.iterio.app.domain.repository.SettingsRepository
import com.iterio.app.domain.repository.SubjectGroupRepository
import com.iterio.app.domain.repository.TaskRepository
import com.iterio.app.ui.premium.PremiumManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

/**
 * TasksViewModel の onEvent テスト
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TasksViewModelEventTest {

    private lateinit var subjectGroupRepository: SubjectGroupRepository
    private lateinit var taskRepository: TaskRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var premiumManager: PremiumManager
    private lateinit var viewModel: TasksViewModel

    private val testDispatcher = StandardTestDispatcher()
    private val subscriptionStatusFlow = MutableStateFlow(SubscriptionStatus())

    private val mockGroup = SubjectGroup(id = 1L, name = "Math", colorHex = "#FF0000")
    private val mockTask = Task(id = 1L, groupId = 1L, name = "Study")

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        subjectGroupRepository = mockk(relaxed = true)
        taskRepository = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)
        premiumManager = mockk(relaxed = true)

        every { subjectGroupRepository.getAllGroups() } returns flowOf(listOf(mockGroup))
        every { taskRepository.getTasksByGroup(any()) } returns flowOf(emptyList())
        coEvery { settingsRepository.getPomodoroSettings() } returns Result.Success(PomodoroSettings())
        every { premiumManager.subscriptionStatus } returns subscriptionStatusFlow
        coEvery { premiumManager.isPremium() } returns false
        every { premiumManager.getReviewCountOptions(any()) } returns listOf(2)

        viewModel = TasksViewModel(
            subjectGroupRepository = subjectGroupRepository,
            taskRepository = taskRepository,
            settingsRepository = settingsRepository,
            premiumManager = premiumManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Dialog visibility tests

    @Test
    fun `onEvent ShowAddGroupDialog shows dialog`() = runTest {
        advanceUntilIdle()

        viewModel.onEvent(TasksEvent.ShowAddGroupDialog)

        assertTrue(viewModel.uiState.value.showAddGroupDialog)
    }

    @Test
    fun `onEvent HideAddGroupDialog hides dialog`() = runTest {
        advanceUntilIdle()
        viewModel.onEvent(TasksEvent.ShowAddGroupDialog)

        viewModel.onEvent(TasksEvent.HideAddGroupDialog)

        assertFalse(viewModel.uiState.value.showAddGroupDialog)
    }

    @Test
    fun `onEvent ShowEditGroupDialog shows dialog with group`() = runTest {
        advanceUntilIdle()

        viewModel.onEvent(TasksEvent.ShowEditGroupDialog(mockGroup))

        assertTrue(viewModel.uiState.value.showEditGroupDialog)
        assertEquals(mockGroup, viewModel.uiState.value.editingGroup)
    }

    @Test
    fun `onEvent HideEditGroupDialog hides dialog`() = runTest {
        advanceUntilIdle()
        viewModel.onEvent(TasksEvent.ShowEditGroupDialog(mockGroup))

        viewModel.onEvent(TasksEvent.HideEditGroupDialog)

        assertFalse(viewModel.uiState.value.showEditGroupDialog)
        assertNull(viewModel.uiState.value.editingGroup)
    }

    @Test
    fun `onEvent ShowAddTaskDialog shows dialog`() = runTest {
        advanceUntilIdle()

        viewModel.onEvent(TasksEvent.ShowAddTaskDialog)

        assertTrue(viewModel.uiState.value.showAddTaskDialog)
    }

    @Test
    fun `onEvent HideAddTaskDialog hides dialog and resets state`() = runTest {
        advanceUntilIdle()
        viewModel.onEvent(TasksEvent.ShowAddTaskDialog)
        viewModel.onEvent(TasksEvent.UpdateAddingScheduleType(ScheduleType.REPEAT))

        viewModel.onEvent(TasksEvent.HideAddTaskDialog)

        assertFalse(viewModel.uiState.value.showAddTaskDialog)
        assertEquals(ScheduleType.NONE, viewModel.uiState.value.addingScheduleType)
    }

    @Test
    fun `onEvent ShowEditTaskDialog shows dialog with task`() = runTest {
        advanceUntilIdle()

        viewModel.onEvent(TasksEvent.ShowEditTaskDialog(mockTask))

        assertTrue(viewModel.uiState.value.showEditTaskDialog)
        assertEquals(mockTask, viewModel.uiState.value.editingTask)
    }

    @Test
    fun `onEvent HideEditTaskDialog hides dialog`() = runTest {
        advanceUntilIdle()
        viewModel.onEvent(TasksEvent.ShowEditTaskDialog(mockTask))

        viewModel.onEvent(TasksEvent.HideEditTaskDialog)

        assertFalse(viewModel.uiState.value.showEditTaskDialog)
        assertNull(viewModel.uiState.value.editingTask)
    }

    // Schedule state tests

    @Test
    fun `onEvent UpdateAddingScheduleType updates state`() = runTest {
        advanceUntilIdle()

        viewModel.onEvent(TasksEvent.UpdateAddingScheduleType(ScheduleType.DEADLINE))

        assertEquals(ScheduleType.DEADLINE, viewModel.uiState.value.addingScheduleType)
    }

    @Test
    fun `onEvent UpdateAddingRepeatDays updates state`() = runTest {
        advanceUntilIdle()

        viewModel.onEvent(TasksEvent.UpdateAddingRepeatDays(setOf(1, 3, 5)))

        assertEquals(setOf(1, 3, 5), viewModel.uiState.value.addingRepeatDays)
    }

    @Test
    fun `onEvent UpdateAddingDeadlineDate updates state`() = runTest {
        advanceUntilIdle()
        val date = LocalDate.of(2026, 3, 15)

        viewModel.onEvent(TasksEvent.UpdateAddingDeadlineDate(date))

        assertEquals(date, viewModel.uiState.value.addingDeadlineDate)
    }

    @Test
    fun `onEvent UpdateAddingSpecificDate updates state`() = runTest {
        advanceUntilIdle()
        val date = LocalDate.of(2026, 4, 20)

        viewModel.onEvent(TasksEvent.UpdateAddingSpecificDate(date))

        assertEquals(date, viewModel.uiState.value.addingSpecificDate)
    }

    @Test
    fun `onEvent UpdateEditingScheduleType updates state`() = runTest {
        advanceUntilIdle()

        viewModel.onEvent(TasksEvent.UpdateEditingScheduleType(ScheduleType.SPECIFIC))

        assertEquals(ScheduleType.SPECIFIC, viewModel.uiState.value.editingScheduleType)
    }

    @Test
    fun `onEvent UpdateEditingRepeatDays updates state`() = runTest {
        advanceUntilIdle()

        viewModel.onEvent(TasksEvent.UpdateEditingRepeatDays(setOf(2, 4)))

        assertEquals(setOf(2, 4), viewModel.uiState.value.editingRepeatDays)
    }

    @Test
    fun `onEvent UpdateEditingDeadlineDate updates state`() = runTest {
        advanceUntilIdle()
        val date = LocalDate.of(2026, 5, 10)

        viewModel.onEvent(TasksEvent.UpdateEditingDeadlineDate(date))

        assertEquals(date, viewModel.uiState.value.editingDeadlineDate)
    }

    @Test
    fun `onEvent UpdateEditingSpecificDate updates state`() = runTest {
        advanceUntilIdle()
        val date = LocalDate.of(2026, 6, 25)

        viewModel.onEvent(TasksEvent.UpdateEditingSpecificDate(date))

        assertEquals(date, viewModel.uiState.value.editingSpecificDate)
    }

    // CRUD tests

    @Test
    fun `onEvent AddGroup calls repository`() = runTest {
        advanceUntilIdle()

        viewModel.onEvent(TasksEvent.AddGroup("Physics", "#00FF00"))
        advanceUntilIdle()

        coVerify { subjectGroupRepository.insertGroup(match { it.name == "Physics" && it.colorHex == "#00FF00" }) }
    }

    @Test
    fun `onEvent UpdateGroup calls repository`() = runTest {
        advanceUntilIdle()

        viewModel.onEvent(TasksEvent.UpdateGroup(mockGroup))
        advanceUntilIdle()

        coVerify { subjectGroupRepository.updateGroup(mockGroup) }
    }

    @Test
    fun `onEvent DeleteGroup calls repository`() = runTest {
        advanceUntilIdle()

        viewModel.onEvent(TasksEvent.DeleteGroup(mockGroup))
        advanceUntilIdle()

        coVerify { subjectGroupRepository.deleteGroup(mockGroup) }
    }

    @Test
    fun `onEvent AddTask calls repository`() = runTest {
        advanceUntilIdle()

        viewModel.onEvent(TasksEvent.AddTask(
            name = "Homework",
            workDurationMinutes = 45,
            scheduleType = ScheduleType.REPEAT,
            repeatDays = setOf(1, 2, 3)
        ))
        advanceUntilIdle()

        coVerify { taskRepository.insertTask(match { it.name == "Homework" && it.workDurationMinutes == 45 }) }
    }

    @Test
    fun `onEvent UpdateTask calls repository`() = runTest {
        advanceUntilIdle()

        viewModel.onEvent(TasksEvent.UpdateTask(mockTask))
        advanceUntilIdle()

        coVerify { taskRepository.updateTask(mockTask) }
    }

    @Test
    fun `onEvent DeleteTask calls repository`() = runTest {
        advanceUntilIdle()

        viewModel.onEvent(TasksEvent.DeleteTask(mockTask))
        advanceUntilIdle()

        coVerify { taskRepository.deleteTask(mockTask) }
    }

    @Test
    fun `onEvent UpdateTaskProgress calls repository`() = runTest {
        advanceUntilIdle()

        viewModel.onEvent(TasksEvent.UpdateTaskProgress(1L, "Good", 50, "Goal"))
        advanceUntilIdle()

        coVerify { taskRepository.updateProgress(1L, "Good", 50, "Goal") }
    }
}
