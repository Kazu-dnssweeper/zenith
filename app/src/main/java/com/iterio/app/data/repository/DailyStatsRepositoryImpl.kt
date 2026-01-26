package com.iterio.app.data.repository

import com.google.gson.Gson
import com.iterio.app.data.local.dao.DailyStatsDao
import com.iterio.app.data.local.entity.DailyStatsEntity
import com.iterio.app.data.mapper.DailyStatsMapper
import com.iterio.app.domain.model.DailyStats
import com.iterio.app.domain.repository.DailyStatsRepository
import com.iterio.app.domain.repository.DayStats
import com.iterio.app.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class DailyStatsRepositoryImpl @Inject constructor(
    private val dailyStatsDao: DailyStatsDao,
    private val mapper: DailyStatsMapper
) : DailyStatsRepository {

    private val gson = Gson()

    override suspend fun updateStats(date: LocalDate, studyMinutes: Int, subjectName: String) {
        val existingStats = dailyStatsDao.getByDate(date)

        if (existingStats != null) {
            val existingDomain = mapper.toDomain(existingStats)
            val breakdown = existingDomain.subjectBreakdown.toMutableMap()
            breakdown[subjectName] = (breakdown[subjectName] ?: 0) + studyMinutes

            val updatedDomain = existingDomain.copy(
                totalStudyMinutes = existingDomain.totalStudyMinutes + studyMinutes,
                sessionCount = existingDomain.sessionCount + 1,
                subjectBreakdown = breakdown
            )
            dailyStatsDao.update(mapper.toEntity(updatedDomain))
        } else {
            val breakdown = mapOf(subjectName to studyMinutes)
            dailyStatsDao.insert(
                DailyStatsEntity(
                    date = date,
                    totalStudyMinutes = studyMinutes,
                    sessionCount = 1,
                    subjectBreakdownJson = gson.toJson(breakdown)
                )
            )
        }
    }

    override suspend fun getByDate(date: LocalDate): DailyStats? {
        return dailyStatsDao.getByDate(date)?.let { mapper.toDomain(it) }
    }

    override fun getByDateFlow(date: LocalDate): Flow<DailyStats?> {
        return dailyStatsDao.getByDateFlow(date).map { it?.let { mapper.toDomain(it) } }
    }

    override fun getStatsBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<DailyStats>> {
        return dailyStatsDao.getStatsBetweenDates(startDate, endDate).map { entities ->
            mapper.toDomainList(entities)
        }
    }

    override suspend fun getTotalMinutesBetweenDates(startDate: LocalDate, endDate: LocalDate): Int {
        return dailyStatsDao.getTotalMinutesBetweenDates(startDate, endDate) ?: 0
    }

    override suspend fun getCurrentStreak(): Int {
        return dailyStatsDao.getCurrentStreak(LocalDate.now())
    }

    override suspend fun getMaxStreak(): Int {
        return dailyStatsDao.getMaxStreak() ?: 0
    }

    override suspend fun getWeeklyData(weekStart: LocalDate): List<DayStats> {
        return (0..6).map { dayOffset ->
            val date = weekStart.plusDays(dayOffset.toLong())
            val stats = dailyStatsDao.getByDate(date)
            val minutes = stats?.totalStudyMinutes ?: 0

            DayStats(
                dayOfWeek = DateUtils.WEEKDAY_LABELS[dayOffset],
                date = date,
                minutes = minutes
            )
        }
    }
}
