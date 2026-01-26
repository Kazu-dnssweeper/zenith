package com.iterio.app.fakes

import com.iterio.app.domain.common.DomainError
import com.iterio.app.domain.common.Result
import com.iterio.app.domain.model.DailyStats
import com.iterio.app.domain.repository.DailyStatsRepository
import com.iterio.app.domain.repository.DayStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * テスト用の DailyStatsRepository 実装
 */
class FakeDailyStatsRepository : DailyStatsRepository {

    private val stats = MutableStateFlow<Map<LocalDate, DailyStats>>(emptyMap())

    override suspend fun updateStats(date: LocalDate, studyMinutes: Int, subjectName: String): Result<Unit, DomainError> {
        val existing = stats.value[date]
        val newStats = if (existing != null) {
            val updatedBreakdown = existing.subjectBreakdown.toMutableMap()
            updatedBreakdown[subjectName] = (updatedBreakdown[subjectName] ?: 0) + studyMinutes
            existing.copy(
                totalStudyMinutes = existing.totalStudyMinutes + studyMinutes,
                sessionCount = existing.sessionCount + 1,
                subjectBreakdown = updatedBreakdown
            )
        } else {
            DailyStats(
                date = date,
                totalStudyMinutes = studyMinutes,
                sessionCount = 1,
                subjectBreakdown = mapOf(subjectName to studyMinutes)
            )
        }
        stats.value = stats.value + (date to newStats)
        return Result.Success(Unit)
    }

    override suspend fun getByDate(date: LocalDate): Result<DailyStats?, DomainError> =
        Result.Success(stats.value[date])

    override fun getByDateFlow(date: LocalDate): Flow<DailyStats?> =
        stats.map { it[date] }

    override fun getStatsBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<DailyStats>> =
        stats.map { map ->
            map.values.filter {
                it.date >= startDate && it.date <= endDate
            }.sortedBy { it.date }
        }

    override suspend fun getTotalMinutesBetweenDates(startDate: LocalDate, endDate: LocalDate): Result<Int, DomainError> =
        Result.Success(
            stats.value.values
                .filter { it.date >= startDate && it.date <= endDate }
                .sumOf { it.totalStudyMinutes }
        )

    override suspend fun getCurrentStreak(): Result<Int, DomainError> {
        if (stats.value.isEmpty()) return Result.Success(0)

        var streak = 0
        var currentDate = LocalDate.now()

        // If today has no study, check if yesterday does (might be end of day)
        if (stats.value[currentDate] == null) {
            currentDate = currentDate.minusDays(1)
        }

        while (stats.value[currentDate] != null) {
            streak++
            currentDate = currentDate.minusDays(1)
        }

        return Result.Success(streak)
    }

    override suspend fun getMaxStreak(): Result<Int, DomainError> {
        if (stats.value.isEmpty()) return Result.Success(0)

        val sortedDates = stats.value.keys.sorted()
        if (sortedDates.isEmpty()) return Result.Success(0)

        var maxStreak = 1
        var currentStreak = 1

        for (i in 1 until sortedDates.size) {
            if (sortedDates[i] == sortedDates[i - 1].plusDays(1)) {
                currentStreak++
                maxStreak = maxOf(maxStreak, currentStreak)
            } else {
                currentStreak = 1
            }
        }

        return Result.Success(maxStreak)
    }

    override suspend fun getWeeklyData(weekStart: LocalDate): Result<List<DayStats>, DomainError> {
        val dayLabels = listOf("月", "火", "水", "木", "金", "土", "日")
        val weeklyData = (0 until 7).map { offset ->
            val date = weekStart.plusDays(offset.toLong())
            val dailyStats = stats.value[date]
            DayStats(
                dayOfWeek = dayLabels[offset],
                date = date,
                minutes = dailyStats?.totalStudyMinutes ?: 0
            )
        }
        return Result.Success(weeklyData)
    }

    // Test helpers
    fun clear() {
        stats.value = emptyMap()
    }
}
