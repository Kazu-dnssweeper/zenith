package com.iterio.app.ui.screens.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.iterio.app.domain.model.ReviewTask
import com.iterio.app.domain.repository.ReviewTaskRepository
import com.iterio.app.widget.IterioWidgetReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ReviewScheduleUiState(
    val allTasks: List<ReviewTask> = emptyList(),
    val filteredTasks: List<ReviewTask> = emptyList(),
    val tasksByDate: Map<LocalDate, List<ReviewTask>> = emptyMap(),
    val selectedFilter: ReviewFilter = ReviewFilter.ALL,
    val isLoading: Boolean = true,
    val totalCount: Int = 0,
    val pendingCount: Int = 0,
    val completedCount: Int = 0,
    val overdueCount: Int = 0,
    val today: LocalDate = LocalDate.now()
)

enum class ReviewFilter {
    ALL, PENDING, COMPLETED, OVERDUE
}

@HiltViewModel
class ReviewScheduleViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val reviewTaskRepository: ReviewTaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewScheduleUiState())
    val uiState: StateFlow<ReviewScheduleUiState> = _uiState.asStateFlow()

    init {
        loadReviewTasks()
    }

    private fun loadReviewTasks() {
        viewModelScope.launch {
            reviewTaskRepository.getAllWithDetails().collect { tasks ->
                val today = _uiState.value.today
                val counts = calculateCounts(tasks, today)
                val currentFilter = _uiState.value.selectedFilter
                val filtered = applyFilter(tasks, currentFilter, today)
                val grouped = groupByDate(filtered)

                _uiState.update {
                    it.copy(
                        allTasks = tasks,
                        filteredTasks = filtered,
                        tasksByDate = grouped,
                        isLoading = false,
                        totalCount = counts.total,
                        pendingCount = counts.pending,
                        completedCount = counts.completed,
                        overdueCount = counts.overdue
                    )
                }
            }
        }
    }

    fun updateFilter(filter: ReviewFilter) {
        val state = _uiState.value
        val filtered = applyFilter(state.allTasks, filter, state.today)
        val grouped = groupByDate(filtered)

        _uiState.update {
            it.copy(
                selectedFilter = filter,
                filteredTasks = filtered,
                tasksByDate = grouped
            )
        }
    }

    fun toggleTaskCompletion(taskId: Long) {
        val task = _uiState.value.allTasks.find { it.id == taskId } ?: return

        viewModelScope.launch {
            if (task.isCompleted) {
                reviewTaskRepository.markAsIncomplete(taskId)
            } else {
                reviewTaskRepository.markAsCompleted(taskId)
            }
            IterioWidgetReceiver.sendDataChangedBroadcast(context)
        }
    }

    fun rescheduleTask(taskId: Long, newDate: LocalDate) {
        viewModelScope.launch {
            reviewTaskRepository.reschedule(taskId, newDate)
        }
    }

    private fun applyFilter(
        tasks: List<ReviewTask>,
        filter: ReviewFilter,
        today: LocalDate
    ): List<ReviewTask> = when (filter) {
        ReviewFilter.ALL -> tasks
        ReviewFilter.PENDING -> tasks.filter { !it.isCompleted && it.scheduledDate >= today }
        ReviewFilter.COMPLETED -> tasks.filter { it.isCompleted }
        ReviewFilter.OVERDUE -> tasks.filter { !it.isCompleted && it.scheduledDate < today }
    }

    private fun groupByDate(tasks: List<ReviewTask>): Map<LocalDate, List<ReviewTask>> =
        tasks
            .sortedBy { it.scheduledDate }
            .groupBy { it.scheduledDate }
            .toSortedMap()

    private fun calculateCounts(tasks: List<ReviewTask>, today: LocalDate): TaskCounts {
        val completed = tasks.count { it.isCompleted }
        val pending = tasks.count { !it.isCompleted && it.scheduledDate >= today }
        val overdue = tasks.count { !it.isCompleted && it.scheduledDate < today }
        return TaskCounts(
            total = tasks.size,
            pending = pending,
            completed = completed,
            overdue = overdue
        )
    }

    private data class TaskCounts(
        val total: Int,
        val pending: Int,
        val completed: Int,
        val overdue: Int
    )
}
