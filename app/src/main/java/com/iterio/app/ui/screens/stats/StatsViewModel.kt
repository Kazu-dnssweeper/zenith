package com.iterio.app.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iterio.app.domain.model.SubscriptionStatus
import com.iterio.app.domain.repository.DailyStatsRepository
import com.iterio.app.domain.repository.StudySessionRepository
import com.iterio.app.ui.premium.PremiumManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

data class DayStats(
    val dayOfWeek: String,
    val minutes: Int
)

data class StatsUiState(
    val isLoading: Boolean = true,
    val todayMinutes: Int = 0,
    val todaySessions: Int = 0,
    val totalSessions: Int = 0,
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val thisWeekMinutes: Int = 0,
    val thisMonthMinutes: Int = 0,
    val averageDailyMinutes: Int = 0,
    val weeklyData: List<DayStats> = emptyList()
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val dailyStatsRepository: DailyStatsRepository,
    private val studySessionRepository: StudySessionRepository,
    private val premiumManager: PremiumManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    val subscriptionStatus: StateFlow<SubscriptionStatus> = premiumManager.subscriptionStatus
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SubscriptionStatus()
        )

    val isPremium: StateFlow<Boolean> = subscriptionStatus
        .map { it.isPremium }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val today = LocalDate.now()
            val weekStart = today.with(DayOfWeek.MONDAY)
            val monthStart = today.withDayOfMonth(1)
            val thirtyDaysAgo = today.minusDays(30)

            coroutineScope {
                val todayMinutesDeferred = async { studySessionRepository.getTotalMinutesForDay(today).getOrDefault(0) }
                val todaySessionsDeferred = async { studySessionRepository.getTotalCyclesForDay(today).getOrDefault(0) }
                val currentStreakDeferred = async { dailyStatsRepository.getCurrentStreak().getOrDefault(0) }
                val maxStreakDeferred = async { dailyStatsRepository.getMaxStreak().getOrDefault(0) }
                val thisWeekMinutesDeferred = async { dailyStatsRepository.getTotalMinutesBetweenDates(weekStart, today).getOrDefault(0) }
                val thisMonthMinutesDeferred = async { dailyStatsRepository.getTotalMinutesBetweenDates(monthStart, today).getOrDefault(0) }
                val totalSessionsDeferred = async { studySessionRepository.getSessionCount().getOrDefault(0) }
                val last30DaysMinutesDeferred = async { dailyStatsRepository.getTotalMinutesBetweenDates(thirtyDaysAgo, today).getOrDefault(0) }
                val weeklyDataDeferred = async { dailyStatsRepository.getWeeklyData(weekStart).getOrDefault(emptyList()) }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        todayMinutes = todayMinutesDeferred.await(),
                        todaySessions = todaySessionsDeferred.await(),
                        totalSessions = totalSessionsDeferred.await(),
                        currentStreak = currentStreakDeferred.await(),
                        maxStreak = maxStreakDeferred.await(),
                        thisWeekMinutes = thisWeekMinutesDeferred.await(),
                        thisMonthMinutes = thisMonthMinutesDeferred.await(),
                        averageDailyMinutes = last30DaysMinutesDeferred.await() / 30,
                        weeklyData = weeklyDataDeferred.await().map { ds ->
                            DayStats(dayOfWeek = ds.dayOfWeek, minutes = ds.minutes)
                        }
                    )
                }
            }
        }
    }

    fun refresh() {
        loadStats()
    }

    fun startTrial() {
        viewModelScope.launch {
            premiumManager.startTrial()
        }
    }
}
