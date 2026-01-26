package com.iterio.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iterio.app.domain.model.ReviewTask
import com.iterio.app.domain.model.Task
import com.iterio.app.domain.repository.DailyStatsRepository
import com.iterio.app.domain.repository.DayStats
import com.iterio.app.domain.repository.ReviewTaskRepository
import com.iterio.app.domain.repository.StudySessionRepository
import com.iterio.app.domain.repository.TaskRepository
import com.iterio.app.domain.usecase.GetTodayTasksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val todayMinutes: Int = 0,
    val todayCycles: Int = 0,
    val currentStreak: Int = 0,
    val todayScheduledTasks: List<Task> = emptyList(),
    val todayReviewTasks: List<ReviewTask> = emptyList(),
    val weeklyData: List<DayStats> = emptyList(),
    val upcomingDeadlineTasks: List<Task> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val studySessionRepository: StudySessionRepository,
    private val dailyStatsRepository: DailyStatsRepository,
    private val reviewTaskRepository: ReviewTaskRepository,
    private val getTodayTasksUseCase: GetTodayTasksUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val today = LocalDate.now()

            // Load today's stats
            launch {
                val minutesResult = studySessionRepository.getTotalMinutesForDay(today)
                val cyclesResult = studySessionRepository.getTotalCyclesForDay(today)
                _uiState.update {
                    it.copy(
                        todayMinutes = minutesResult.getOrDefault(0),
                        todayCycles = cyclesResult.getOrDefault(0)
                    )
                }
            }

            // Load current streak
            launch {
                dailyStatsRepository.getCurrentStreak()
                    .onSuccess { streak ->
                        _uiState.update { it.copy(currentStreak = streak) }
                    }
            }

            // Load today's scheduled tasks and review tasks using UseCase
            launch {
                getTodayTasksUseCase(today).collect { result ->
                    _uiState.update {
                        it.copy(
                            todayScheduledTasks = result.scheduledTasks,
                            todayReviewTasks = result.reviewTasks,
                            isLoading = false
                        )
                    }
                }
            }

            // Load weekly data
            launch {
                val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                dailyStatsRepository.getWeeklyData(weekStart)
                    .onSuccess { weeklyData ->
                        _uiState.update { it.copy(weeklyData = weeklyData) }
                    }
            }

            // Load upcoming deadline tasks (next 7 days)
            launch {
                val endDate = today.plusDays(7)
                taskRepository.getUpcomingDeadlineTasks(today, endDate).collect { tasks ->
                    _uiState.update { it.copy(upcomingDeadlineTasks = tasks) }
                }
            }
        }
    }

    fun toggleReviewTaskComplete(taskId: Long, complete: Boolean) {
        viewModelScope.launch {
            if (complete) {
                reviewTaskRepository.markAsCompleted(taskId)
            } else {
                reviewTaskRepository.markAsIncomplete(taskId)
            }
        }
    }
}
