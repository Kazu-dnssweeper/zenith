package com.iterio.app.widget

import android.content.Context
import android.content.SharedPreferences
import com.iterio.app.data.local.IterioDatabase
import com.iterio.app.data.local.dao.DailyStatsDao
import com.iterio.app.data.local.dao.ReviewTaskDao
import com.iterio.app.data.local.dao.TaskDao
import com.iterio.app.data.local.dao.TaskWithGroupName
import com.iterio.app.data.local.entity.DailyStatsEntity
import com.iterio.app.service.TimerPhase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class IterioWidgetStateHelperTest {

    private lateinit var context: Context
    private lateinit var prefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var mockDatabase: IterioDatabase
    private lateinit var dailyStatsDao: DailyStatsDao
    private lateinit var reviewTaskDao: ReviewTaskDao
    private lateinit var taskDao: TaskDao

    @Before
    fun setup() {
        mockkObject(IterioWidgetStateHelper)

        context = mockk(relaxed = true)
        prefs = mockk(relaxed = true)
        editor = mockk(relaxed = true)

        every { context.getSharedPreferences("iterio_widget_timer_prefs", Context.MODE_PRIVATE) } returns prefs
        every { prefs.edit() } returns editor
        every { editor.putInt(any(), any()) } returns editor
        every { editor.putBoolean(any(), any()) } returns editor
        every { editor.putLong(any(), any()) } returns editor

        mockDatabase = mockk(relaxed = true)
        dailyStatsDao = mockk(relaxed = true)
        reviewTaskDao = mockk(relaxed = true)
        taskDao = mockk(relaxed = true)

        every { mockDatabase.dailyStatsDao() } returns dailyStatsDao
        every { mockDatabase.reviewTaskDao() } returns reviewTaskDao
        every { mockDatabase.taskDao() } returns taskDao

        IterioWidgetStateHelper.setDatabaseForTesting(mockDatabase)
        coEvery { IterioWidgetStateHelper.checkPremiumStatus(any()) } returns false
    }

    @After
    fun tearDown() {
        IterioWidgetStateHelper.setDatabaseForTesting(null)
        unmockkObject(IterioWidgetStateHelper)
    }

    // ========== Group A: saveTimerStateToPrefs ==========

    @Test
    fun `saveTimerStateToPrefs writes correct phase ordinal`() {
        IterioWidgetStateHelper.saveTimerStateToPrefs(context, TimerPhase.WORK, 1500, true)

        verify { editor.putInt("timer_phase", TimerPhase.WORK.ordinal) }
    }

    @Test
    fun `saveTimerStateToPrefs writes time remaining and isRunning`() {
        IterioWidgetStateHelper.saveTimerStateToPrefs(context, TimerPhase.WORK, 1500, true)

        verify { editor.putInt("time_remaining", 1500) }
        verify { editor.putBoolean("is_running", true) }
    }

    @Test
    fun `saveTimerStateToPrefs calls apply`() {
        IterioWidgetStateHelper.saveTimerStateToPrefs(context, TimerPhase.WORK, 1500, true)

        verify(exactly = 1) { editor.apply() }
    }

    // ========== Group B: getWidgetState — Room mock ==========

    @Test
    fun `getWidgetState returns study minutes from dao`() = runTest {
        val today = LocalDate.now()
        coEvery { dailyStatsDao.getByDate(today) } returns DailyStatsEntity(
            date = today,
            totalStudyMinutes = 120,
            sessionCount = 3
        )
        coEvery { dailyStatsDao.getCurrentStreak(today) } returns 0
        coEvery { reviewTaskDao.getPendingTaskCountForDate(today) } returns 0
        coEvery { taskDao.getTasksForDateWithGroup(any(), any()) } returns emptyList()
        setupDefaultTimerPrefs()

        val state = IterioWidgetStateHelper.getWidgetState(context)

        assertEquals(120, state.todayStudyMinutes)
    }

    @Test
    fun `getWidgetState returns streak from dao`() = runTest {
        val today = LocalDate.now()
        coEvery { dailyStatsDao.getByDate(today) } returns null
        coEvery { dailyStatsDao.getCurrentStreak(today) } returns 5
        coEvery { reviewTaskDao.getPendingTaskCountForDate(today) } returns 0
        coEvery { taskDao.getTasksForDateWithGroup(any(), any()) } returns emptyList()
        setupDefaultTimerPrefs()

        val state = IterioWidgetStateHelper.getWidgetState(context)

        assertEquals(5, state.currentStreak)
    }

    @Test
    fun `getWidgetState returns pending review count`() = runTest {
        val today = LocalDate.now()
        coEvery { dailyStatsDao.getByDate(today) } returns null
        coEvery { dailyStatsDao.getCurrentStreak(today) } returns 0
        coEvery { reviewTaskDao.getPendingTaskCountForDate(today) } returns 3
        coEvery { taskDao.getTasksForDateWithGroup(any(), any()) } returns emptyList()
        setupDefaultTimerPrefs()

        val state = IterioWidgetStateHelper.getWidgetState(context)

        assertEquals(3, state.pendingReviewCount)
    }

    @Test
    fun `getWidgetState returns today tasks from TaskDao`() = runTest {
        val today = LocalDate.now()
        coEvery { dailyStatsDao.getByDate(today) } returns null
        coEvery { dailyStatsDao.getCurrentStreak(today) } returns 0
        coEvery { reviewTaskDao.getPendingTaskCountForDate(today) } returns 0
        coEvery { taskDao.getTasksForDateWithGroup(any(), any()) } returns listOf(
            TaskWithGroupName(id = 1L, name = "Math", groupName = "Science"),
            TaskWithGroupName(id = 2L, name = "English", groupName = "Language")
        )
        setupDefaultTimerPrefs()

        val state = IterioWidgetStateHelper.getWidgetState(context)

        assertEquals(2, state.todayTasks.size)
        assertEquals("Math", state.todayTasks[0].name)
        assertEquals("Science", state.todayTasks[0].groupName)
        assertEquals("English", state.todayTasks[1].name)
        assertEquals("Language", state.todayTasks[1].groupName)
    }

    @Test
    fun `getWidgetState returns empty tasks when TaskDao throws`() = runTest {
        val today = LocalDate.now()
        coEvery { dailyStatsDao.getByDate(today) } returns null
        coEvery { dailyStatsDao.getCurrentStreak(today) } returns 0
        coEvery { reviewTaskDao.getPendingTaskCountForDate(today) } returns 0
        coEvery { taskDao.getTasksForDateWithGroup(any(), any()) } throws RuntimeException("DB error")
        setupDefaultTimerPrefs()

        val state = IterioWidgetStateHelper.getWidgetState(context)

        assertTrue(state.todayTasks.isEmpty())
    }

    @Test
    fun `getWidgetState returns zero reviews when ReviewTaskDao throws`() = runTest {
        val today = LocalDate.now()
        coEvery { dailyStatsDao.getByDate(today) } returns null
        coEvery { dailyStatsDao.getCurrentStreak(today) } returns 0
        coEvery { reviewTaskDao.getPendingTaskCountForDate(today) } throws RuntimeException("DB error")
        coEvery { taskDao.getTasksForDateWithGroup(any(), any()) } returns emptyList()
        setupDefaultTimerPrefs()

        val state = IterioWidgetStateHelper.getWidgetState(context)

        assertEquals(0, state.pendingReviewCount)
    }

    @Test
    fun `getWidgetState returns zero minutes for null stats`() = runTest {
        val today = LocalDate.now()
        coEvery { dailyStatsDao.getByDate(today) } returns null
        coEvery { dailyStatsDao.getCurrentStreak(today) } returns 0
        coEvery { reviewTaskDao.getPendingTaskCountForDate(today) } returns 0
        coEvery { taskDao.getTasksForDateWithGroup(any(), any()) } returns emptyList()
        setupDefaultTimerPrefs()

        val state = IterioWidgetStateHelper.getWidgetState(context)

        assertEquals(0, state.todayStudyMinutes)
    }

    // ========== Group C: Timer state (SharedPreferences) ==========

    @Test
    fun `getWidgetState reads timer phase from prefs`() = runTest {
        val today = LocalDate.now()
        setupDaoDefaults(today)
        every { prefs.getInt("timer_phase", TimerPhase.IDLE.ordinal) } returns TimerPhase.WORK.ordinal
        every { prefs.getInt("time_remaining", 0) } returns 300
        every { prefs.getBoolean("is_running", false) } returns true
        every { prefs.getLong("last_updated_at", 0L) } returns System.currentTimeMillis()

        val state = IterioWidgetStateHelper.getWidgetState(context)

        assertEquals(TimerPhase.WORK, state.timerPhase)
    }

    @Test
    fun `getWidgetState reads time remaining from prefs`() = runTest {
        val today = LocalDate.now()
        setupDaoDefaults(today)
        every { prefs.getInt("timer_phase", TimerPhase.IDLE.ordinal) } returns TimerPhase.WORK.ordinal
        every { prefs.getInt("time_remaining", 0) } returns 300
        every { prefs.getBoolean("is_running", false) } returns false
        every { prefs.getLong("last_updated_at", 0L) } returns 0L

        val state = IterioWidgetStateHelper.getWidgetState(context)

        assertEquals(300, state.timeRemainingSeconds)
    }

    @Test
    fun `getWidgetState reads isRunning from prefs`() = runTest {
        val today = LocalDate.now()
        setupDaoDefaults(today)
        every { prefs.getInt("timer_phase", TimerPhase.IDLE.ordinal) } returns TimerPhase.WORK.ordinal
        every { prefs.getInt("time_remaining", 0) } returns 300
        every { prefs.getBoolean("is_running", false) } returns true
        every { prefs.getLong("last_updated_at", 0L) } returns System.currentTimeMillis()

        val state = IterioWidgetStateHelper.getWidgetState(context)

        assertTrue(state.isTimerRunning)
    }

    @Test
    fun `getWidgetState defaults to IDLE on invalid phase ordinal`() = runTest {
        val today = LocalDate.now()
        setupDaoDefaults(today)
        every { prefs.getInt("timer_phase", TimerPhase.IDLE.ordinal) } returns 99
        every { prefs.getInt("time_remaining", 0) } returns 0
        every { prefs.getBoolean("is_running", false) } returns false
        every { prefs.getLong("last_updated_at", 0L) } returns 0L

        val state = IterioWidgetStateHelper.getWidgetState(context)

        assertEquals(TimerPhase.IDLE, state.timerPhase)
    }

    // ========== Group D: Staleness detection ==========

    @Test
    fun `getWidgetState returns IDLE when timer state is stale`() = runTest {
        val today = LocalDate.now()
        setupDaoDefaults(today)
        val twoMinutesAgo = System.currentTimeMillis() - 120_000L
        every { prefs.getInt("timer_phase", TimerPhase.IDLE.ordinal) } returns TimerPhase.WORK.ordinal
        every { prefs.getInt("time_remaining", 0) } returns 300
        every { prefs.getBoolean("is_running", false) } returns true
        every { prefs.getLong("last_updated_at", 0L) } returns twoMinutesAgo

        val state = IterioWidgetStateHelper.getWidgetState(context)

        assertEquals(TimerPhase.IDLE, state.timerPhase)
        assertFalse(state.isTimerRunning)
    }

    @Test
    fun `getWidgetState preserves running state when not stale`() = runTest {
        val today = LocalDate.now()
        setupDaoDefaults(today)
        val fiveSecondsAgo = System.currentTimeMillis() - 5_000L
        every { prefs.getInt("timer_phase", TimerPhase.IDLE.ordinal) } returns TimerPhase.WORK.ordinal
        every { prefs.getInt("time_remaining", 0) } returns 300
        every { prefs.getBoolean("is_running", false) } returns true
        every { prefs.getLong("last_updated_at", 0L) } returns fiveSecondsAgo

        val state = IterioWidgetStateHelper.getWidgetState(context)

        assertEquals(TimerPhase.WORK, state.timerPhase)
        assertTrue(state.isTimerRunning)
    }

    // ========== Group E: Premium + error ==========

    @Test
    fun `getWidgetState returns isPremium true`() = runTest {
        val today = LocalDate.now()
        setupDaoDefaults(today)
        setupDefaultTimerPrefs()
        coEvery { IterioWidgetStateHelper.checkPremiumStatus(any()) } returns true

        val state = IterioWidgetStateHelper.getWidgetState(context)

        assertTrue(state.isPremium)
    }

    @Test
    fun `getWidgetState returns default state on database exception`() = runTest {
        setupDefaultTimerPrefs()
        every { mockDatabase.dailyStatsDao() } throws RuntimeException("DB crashed")

        val state = IterioWidgetStateHelper.getWidgetState(context)

        assertEquals(WidgetState(), state)
    }

    // ========== Group F: closeDatabase ==========

    @Test
    fun `closeDatabase calls close on database`() {
        IterioWidgetStateHelper.setDatabaseForTesting(mockDatabase)

        IterioWidgetStateHelper.closeDatabase()

        verify { mockDatabase.close() }
    }

    @Test
    fun `closeDatabase handles null database gracefully`() {
        IterioWidgetStateHelper.setDatabaseForTesting(null)

        // Should not throw
        IterioWidgetStateHelper.closeDatabase()
    }

    // ========== Helpers ==========

    private fun setupDefaultTimerPrefs() {
        every { prefs.getInt("timer_phase", TimerPhase.IDLE.ordinal) } returns TimerPhase.IDLE.ordinal
        every { prefs.getInt("time_remaining", 0) } returns 0
        every { prefs.getBoolean("is_running", false) } returns false
        every { prefs.getLong("last_updated_at", 0L) } returns 0L
    }

    private fun setupDaoDefaults(today: LocalDate) {
        coEvery { dailyStatsDao.getByDate(today) } returns null
        coEvery { dailyStatsDao.getCurrentStreak(today) } returns 0
        coEvery { reviewTaskDao.getPendingTaskCountForDate(today) } returns 0
        coEvery { taskDao.getTasksForDateWithGroup(any(), any()) } returns emptyList()
    }
}
