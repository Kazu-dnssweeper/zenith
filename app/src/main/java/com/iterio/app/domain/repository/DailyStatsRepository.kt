package com.iterio.app.domain.repository

import com.iterio.app.domain.common.DomainError
import com.iterio.app.domain.common.Result
import com.iterio.app.domain.model.DailyStats
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

data class DayStats(
    val dayOfWeek: String,  // "月", "火", ...
    val date: LocalDate,
    val minutes: Int
)

interface DailyStatsRepository {
    suspend fun updateStats(date: LocalDate, studyMinutes: Int, subjectName: String): Result<Unit, DomainError>
    suspend fun getByDate(date: LocalDate): Result<DailyStats?, DomainError>
    fun getByDateFlow(date: LocalDate): Flow<DailyStats?>
    fun getStatsBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<DailyStats>>
    suspend fun getTotalMinutesBetweenDates(startDate: LocalDate, endDate: LocalDate): Result<Int, DomainError>
    suspend fun getCurrentStreak(): Result<Int, DomainError>
    suspend fun getMaxStreak(): Result<Int, DomainError>

    /**
     * 週間データ（月曜〜日曜）を取得
     */
    suspend fun getWeeklyData(weekStart: LocalDate): Result<List<DayStats>, DomainError>
}
