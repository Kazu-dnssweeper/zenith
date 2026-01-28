package com.iterio.app.data.repository

import app.cash.turbine.test
import com.google.gson.Gson
import com.iterio.app.data.local.dao.DailyStatsDao
import com.iterio.app.data.local.entity.DailyStatsEntity
import com.iterio.app.data.mapper.DailyStatsMapper
import com.iterio.app.domain.common.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

/**
 * DailyStatsRepositoryImpl のユニットテスト
 */
class DailyStatsRepositoryImplTest {

    private lateinit var dailyStatsDao: DailyStatsDao
    private lateinit var mapper: DailyStatsMapper
    private lateinit var repository: DailyStatsRepositoryImpl

    @Before
    fun setup() {
        dailyStatsDao = mockk()
        mapper = DailyStatsMapper(Gson())
        repository = DailyStatsRepositoryImpl(dailyStatsDao, mapper)
    }

    @Test
    fun `updateStats creates new entry when none exists`() = runTest {
        val date = LocalDate.now()
        coEvery { dailyStatsDao.getByDate(date) } returns null
        coEvery { dailyStatsDao.insert(any()) } returns Unit

        repository.updateStats(date, 25, "Math")

        coVerify { dailyStatsDao.insert(match {
            it.date == date &&
            it.totalStudyMinutes == 25 &&
            it.sessionCount == 1 &&
            it.subjectBreakdownJson.contains("Math")
        }) }
    }

    @Test
    fun `updateStats updates existing entry`() = runTest {
        val date = LocalDate.now()
        val existingEntity = createEntity(
            date = date,
            totalStudyMinutes = 50,
            sessionCount = 2,
            subjectBreakdownJson = """{"Math":50}"""
        )
        coEvery { dailyStatsDao.getByDate(date) } returns existingEntity
        coEvery { dailyStatsDao.update(any()) } returns Unit

        repository.updateStats(date, 25, "Math")

        coVerify { dailyStatsDao.update(match {
            it.totalStudyMinutes == 75 &&
            it.sessionCount == 3
        }) }
    }

    @Test
    fun `updateStats adds new subject to breakdown`() = runTest {
        val date = LocalDate.now()
        val existingEntity = createEntity(
            date = date,
            totalStudyMinutes = 30,
            sessionCount = 1,
            subjectBreakdownJson = """{"Math":30}"""
        )
        coEvery { dailyStatsDao.getByDate(date) } returns existingEntity
        coEvery { dailyStatsDao.update(any()) } returns Unit

        repository.updateStats(date, 20, "English")

        coVerify { dailyStatsDao.update(match {
            it.totalStudyMinutes == 50 &&
            it.subjectBreakdownJson.contains("English") &&
            it.subjectBreakdownJson.contains("Math")
        }) }
    }

    @Test
    fun `getByDate returns stats when exists`() = runTest {
        val date = LocalDate.now()
        val entity = createEntity(date = date, totalStudyMinutes = 60)
        coEvery { dailyStatsDao.getByDate(date) } returns entity

        val result = repository.getByDate(date)

        assertTrue(result.isSuccess)
        val stats = (result as Result.Success).value
        assertNotNull(stats)
        assertEquals(60, stats?.totalStudyMinutes)
    }

    @Test
    fun `getByDate returns null when not exists`() = runTest {
        val date = LocalDate.now()
        coEvery { dailyStatsDao.getByDate(date) } returns null

        val result = repository.getByDate(date)

        assertTrue(result.isSuccess)
        val stats = (result as Result.Success).value
        assertNull(stats)
    }

