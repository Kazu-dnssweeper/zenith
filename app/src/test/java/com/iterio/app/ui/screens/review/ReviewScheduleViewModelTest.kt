package com.iterio.app.ui.screens.review

import android.content.Context
import app.cash.turbine.test
import com.iterio.app.domain.common.DomainError
import com.iterio.app.widget.IterioWidgetReceiver
import com.iterio.app.domain.common.Result
import com.iterio.app.domain.model.ReviewTask
import com.iterio.app.domain.repository.ReviewTaskRepository
import com.iterio.app.testutil.CoroutineTestRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * ReviewScheduleViewModel unit tests
 *
 * Tests cover:
 * - Initial state and loading
 * - Task loading and grouping by date
 * - Filtering (ALL, PENDING, COMPLETED, OVERDUE)
 * - Count calculations (total, pending, completed, overdue)
 * - Toggle task completion (mark completed / incomplete)
 * - Rescheduling tasks
 * - Edge cases (empty data, single task, all same date)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReviewScheduleViewModelTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var reviewTaskRepository: ReviewTaskRepository
    private lateinit var context: Context
    private val tasksFlow = MutableStateFlow<List<ReviewTask>>(emptyList())

    private val today: LocalDate = LocalDate.now()

    @Before
    fun setup() {
        reviewTaskRepository = mockk()
        context = mockk(relaxed = true)

        // Mock widget broadcast to avoid Android Intent in unit tests
        mockkObject(IterioWidgetReceiver.Companion)
        every { IterioWidgetReceiver.sendDataChangedBroadcast(any()) } returns Unit
        every { IterioWidgetReceiver.sendUpdateBroadcast(any()) } returns Unit

        every { reviewTaskRepository.getAllWithDetails() } returns tasksFlow
    }

    private fun createViewModel() = ReviewScheduleViewModel(
        context = context,
        reviewTaskRepository = reviewTaskRepository
    )

    // ==================== Helper Functions ====================

    private fun createReviewTask(
        id: Long = 1L,
        studySessionId: Long = 1L,
        taskId: Long = 1L,
        scheduledDate: LocalDate = today,
        reviewNumber: Int = 1,
        isCompleted: Boolean = false,
        completedAt: LocalDateTime? = null,
        taskName: String? = "Math Review",
        groupName: String? = "Mathematics"
    ) = ReviewTask(
        id = id,
        studySessionId = studySessionId,
        taskId = taskId,
        scheduledDate = scheduledDate,
        reviewNumber = reviewNumber,
        isCompleted = isCompleted,
        completedAt = completedAt,
        taskName = taskName,
        groupName = groupName
    )

    // ==================== Initial State ====================

    @Test
    fun `initial state is loading with default values`() = runTest {
        val vm = createViewModel()

        vm.uiState.test {
            val initial = awaitItem()
            assertTrue(initial.isLoading)
            assertEquals(ReviewFilter.ALL, initial.selectedFilter)
            assertTrue(initial.allTasks.isEmpty())
            assertTrue(initial.filteredTasks.isEmpty())
            assertTrue(initial.tasksByDate.isEmpty())
            assertEquals(0, initial.totalCount)
            assertEquals(0, initial.pendingCount)
            assertEquals(0, initial.completedCount)
            assertEquals(0, initial.overdueCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== Loading Tasks ====================

    @Test
    fun `loads all tasks from repository`() = runTest {
        val tasks = listOf(
            createReviewTask(id = 1L, scheduledDate = today, taskName = "Task A"),
            createReviewTask(id = 2L, scheduledDate = today.plusDays(1), taskName = "Task B"),
            createReviewTask(id = 3L, scheduledDate = today.plusDays(3), taskName = "Task C")
        )
        tasksFlow.value = tasks

        val vm = createViewModel()
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(3, state.allTasks.size)
            assertEquals(3, state.filteredTasks.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `groups tasks by scheduled date`() = runTest {
        val dateA = today
        val dateB = today.plusDays(3)
        val tasks = listOf(
            createReviewTask(id = 1L, scheduledDate = dateA),
            createReviewTask(id = 2L, scheduledDate = dateA),
            createReviewTask(id = 3L, scheduledDate = dateB)
        )
        tasksFlow.value = tasks

        val vm = createViewModel()
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.tasksByDate.size)
            assertEquals(2, state.tasksByDate[dateA]?.size)
            assertEquals(1, state.tasksByDate[dateB]?.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `tasks by date are sorted by date ascending`() = runTest {
        val dateC = today.plusDays(10)
        val dateA = today
        val dateB = today.plusDays(3)
        val tasks = listOf(
            createReviewTask(id = 1L, scheduledDate = dateC),
            createReviewTask(id = 2L, scheduledDate = dateA),
            createReviewTask(id = 3L, scheduledDate = dateB)
        )
        tasksFlow.value = tasks

        val vm = createViewModel()
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            val dates = state.tasksByDate.keys.toList()
            assertEquals(3, dates.size)
            assertEquals(dateA, dates[0])
            assertEquals(dateB, dates[1])
            assertEquals(dateC, dates[2])
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `calculates counts correctly`() = runTest {
        val tasks = listOf(
            // Pending: not completed, scheduled today or future
            createReviewTask(id = 1L, scheduledDate = today, isCompleted = false),
            createReviewTask(id = 2L, scheduledDate = today.plusDays(5), isCompleted = false),
            // Completed
            createReviewTask(
                id = 3L,
                scheduledDate = today.minusDays(1),
                isCompleted = true,
                completedAt = LocalDateTime.now()
            ),
            createReviewTask(
                id = 4L,
                scheduledDate = today,
                isCompleted = true,
                completedAt = LocalDateTime.now()
            ),
            // Overdue: not completed, scheduled before today
            createReviewTask(id = 5L, scheduledDate = today.minusDays(3), isCompleted = false),
            createReviewTask(id = 6L, scheduledDate = today.minusDays(1), isCompleted = false)
        )
        tasksFlow.value = tasks

        val vm = createViewModel()
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(6, state.totalCount)
            assertEquals(2, state.pendingCount)
            assertEquals(2, state.completedCount)
            assertEquals(2, state.overdueCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `handles empty task list`() = runTest {
        tasksFlow.value = emptyList()

        val vm = createViewModel()
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertTrue(state.allTasks.isEmpty())
            assertTrue(state.filteredTasks.isEmpty())
            assertTrue(state.tasksByDate.isEmpty())
            assertEquals(0, state.totalCount)
            assertEquals(0, state.pendingCount)
            assertEquals(0, state.completedCount)
            assertEquals(0, state.overdueCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== Filtering ====================

    @Test
    fun `filter ALL shows all tasks`() = runTest {
        val tasks = listOf(
            createReviewTask(id = 1L, scheduledDate = today, isCompleted = false),
            createReviewTask(
                id = 2L,
                scheduledDate = today.minusDays(1),
                isCompleted = true,
                completedAt = LocalDateTime.now()
            ),
            createReviewTask(id = 3L, scheduledDate = today.minusDays(2), isCompleted = false)
        )
        tasksFlow.value = tasks

        val vm = createViewModel()
        advanceUntilIdle()

        vm.updateFilter(ReviewFilter.ALL)
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(ReviewFilter.ALL, state.selectedFilter)
            assertEquals(3, state.filteredTasks.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `filter PENDING shows only pending tasks`() = runTest {
        val tasks = listOf(
            createReviewTask(id = 1L, scheduledDate = today, isCompleted = false),
            createReviewTask(id = 2L, scheduledDate = today.plusDays(5), isCompleted = false),
            createReviewTask(
                id = 3L,
                scheduledDate = today,
                isCompleted = true,
                completedAt = LocalDateTime.now()
            ),
            // Overdue (not completed but past) should NOT appear in PENDING
            createReviewTask(id = 4L, scheduledDate = today.minusDays(1), isCompleted = false)
        )
        tasksFlow.value = tasks

        val vm = createViewModel()
        advanceUntilIdle()

        vm.updateFilter(ReviewFilter.PENDING)
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(ReviewFilter.PENDING, state.selectedFilter)
            assertEquals(2, state.filteredTasks.size)
            assertTrue(state.filteredTasks.all { !it.isCompleted && it.scheduledDate >= today })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `filter COMPLETED shows only completed tasks`() = runTest {
        val completedAt = LocalDateTime.now()
        val tasks = listOf(
            createReviewTask(id = 1L, scheduledDate = today, isCompleted = false),
            createReviewTask(
                id = 2L,
                scheduledDate = today.minusDays(1),
                isCompleted = true,
                completedAt = completedAt
            ),
            createReviewTask(
                id = 3L,
                scheduledDate = today.minusDays(3),
                isCompleted = true,
                completedAt = completedAt
            )
        )
        tasksFlow.value = tasks

        val vm = createViewModel()
        advanceUntilIdle()

        vm.updateFilter(ReviewFilter.COMPLETED)
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(ReviewFilter.COMPLETED, state.selectedFilter)
            assertEquals(2, state.filteredTasks.size)
            assertTrue(state.filteredTasks.all { it.isCompleted })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `filter OVERDUE shows only overdue tasks`() = runTest {
        val tasks = listOf(
            // Pending (today or future) - not overdue
            createReviewTask(id = 1L, scheduledDate = today, isCompleted = false),
            createReviewTask(id = 2L, scheduledDate = today.plusDays(3), isCompleted = false),
            // Completed in the past - not overdue
            createReviewTask(
                id = 3L,
                scheduledDate = today.minusDays(2),
                isCompleted = true,
                completedAt = LocalDateTime.now()
            ),
            // Overdue: not completed and past
            createReviewTask(id = 4L, scheduledDate = today.minusDays(1), isCompleted = false),
            createReviewTask(id = 5L, scheduledDate = today.minusDays(5), isCompleted = false)
        )
        tasksFlow.value = tasks

        val vm = createViewModel()
        advanceUntilIdle()

        vm.updateFilter(ReviewFilter.OVERDUE)
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(ReviewFilter.OVERDUE, state.selectedFilter)
            assertEquals(2, state.filteredTasks.size)
            assertTrue(state.filteredTasks.all { !it.isCompleted && it.scheduledDate < today })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `filter updates tasksByDate to match filtered tasks`() = runTest {
        val tasks = listOf(
            createReviewTask(id = 1L, scheduledDate = today, isCompleted = false),
            createReviewTask(
                id = 2L,
                scheduledDate = today,
                isCompleted = true,
                completedAt = LocalDateTime.now()
            ),
            createReviewTask(id = 3L, scheduledDate = today.plusDays(1), isCompleted = false)
        )
        tasksFlow.value = tasks

        val vm = createViewModel()
        advanceUntilIdle()

        vm.updateFilter(ReviewFilter.PENDING)
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            // Only pending tasks grouped by date
            assertEquals(2, state.tasksByDate.size)
            assertEquals(1, state.tasksByDate[today]?.size)
            assertEquals(1, state.tasksByDate[today.plusDays(1)]?.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `switching filter back to ALL restores all tasks`() = runTest {
        val tasks = listOf(
            createReviewTask(id = 1L, scheduledDate = today, isCompleted = false),
            createReviewTask(
                id = 2L,
                scheduledDate = today.minusDays(1),
                isCompleted = true,
                completedAt = LocalDateTime.now()
            )
        )
        tasksFlow.value = tasks

        val vm = createViewModel()
        advanceUntilIdle()

        vm.updateFilter(ReviewFilter.COMPLETED)
        advanceUntilIdle()
        vm.updateFilter(ReviewFilter.ALL)
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(ReviewFilter.ALL, state.selectedFilter)
            assertEquals(2, state.filteredTasks.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== Toggle Task Completion ====================

    @Test
    fun `toggleTaskCompletion marks pending task as completed`() = runTest {
        val tasks = listOf(
            createReviewTask(id = 1L, scheduledDate = today, isCompleted = false)
        )
        tasksFlow.value = tasks
        coEvery { reviewTaskRepository.markAsCompleted(1L) } returns Result.Success(Unit)

        val vm = createViewModel()
        advanceUntilIdle()

        vm.toggleTaskCompletion(1L)
        advanceUntilIdle()

        coVerify { reviewTaskRepository.markAsCompleted(1L) }
    }

    @Test
    fun `toggleTaskCompletion marks completed task as incomplete`() = runTest {
        val tasks = listOf(
            createReviewTask(
                id = 1L,
                scheduledDate = today,
                isCompleted = true,
                completedAt = LocalDateTime.now()
            )
        )
        tasksFlow.value = tasks
        coEvery { reviewTaskRepository.markAsIncomplete(1L) } returns Result.Success(Unit)

        val vm = createViewModel()
        advanceUntilIdle()

        vm.toggleTaskCompletion(1L)
        advanceUntilIdle()

        coVerify { reviewTaskRepository.markAsIncomplete(1L) }
    }

    @Test
    fun `toggleTaskCompletion does nothing for unknown task id`() = runTest {
        val tasks = listOf(
            createReviewTask(id = 1L, scheduledDate = today, isCompleted = false)
        )
        tasksFlow.value = tasks

        val vm = createViewModel()
        advanceUntilIdle()

        // Task ID 999 doesn't exist
        vm.toggleTaskCompletion(999L)
        advanceUntilIdle()

        // Neither markAsCompleted nor markAsIncomplete should be called
        coVerify(exactly = 0) { reviewTaskRepository.markAsCompleted(any()) }
        coVerify(exactly = 0) { reviewTaskRepository.markAsIncomplete(any()) }
    }

    // ==================== Reschedule ====================

    @Test
    fun `rescheduleTask calls repository with correct parameters`() = runTest {
        val tasks = listOf(
            createReviewTask(id = 1L, scheduledDate = today)
        )
        tasksFlow.value = tasks
        val newDate = today.plusDays(7)
        coEvery { reviewTaskRepository.reschedule(1L, newDate) } returns Result.Success(Unit)

        val vm = createViewModel()
        advanceUntilIdle()

        vm.rescheduleTask(1L, newDate)
        advanceUntilIdle()

        coVerify { reviewTaskRepository.reschedule(1L, newDate) }
    }

    // ==================== Reactive Updates ====================

    @Test
    fun `state updates when repository flow emits new data`() = runTest {
        tasksFlow.value = emptyList()

        val vm = createViewModel()
        advanceUntilIdle()

        vm.uiState.test {
            val emptyState = awaitItem()
            assertEquals(0, emptyState.totalCount)

            // Simulate new tasks appearing in the repository
            tasksFlow.value = listOf(
                createReviewTask(id = 1L, scheduledDate = today),
                createReviewTask(id = 2L, scheduledDate = today.plusDays(1))
            )

            val updatedState = awaitItem()
            assertEquals(2, updatedState.totalCount)
            assertEquals(2, updatedState.filteredTasks.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `filter is preserved when repository emits new data`() = runTest {
        val completedAt = LocalDateTime.now()
        tasksFlow.value = listOf(
            createReviewTask(id = 1L, scheduledDate = today, isCompleted = false),
            createReviewTask(
                id = 2L,
                scheduledDate = today,
                isCompleted = true,
                completedAt = completedAt
            )
        )

        val vm = createViewModel()
        advanceUntilIdle()

        vm.updateFilter(ReviewFilter.COMPLETED)
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(ReviewFilter.COMPLETED, state.selectedFilter)
            assertEquals(1, state.filteredTasks.size)

            // New data arrives with an additional completed task
            tasksFlow.value = listOf(
                createReviewTask(id = 1L, scheduledDate = today, isCompleted = false),
                createReviewTask(
                    id = 2L,
                    scheduledDate = today,
                    isCompleted = true,
                    completedAt = completedAt
                ),
                createReviewTask(
                    id = 3L,
                    scheduledDate = today.plusDays(1),
                    isCompleted = true,
                    completedAt = completedAt
                )
            )

            val updatedState = awaitItem()
            assertEquals(ReviewFilter.COMPLETED, updatedState.selectedFilter)
            assertEquals(2, updatedState.filteredTasks.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== Edge Cases ====================

    @Test
    fun `all tasks on same date are grouped together`() = runTest {
        val tasks = (1L..5L).map { id ->
            createReviewTask(id = id, scheduledDate = today, taskName = "Task $id")
        }
        tasksFlow.value = tasks

        val vm = createViewModel()
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.tasksByDate.size)
            assertEquals(5, state.tasksByDate[today]?.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `single task produces correct counts`() = runTest {
        tasksFlow.value = listOf(
            createReviewTask(id = 1L, scheduledDate = today, isCompleted = false)
        )

        val vm = createViewModel()
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.totalCount)
            assertEquals(1, state.pendingCount)
            assertEquals(0, state.completedCount)
            assertEquals(0, state.overdueCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `all tasks completed produces zero pending and overdue`() = runTest {
        val completedAt = LocalDateTime.now()
        val tasks = listOf(
            createReviewTask(
                id = 1L,
                scheduledDate = today,
                isCompleted = true,
                completedAt = completedAt
            ),
            createReviewTask(
                id = 2L,
                scheduledDate = today.minusDays(3),
                isCompleted = true,
                completedAt = completedAt
            )
        )
        tasksFlow.value = tasks

        val vm = createViewModel()
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.totalCount)
            assertEquals(0, state.pendingCount)
            assertEquals(2, state.completedCount)
            assertEquals(0, state.overdueCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `all tasks overdue produces zero pending and completed`() = runTest {
        val tasks = listOf(
            createReviewTask(id = 1L, scheduledDate = today.minusDays(1), isCompleted = false),
            createReviewTask(id = 2L, scheduledDate = today.minusDays(5), isCompleted = false)
        )
        tasksFlow.value = tasks

        val vm = createViewModel()
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.totalCount)
            assertEquals(0, state.pendingCount)
            assertEquals(0, state.completedCount)
            assertEquals(2, state.overdueCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `filter PENDING with no pending tasks returns empty`() = runTest {
        val completedAt = LocalDateTime.now()
        val tasks = listOf(
            createReviewTask(
                id = 1L,
                scheduledDate = today.minusDays(1),
                isCompleted = true,
                completedAt = completedAt
            ),
            createReviewTask(id = 2L, scheduledDate = today.minusDays(2), isCompleted = false)
        )
        tasksFlow.value = tasks

        val vm = createViewModel()
        advanceUntilIdle()

        vm.updateFilter(ReviewFilter.PENDING)
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertTrue(state.filteredTasks.isEmpty())
            assertTrue(state.tasksByDate.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `counts do not change when filter changes`() = runTest {
        val tasks = listOf(
            createReviewTask(id = 1L, scheduledDate = today, isCompleted = false),
            createReviewTask(
                id = 2L,
                scheduledDate = today,
                isCompleted = true,
                completedAt = LocalDateTime.now()
            ),
            createReviewTask(id = 3L, scheduledDate = today.minusDays(1), isCompleted = false)
        )
        tasksFlow.value = tasks

        val vm = createViewModel()
        advanceUntilIdle()

        vm.updateFilter(ReviewFilter.COMPLETED)
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            // Counts should reflect ALL tasks, not filtered
            assertEquals(3, state.totalCount)
            assertEquals(1, state.pendingCount)
            assertEquals(1, state.completedCount)
            assertEquals(1, state.overdueCount)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
