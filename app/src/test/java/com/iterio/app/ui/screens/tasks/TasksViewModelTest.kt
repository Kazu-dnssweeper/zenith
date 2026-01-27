package com.iterio.app.ui.screens.tasks

import com.iterio.app.domain.common.DomainError
import com.iterio.app.domain.common.Result
import com.iterio.app.domain.model.PomodoroSettings
import com.iterio.app.domain.model.ScheduleType
import com.iterio.app.domain.model.SubjectGroup
import com.iterio.app.domain.model.SubscriptionStatus
import com.iterio.app.domain.model.SubscriptionType
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
 * TasksViewModel のユニットテスト
 *
 * init ブロックから起動される loadGroups / loadDefaultWorkDuration / observePremiumStatus、
 * および各 public メソッドの状態遷移を網羅的にテストする。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TasksViewModelTest {

    private lateinit var subjectGroupRepository: SubjectGroupRepository
    private lateinit var taskRepository: TaskRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var premiumManager: PremiumManager

    private val testDispatcher = StandardTestDispatcher()
    private val subscriptionStatusFlow = MutableStateFlow(SubscriptionStatus())
    private val groupsFlow = MutableStateFlow<List<SubjectGroup>>(emptyList())

    private val group1 = SubjectGroup(id = 1L, name = "Math", colorHex = "#FF0000")
    private val group2 = SubjectGroup(id = 2L, name = "English", colorHex = "#00FF00")
    private val group3 = SubjectGroup(id = 3L, name = "Science", colorHex = "#0000FF")

    private val task1 = Task(id = 10L, groupId = 1L, name = "Algebra homework")
    private val task2 = Task(id = 11L, groupId = 1L, name = "Calculus review")
    private val taskWithSchedule = Task(
        id = 12L,
        groupId = 1L,
        name = "Weekly quiz",
        scheduleType = ScheduleType.REPEAT,
        repeatDays = setOf(1, 3, 5),
        deadlineDate = LocalDate.of(2026, 6, 30),
        specificDate = LocalDate.of(2026, 3, 15),
        reviewCount = 4,
        reviewEnabled = false
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        subjectGroupRepository = mockk(relaxed = true)
        taskRepository = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)
        premiumManager = mockk(relaxed = true)

        // Default mock setup
        every { subjectGroupRepository.getAllGroups() } returns groupsFlow
        every { taskRepository.getTasksByGroup(any()) } returns flowOf(emptyList())
        coEvery { settingsRepository.getPomodoroSettings() } returns Result.Success(PomodoroSettings())
        every { premiumManager.subscriptionStatus } returns subscriptionStatusFlow
        every { premiumManager.getReviewCountOptions(any()) } returns listOf(2)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): TasksViewModel {
        return TasksViewModel(
            subjectGroupRepository = subjectGroupRepository,
            taskRepository = taskRepository,
            settingsRepository = settingsRepository,
            premiumManager = premiumManager
        )
    }

    // ========================================================================
    // 1. loadGroups - initial load populates groups in state
    // ========================================================================

    @Test
    fun `loadGroups - initial load populates groups in state`() = runTest {
        groupsFlow.value = listOf(group1, group2)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.groups.size)
        assertEquals("Math", state.groups[0].name)
        assertEquals("English", state.groups[1].name)
        assertFalse(state.isLoading)
    }

    // ========================================================================
    // 2. loadGroups - selects first group automatically
    // ========================================================================

    @Test
    fun `loadGroups - selects first group automatically when no group selected`() = runTest {
        groupsFlow.value = listOf(group1, group2)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(group1, state.selectedGroup)
    }

    // ========================================================================
    // 3. loadGroups - doesn't override existing selectedGroup
    // ========================================================================

    @Test
    fun `loadGroups - does not override existing selectedGroup on re-emission`() = runTest {
        groupsFlow.value = listOf(group1, group2)

        val viewModel = createViewModel()
        advanceUntilIdle()

        // Select the second group manually
        viewModel.selectGroup(group2)
        advanceUntilIdle()

        // Simulate a new emission from the Flow (e.g., a third group added)
        groupsFlow.value = listOf(group1, group2, group3)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(3, state.groups.size)
        // selectedGroup should remain group2, not reset to group1
        assertEquals(group2, state.selectedGroup)
    }

    // ========================================================================
    // 4. loadDefaultWorkDuration - updates defaultWorkDurationMinutes
    // ========================================================================

    @Test
    fun `loadDefaultWorkDuration - updates defaultWorkDurationMinutes from settings`() = runTest {
        coEvery { settingsRepository.getPomodoroSettings() } returns Result.Success(
            PomodoroSettings(workDurationMinutes = 50)
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(50, viewModel.uiState.value.defaultWorkDurationMinutes)
    }

    // ========================================================================
    // 5. observePremiumStatus - updates isPremium and reviewCountOptions
    // ========================================================================

    @Test
    fun `observePremiumStatus - updates isPremium and reviewCountOptions when premium`() = runTest {
        every { premiumManager.getReviewCountOptions(true) } returns listOf(2, 4, 6)
        subscriptionStatusFlow.value = SubscriptionStatus(type = SubscriptionType.LIFETIME)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isPremium)
        assertEquals(listOf(2, 4, 6), state.reviewCountOptions)
    }

    // ========================================================================
    // 6. selectGroup - updates selectedGroup and loads tasks
    // ========================================================================

    @Test
    fun `selectGroup - updates selectedGroup and loads tasks for that group`() = runTest {
        groupsFlow.value = listOf(group1, group2)
        every { taskRepository.getTasksByGroup(2L) } returns flowOf(
            listOf(Task(id = 20L, groupId = 2L, name = "English essay"))
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.selectGroup(group2)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(group2, state.selectedGroup)
        assertEquals(1, state.tasksForSelectedGroup.size)
        assertEquals("English essay", state.tasksForSelectedGroup[0].name)
    }

    // ========================================================================
    // 7. addGroup - success case (calls insertGroup, hides dialog, selects new group)
    // ========================================================================

    @Test
    fun `addGroup - success hides dialog and selects newly created group`() = runTest {
        groupsFlow.value = listOf(group1)
        val newGroup = SubjectGroup(id = 5L, name = "History", colorHex = "#AABB00")

        coEvery { subjectGroupRepository.insertGroup(any()) } returns Result.Success(5L)
        coEvery { subjectGroupRepository.getGroupById(5L) } returns Result.Success(newGroup)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.showAddGroupDialog()
        viewModel.addGroup("History", "#AABB00")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.showAddGroupDialog)
        assertEquals(newGroup, state.selectedGroup)
    }

    // ========================================================================
    // 8. addGroup - success inserts and refreshes
    // ========================================================================

    @Test
    fun `addGroup - success calls insertGroup with correct parameters`() = runTest {
        groupsFlow.value = listOf(group1)
        coEvery { subjectGroupRepository.insertGroup(any()) } returns Result.Success(10L)
        coEvery { subjectGroupRepository.getGroupById(10L) } returns Result.Success(
            SubjectGroup(id = 10L, name = "Physics", colorHex = "#112233")
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.addGroup("Physics", "#112233")
        advanceUntilIdle()

        coVerify {
            subjectGroupRepository.insertGroup(match {
                it.name == "Physics" && it.colorHex == "#112233"
            })
        }
        coVerify { subjectGroupRepository.getGroupById(10L) }
    }

    // ========================================================================
    // 9. updateGroup - success hides dialog
    // ========================================================================

    @Test
    fun `updateGroup - success hides edit group dialog`() = runTest {
        groupsFlow.value = listOf(group1)
        coEvery { subjectGroupRepository.updateGroup(any()) } returns Result.Success(Unit)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.showEditGroupDialog(group1)
        assertTrue(viewModel.uiState.value.showEditGroupDialog)

        val updatedGroup = group1.copy(name = "Advanced Math")
        viewModel.updateGroup(updatedGroup)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.showEditGroupDialog)
        assertNull(state.editingGroup)
    }

    // ========================================================================
    // 10. deleteGroup - success removes group, selects first remaining
    // ========================================================================

    @Test
    fun `deleteGroup - success selects first remaining group`() = runTest {
        groupsFlow.value = listOf(group1, group2)
        coEvery { subjectGroupRepository.deleteGroup(any()) } returns Result.Success(Unit)

        val viewModel = createViewModel()
        advanceUntilIdle()

        // Currently group1 is selected (first auto-select)
        viewModel.deleteGroup(group1)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(group2, state.selectedGroup)
        assertTrue(state.tasksForSelectedGroup.isEmpty())
        assertFalse(state.showEditGroupDialog)
    }

    // ========================================================================
    // 11. deleteGroup - when last group deleted, selectedGroup becomes null
    // ========================================================================

    @Test
    fun `deleteGroup - when last group deleted selectedGroup becomes null`() = runTest {
        groupsFlow.value = listOf(group1)
        coEvery { subjectGroupRepository.deleteGroup(any()) } returns Result.Success(Unit)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.deleteGroup(group1)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.selectedGroup)
        assertTrue(state.tasksForSelectedGroup.isEmpty())
    }

    // ========================================================================
    // 12. addTask - success hides dialog
    // ========================================================================

    @Test
    fun `addTask - success hides add task dialog`() = runTest {
        groupsFlow.value = listOf(group1)
        coEvery { taskRepository.insertTask(any()) } returns Result.Success(100L)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.showAddTaskDialog()
        assertTrue(viewModel.uiState.value.showAddTaskDialog)

        viewModel.addTask("New task", 25)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showAddTaskDialog)
    }

    // ========================================================================
    // 13. addTask - returns early when no selectedGroup
    // ========================================================================

    @Test
    fun `addTask - returns early when no selectedGroup`() = runTest {
        // No groups -> no selectedGroup
        groupsFlow.value = emptyList()

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.selectedGroup)

        viewModel.addTask("Orphan task", 30)
        advanceUntilIdle()

        // insertTask should never be called
        coVerify(exactly = 0) { taskRepository.insertTask(any()) }
    }

    // ========================================================================
    // 14. addTask with all schedule params
    // ========================================================================

    @Test
    fun `addTask - passes all schedule parameters to repository`() = runTest {
        groupsFlow.value = listOf(group1)
        coEvery { taskRepository.insertTask(any()) } returns Result.Success(200L)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val deadline = LocalDate.of(2026, 12, 31)
        val specificDate = LocalDate.of(2026, 6, 15)

        viewModel.addTask(
            name = "Full params task",
            workDurationMinutes = 45,
            scheduleType = ScheduleType.DEADLINE,
            repeatDays = setOf(1, 3, 5),
            deadlineDate = deadline,
            specificDate = specificDate,
            reviewCount = 4,
            reviewEnabled = false
        )
        advanceUntilIdle()

        coVerify {
            taskRepository.insertTask(match { task ->
                task.groupId == 1L &&
                task.name == "Full params task" &&
                task.workDurationMinutes == 45 &&
                task.scheduleType == ScheduleType.DEADLINE &&
                task.repeatDays == setOf(1, 3, 5) &&
                task.deadlineDate == deadline &&
                task.specificDate == specificDate &&
                task.reviewCount == 4 &&
                !task.reviewEnabled
            })
        }
    }

    // ========================================================================
    // 15. updateTask - success hides dialog
    // ========================================================================

    @Test
    fun `updateTask - success hides edit task dialog`() = runTest {
        groupsFlow.value = listOf(group1)
        coEvery { taskRepository.updateTask(any()) } returns Result.Success(Unit)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.showEditTaskDialog(task1)
        assertTrue(viewModel.uiState.value.showEditTaskDialog)

        viewModel.updateTask(task1.copy(name = "Updated algebra"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.showEditTaskDialog)
        assertNull(state.editingTask)
        assertEquals(ScheduleType.NONE, state.editingScheduleType)
    }

    // ========================================================================
    // 16. deleteTask - success hides dialog
    // ========================================================================

    @Test
    fun `deleteTask - success hides edit task dialog`() = runTest {
        groupsFlow.value = listOf(group1)
        coEvery { taskRepository.deleteTask(any()) } returns Result.Success(Unit)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.showEditTaskDialog(task1)
        assertTrue(viewModel.uiState.value.showEditTaskDialog)

        viewModel.deleteTask(task1)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.showEditTaskDialog)
        assertNull(state.editingTask)
    }

    // ========================================================================
    // 17. updateTaskProgress - calls repository
    // ========================================================================

    @Test
    fun `updateTaskProgress - calls repository with correct parameters`() = runTest {
        groupsFlow.value = listOf(group1)
        coEvery { taskRepository.updateProgress(any(), any(), any(), any()) } returns Result.Success(Unit)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateTaskProgress(10L, "Good progress", 75, "Finish chapter 5")
        advanceUntilIdle()

        coVerify { taskRepository.updateProgress(10L, "Good progress", 75, "Finish chapter 5") }
    }

    // ========================================================================
    // 18. showAddGroupDialog / hideAddGroupDialog toggles
    // ========================================================================

    @Test
    fun `showAddGroupDialog and hideAddGroupDialog toggle dialog visibility`() = runTest {
        groupsFlow.value = listOf(group1)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showAddGroupDialog)

        viewModel.showAddGroupDialog()
        assertTrue(viewModel.uiState.value.showAddGroupDialog)

        viewModel.hideAddGroupDialog()
        assertFalse(viewModel.uiState.value.showAddGroupDialog)
    }

    // ========================================================================
    // 19. showEditGroupDialog sets editing group
    // ========================================================================

    @Test
    fun `showEditGroupDialog sets editingGroup and shows dialog`() = runTest {
        groupsFlow.value = listOf(group1)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.showEditGroupDialog(group1)

        val state = viewModel.uiState.value
        assertTrue(state.showEditGroupDialog)
        assertEquals(group1, state.editingGroup)
    }

    // ========================================================================
    // 20. showAddTaskDialog / hideAddTaskDialog (resets adding state)
    // ========================================================================

    @Test
    fun `hideAddTaskDialog resets all adding state fields`() = runTest {
        groupsFlow.value = listOf(group1)

        val viewModel = createViewModel()
        advanceUntilIdle()

        // Set up some adding state
        viewModel.showAddTaskDialog()
        viewModel.updateAddingScheduleType(ScheduleType.REPEAT)
        viewModel.updateAddingRepeatDays(setOf(1, 2))
        viewModel.updateAddingDeadlineDate(LocalDate.of(2026, 3, 1))
        viewModel.updateAddingSpecificDate(LocalDate.of(2026, 4, 1))
        viewModel.updateAddingReviewCount(4)
        viewModel.updateAddingReviewEnabled(false)

        // Verify state was set
        val beforeState = viewModel.uiState.value
        assertTrue(beforeState.showAddTaskDialog)
        assertEquals(ScheduleType.REPEAT, beforeState.addingScheduleType)
        assertEquals(setOf(1, 2), beforeState.addingRepeatDays)
        assertNotNull(beforeState.addingDeadlineDate)
        assertNotNull(beforeState.addingSpecificDate)
        assertEquals(4, beforeState.addingReviewCount)
        assertFalse(beforeState.addingReviewEnabled)

        // Hide and verify reset
        viewModel.hideAddTaskDialog()

        val afterState = viewModel.uiState.value
        assertFalse(afterState.showAddTaskDialog)
        assertEquals(ScheduleType.NONE, afterState.addingScheduleType)
        assertTrue(afterState.addingRepeatDays.isEmpty())
        assertNull(afterState.addingDeadlineDate)
        assertNull(afterState.addingSpecificDate)
        assertNull(afterState.addingReviewCount)
        assertTrue(afterState.addingReviewEnabled)
    }

    // ========================================================================
    // 21. showEditTaskDialog populates editing state from task
    // ========================================================================

    @Test
    fun `showEditTaskDialog populates all editing state fields from task`() = runTest {
        groupsFlow.value = listOf(group1)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.showEditTaskDialog(taskWithSchedule)

        val state = viewModel.uiState.value
        assertTrue(state.showEditTaskDialog)
        assertEquals(taskWithSchedule, state.editingTask)
        assertEquals(ScheduleType.REPEAT, state.editingScheduleType)
        assertEquals(setOf(1, 3, 5), state.editingRepeatDays)
        assertEquals(LocalDate.of(2026, 6, 30), state.editingDeadlineDate)
        assertEquals(LocalDate.of(2026, 3, 15), state.editingSpecificDate)
        assertEquals(4, state.editingReviewCount)
        assertFalse(state.editingReviewEnabled)
    }

    // ========================================================================
    // 22. hideEditTaskDialog resets editing state
    // ========================================================================

    @Test
    fun `hideEditTaskDialog resets all editing state fields`() = runTest {
        groupsFlow.value = listOf(group1)

        val viewModel = createViewModel()
        advanceUntilIdle()

        // First open the edit dialog with a task that has schedule data
        viewModel.showEditTaskDialog(taskWithSchedule)

        // Verify editing state is populated
        assertTrue(viewModel.uiState.value.showEditTaskDialog)
        assertNotNull(viewModel.uiState.value.editingTask)

        // Now hide and verify full reset
        viewModel.hideEditTaskDialog()

        val state = viewModel.uiState.value
        assertFalse(state.showEditTaskDialog)
        assertNull(state.editingTask)
        assertEquals(ScheduleType.NONE, state.editingScheduleType)
        assertTrue(state.editingRepeatDays.isEmpty())
        assertNull(state.editingDeadlineDate)
        assertNull(state.editingSpecificDate)
        assertNull(state.editingReviewCount)
        assertTrue(state.editingReviewEnabled)
    }

    // ========================================================================
    // 23. updateAddingScheduleType / updateEditingScheduleType
    // ========================================================================

    @Test
    fun `updateAddingScheduleType and updateEditingScheduleType update state`() = runTest {
        groupsFlow.value = listOf(group1)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateAddingScheduleType(ScheduleType.DEADLINE)
        assertEquals(ScheduleType.DEADLINE, viewModel.uiState.value.addingScheduleType)

        viewModel.updateEditingScheduleType(ScheduleType.SPECIFIC)
        assertEquals(ScheduleType.SPECIFIC, viewModel.uiState.value.editingScheduleType)
    }

    // ========================================================================
    // 24. updateAddingReviewCount / updateEditingReviewCount
    // ========================================================================

    @Test
    fun `updateAddingReviewCount and updateEditingReviewCount update state`() = runTest {
        groupsFlow.value = listOf(group1)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateAddingReviewCount(6)
        assertEquals(6, viewModel.uiState.value.addingReviewCount)

        viewModel.updateAddingReviewCount(null)
        assertNull(viewModel.uiState.value.addingReviewCount)

        viewModel.updateEditingReviewCount(4)
        assertEquals(4, viewModel.uiState.value.editingReviewCount)

        viewModel.updateEditingReviewCount(null)
        assertNull(viewModel.uiState.value.editingReviewCount)
    }

    // ========================================================================
    // 25. updateAddingReviewEnabled / updateEditingReviewEnabled
    // ========================================================================

    @Test
    fun `updateAddingReviewEnabled and updateEditingReviewEnabled update state`() = runTest {
        groupsFlow.value = listOf(group1)

        val viewModel = createViewModel()
        advanceUntilIdle()

        // Default is true
        assertTrue(viewModel.uiState.value.addingReviewEnabled)
        assertTrue(viewModel.uiState.value.editingReviewEnabled)

        viewModel.updateAddingReviewEnabled(false)
        assertFalse(viewModel.uiState.value.addingReviewEnabled)

        viewModel.updateAddingReviewEnabled(true)
        assertTrue(viewModel.uiState.value.addingReviewEnabled)

        viewModel.updateEditingReviewEnabled(false)
        assertFalse(viewModel.uiState.value.editingReviewEnabled)

        viewModel.updateEditingReviewEnabled(true)
        assertTrue(viewModel.uiState.value.editingReviewEnabled)
    }
}
