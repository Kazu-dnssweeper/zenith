package com.iterio.app.fakes

import com.iterio.app.domain.model.DailyStats
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

/**
 * FakeDailyStatsRepository のテスト
 */
class FakeDailyStatsRepositoryTest {

    private lateinit var repository: FakeDailyStatsRepository

    @Before
    fun setup() {
        repository = FakeDailyStatsRepository()
    }

    // Update Stats Tests

    @Test
    fun `updateStats creates new entry for new date`() = runTest {
        val today = LocalDate.now()

        repository.updateStats(today, studyMinutes = 25, subjectName = "Math")

        val stats = repository.getByDate(today)
        assertNotNull(stats)
        assertEquals(25, stats?.totalStudyMinutes)
    }

    @Test
    fun `updateStats adds to existing minutes`() = runTest {
        val today = LocalDate.now()

        repository.updateStats(today, studyMinutes = 25, subjectName = "Math")
        repository.updateStats(today, studyMinutes = 30, subjectName = "Math")

        val stats = repository.getByDate(today)
        assertEquals(55, stats?.totalStudyMinutes)
    }

    @Test
    fun `updateStats tracks different subjects`() = runTest {
        val today = LocalDate.now()

        repository.updateStats(today, studyMinutes = 25, subjectName = "Math")
        repository.updateStats(today, studyMinutes = 30, subjectName = "English")

        val stats = repository.getByDate(today)
        assertEquals(55, stats?.totalStudyMinutes)
        assertEquals(2, stats?.subjectBreakdown?.size)
    }

    // Get Tests

    @Test
    fun `getByDate returns null for non-existent date`() = runTest {
        val result = repository.getByDate(LocalDate.now())
        assertNull(result)
    }

    @Test
    fun `getByDateFlow emits updates`() = runTest {
        val today = LocalDate.now()

        val initial = repository.getByDateFlow(today).first()
        assertNull(initial)

        repository.updateStats(today, studyMinutes = 25, subjectName = "Math")

        val updated = repository.getByDateFlow(today).first()
        assertNotNull(updated)
        assertEquals(25, updated?.totalStudyMinutes)
    }

    @Test
    fun `getStatsBetweenDates returns stats in date range`() = runTest {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val twoDaysAgo = today.minusDays(2)
        val threeDaysAgo = today.minusDays(3)

        repository.updateStats(threeDaysAgo, studyMinutes = 10, subjectName = "A")
        repository.updateStats(twoDaysAgo, studyMinutes = 20, subjectName = "B")
        repository.updateStats(yesterday, studyMinutes = 30, subjectName = "C")
        repository.updateStats(today, studyMinutes = 40, subjectName = "D")

        val stats = repository.getStatsBetweenDates(twoDaysAgo, today).first()

        assertEquals(3, stats.size)
    }

    @Test
    fun `getTotalMinutesBetweenDates returns sum`() = runTest {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val twoDaysAgo = today.minusDays(2)

        repository.updateStats(twoDaysAgo, studyMinutes = 10, subjectName = "A")
        repository.updateStats(yesterday, studyMinutes = 20, subjectName = "B")
        repository.updateStats(today, studyMinutes = 30, subjectName = "C")

        val total = repository.getTotalMinutesBetweenDates(twoDaysAgo, today)

        assertEquals(60, total)
    }

    // Streak Tests

    @Test
    fun `getCurrentStreak returns 0 when no data`() = runTest {
        val streak = repository.getCurrentStreak()
        assertEquals(0, streak)
    }

    @Test
    fun `getCurrentStreak returns 1 for today only`() = runTest {
        val today = LocalDate.now()
        repository.updateStats(today, studyMinutes = 25, subjectName = "Math")

        val streak = repository.getCurrentStreak()
        assertEquals(1, streak)
    }

    @Test
    fun `getCurrentStreak counts consecutive days`() = runTest {
        val today = LocalDate.now()

        repository.updateStats(today, studyMinutes = 25, subjectName = "Math")
        repository.updateStats(today.minusDays(1), studyMinutes = 25, subjectName = "Math")
        repository.updateStats(today.minusDays(2), studyMinutes = 25, subjectName = "Math")

        val streak = repository.getCurrentStreak()
        assertEquals(3, streak)
    }

    @Test
    fun `getCurrentStreak breaks on gap`() = runTest {
        val today = LocalDate.now()

        repository.updateStats(today, studyMinutes = 25, subjectName = "Math")
        repository.updateStats(today.minusDays(1), studyMinutes = 25, subjectName = "Math")
        // Gap on minusDays(2)
        repository.updateStats(today.minusDays(3), studyMinutes = 25, subjectName = "Math")

        val streak = repository.getCurrentStreak()
        assertEquals(2, streak)
    }

    @Test
    fun `getMaxStreak returns 0 when no data`() = runTest {
        val streak = repository.getMaxStreak()
        assertEquals(0, streak)
    }

    @Test
    fun `getMaxStreak returns longest streak`() = runTest {
        val today = LocalDate.now()

        // First streak: 2 days
        repository.updateStats(today.minusDays(10), studyMinutes = 25, subjectName = "A")
        repository.updateStats(today.minusDays(9), studyMinutes = 25, subjectName = "A")
        // Gap
        // Second streak: 4 days (longest)
        repository.updateStats(today.minusDays(6), studyMinutes = 25, subjectName = "A")
        repository.updateStats(today.minusDays(5), studyMinutes = 25, subjectName = "A")
        repository.updateStats(today.minusDays(4), studyMinutes = 25, subjectName = "A")
        repository.updateStats(today.minusDays(3), studyMinutes = 25, subjectName = "A")
        // Gap
        // Current streak: 2 days
        repository.updateStats(today.minusDays(1), studyMinutes = 25, subjectName = "A")
        repository.updateStats(today, studyMinutes = 25, subjectName = "A")

        val maxStreak = repository.getMaxStreak()
        assertEquals(4, maxStreak)
    }

    // Weekly Data Tests

    @Test
    fun `getWeeklyData returns 7 days starting from weekStart`() = runTest {
        val weekStart = LocalDate.of(2026, 1, 19) // Monday

        repository.updateStats(weekStart, studyMinutes = 30, subjectName = "Math")
        repository.updateStats(weekStart.plusDays(2), studyMinutes = 45, subjectName = "English")

        val weeklyData = repository.getWeeklyData(weekStart)

        assertEquals(7, weeklyData.size)
        assertEquals(30, weeklyData[0].minutes) // Monday
        assertEquals(0, weeklyData[1].minutes)   // Tuesday
        assertEquals(45, weeklyData[2].minutes)  // Wednesday
    }

    @Test
    fun `getWeeklyData has correct day labels`() = runTest {
        val weekStart = LocalDate.of(2026, 1, 19) // Monday

        val weeklyData = repository.getWeeklyData(weekStart)

        assertEquals("月", weeklyData[0].dayOfWeek)
        assertEquals("火", weeklyData[1].dayOfWeek)
        assertEquals("水", weeklyData[2].dayOfWeek)
        assertEquals("木", weeklyData[3].dayOfWeek)
        assertEquals("金", weeklyData[4].dayOfWeek)
        assertEquals("土", weeklyData[5].dayOfWeek)
        assertEquals("日", weeklyData[6].dayOfWeek)
    }
}