    @Test
    fun `getByDateFlow emits stats when exists`() = runTest {
        val date = LocalDate.now()
        val entity = createEntity(date = date, totalStudyMinutes = 45)
        every { dailyStatsDao.getByDateFlow(date) } returns flowOf(entity)

        repository.getByDateFlow(date).test {
            val stats = awaitItem()
            assertNotNull(stats)
            assertEquals(45, stats?.totalStudyMinutes)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getByDateFlow emits null when not exists`() = runTest {
        val date = LocalDate.now()
        every { dailyStatsDao.getByDateFlow(date) } returns flowOf(null)

        repository.getByDateFlow(date).test {
            val stats = awaitItem()
            assertNull(stats)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getStatsBetweenDates returns stats for range`() = runTest {
        val startDate = LocalDate.now().minusDays(7)
        val endDate = LocalDate.now()
        val entities = listOf(
            createEntity(date = startDate, totalStudyMinutes = 30),
            createEntity(date = startDate.plusDays(1), totalStudyMinutes = 45),
            createEntity(date = endDate, totalStudyMinutes = 60)
        )
        every { dailyStatsDao.getStatsBetweenDates(startDate, endDate) } returns flowOf(entities)

        repository.getStatsBetweenDates(startDate, endDate).test {
            val stats = awaitItem()
            assertEquals(3, stats.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getTotalMinutesBetweenDates returns correct sum`() = runTest {
        val startDate = LocalDate.now().minusDays(7)
        val endDate = LocalDate.now()
        coEvery { dailyStatsDao.getTotalMinutesBetweenDates(startDate, endDate) } returns 420

        val result = repository.getTotalMinutesBetweenDates(startDate, endDate)

        assertTrue(result.isSuccess)
        assertEquals(420, (result as Result.Success).value)
    }

    @Test
    fun `getTotalMinutesBetweenDates returns 0 for null`() = runTest {
        val startDate = LocalDate.now().minusDays(7)
        val endDate = LocalDate.now()
        coEvery { dailyStatsDao.getTotalMinutesBetweenDates(startDate, endDate) } returns null

        val result = repository.getTotalMinutesBetweenDates(startDate, endDate)

        assertTrue(result.isSuccess)
        assertEquals(0, (result as Result.Success).value)
    }

    @Test
    fun `getCurrentStreak returns correct streak`() = runTest {
        coEvery { dailyStatsDao.getCurrentStreak(any()) } returns 7

        val result = repository.getCurrentStreak()

        assertTrue(result.isSuccess)
        assertEquals(7, (result as Result.Success).value)
    }

    @Test
    fun `getCurrentStreak returns 0 for no streak`() = runTest {
        coEvery { dailyStatsDao.getCurrentStreak(any()) } returns 0

        val result = repository.getCurrentStreak()

        assertTrue(result.isSuccess)
        assertEquals(0, (result as Result.Success).value)
    }

    @Test
    fun `getMaxStreak returns max streak`() = runTest {
        coEvery { dailyStatsDao.getMaxStreak() } returns 30

        val result = repository.getMaxStreak()

        assertTrue(result.isSuccess)
        assertEquals(30, (result as Result.Success).value)
    }

    @Test
    fun `getMaxStreak returns 0 for null`() = runTest {
        coEvery { dailyStatsDao.getMaxStreak() } returns null

        val result = repository.getMaxStreak()

        assertTrue(result.isSuccess)
        assertEquals(0, (result as Result.Success).value)
    }

    @Test
    fun `getWeeklyData returns 7 days`() = runTest {
        val weekStart = LocalDate.now().minusDays(6)
        val weekEnd = weekStart.plusDays(6)
        val entities = (0..6).map { dayOffset ->
            createEntity(date = weekStart.plusDays(dayOffset.toLong()), totalStudyMinutes = 30 + dayOffset * 10)
        }
        coEvery { dailyStatsDao.getStatsByDateRange(weekStart, weekEnd) } returns entities

        val result = repository.getWeeklyData(weekStart)

        assertTrue(result.isSuccess)
        val data = (result as Result.Success).value
        assertEquals(7, data.size)
        assertEquals("月", data[0].dayOfWeek)
        assertEquals("日", data[6].dayOfWeek)
    }

    @Test
    fun `getWeeklyData handles missing days`() = runTest {
        val weekStart = LocalDate.now().minusDays(6)
        val weekEnd = weekStart.plusDays(6)
        // Only even days have data
        val entities = (0..6).filter { it % 2 == 0 }.map { dayOffset ->
            createEntity(date = weekStart.plusDays(dayOffset.toLong()), totalStudyMinutes = 60)
        }
        coEvery { dailyStatsDao.getStatsByDateRange(weekStart, weekEnd) } returns entities

        val result = repository.getWeeklyData(weekStart)

        assertTrue(result.isSuccess)
        val data = (result as Result.Success).value
        assertEquals(7, data.size)
        assertEquals(60, data[0].minutes)
        assertEquals(0, data[1].minutes)
        assertEquals(60, data[2].minutes)
    }

    @Test
    fun `getWeeklyData uses range query instead of individual queries`() = runTest {
        val weekStart = LocalDate.now().minusDays(6)
        val weekEnd = weekStart.plusDays(6)
        coEvery { dailyStatsDao.getStatsByDateRange(weekStart, weekEnd) } returns emptyList()

        repository.getWeeklyData(weekStart)

        coVerify(exactly = 1) { dailyStatsDao.getStatsByDateRange(weekStart, weekEnd) }
        coVerify(exactly = 0) { dailyStatsDao.getByDate(any()) }
    }

    @Test
    fun `getWeeklyData returns zero minutes for empty stats`() = runTest {
        val weekStart = LocalDate.now().minusDays(6)
        val weekEnd = weekStart.plusDays(6)
        coEvery { dailyStatsDao.getStatsByDateRange(weekStart, weekEnd) } returns emptyList()

        val result = repository.getWeeklyData(weekStart)

        assertTrue(result.isSuccess)
        val data = (result as Result.Success).value
        assertEquals(7, data.size)
        data.forEach { dayStats ->
            assertEquals(0, dayStats.minutes)
        }
    }

    // ==================== Helpers ====================

    private fun createEntity(
        date: LocalDate = LocalDate.now(),
        totalStudyMinutes: Int = 0,
        sessionCount: Int = 0,
        subjectBreakdownJson: String = "{}"
    ) = DailyStatsEntity(
        date = date,
        totalStudyMinutes = totalStudyMinutes,
        sessionCount = sessionCount,
        subjectBreakdownJson = subjectBreakdownJson
    )
}
