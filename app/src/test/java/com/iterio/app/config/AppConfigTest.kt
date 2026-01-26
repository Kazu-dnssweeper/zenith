package com.iterio.app.config

import org.junit.Assert.*
import org.junit.Test

/**
 * AppConfig のテスト
 * 設定値が正しく定義されていることを確認
 */
class AppConfigTest {

    // Timer Constants Tests
    @Test
    fun `Timer DEFAULT_WORK_MINUTES is 25`() {
        assertEquals(25, AppConfig.Timer.DEFAULT_WORK_MINUTES)
    }

    @Test
    fun `Timer DEFAULT_SHORT_BREAK_MINUTES is 5`() {
        assertEquals(5, AppConfig.Timer.DEFAULT_SHORT_BREAK_MINUTES)
    }

    @Test
    fun `Timer DEFAULT_LONG_BREAK_MINUTES is 15`() {
        assertEquals(15, AppConfig.Timer.DEFAULT_LONG_BREAK_MINUTES)
    }

    @Test
    fun `Timer DEFAULT_CYCLES is 4`() {
        assertEquals(4, AppConfig.Timer.DEFAULT_CYCLES)
    }

    @Test
    fun `Timer MIN_WORK_MINUTES is 1`() {
        assertEquals(1, AppConfig.Timer.MIN_WORK_MINUTES)
    }

    @Test
    fun `Timer MAX_WORK_MINUTES is 120`() {
        assertEquals(120, AppConfig.Timer.MAX_WORK_MINUTES)
    }

    @Test
    fun `Timer MIN_BREAK_MINUTES is 1`() {
        assertEquals(1, AppConfig.Timer.MIN_BREAK_MINUTES)
    }

    @Test
    fun `Timer MAX_BREAK_MINUTES is 60`() {
        assertEquals(60, AppConfig.Timer.MAX_BREAK_MINUTES)
    }

    @Test
    fun `Timer MIN_CYCLES is 1`() {
        assertEquals(1, AppConfig.Timer.MIN_CYCLES)
    }

    @Test
    fun `Timer MAX_CYCLES is 10`() {
        assertEquals(10, AppConfig.Timer.MAX_CYCLES)
    }

    // Premium Constants Tests
    @Test
    fun `Premium PREMIUM_REVIEW_INTERVALS contains correct values`() {
        val expected = listOf(1, 3, 7, 14, 30, 60)
        assertEquals(expected, AppConfig.Premium.PREMIUM_REVIEW_INTERVALS)
    }

    @Test
    fun `Premium FREE_REVIEW_INTERVALS contains correct values`() {
        val expected = listOf(1, 3)
        assertEquals(expected, AppConfig.Premium.FREE_REVIEW_INTERVALS)
    }

    @Test
    fun `Premium TRIAL_DURATION_DAYS is 3`() {
        assertEquals(3L, AppConfig.Premium.TRIAL_DURATION_DAYS)
    }

    @Test
    fun `Premium FREE_REVIEW_INTERVALS is subset of PREMIUM_REVIEW_INTERVALS`() {
        val freeIntervals = AppConfig.Premium.FREE_REVIEW_INTERVALS
        val premiumIntervals = AppConfig.Premium.PREMIUM_REVIEW_INTERVALS
        assertTrue(premiumIntervals.containsAll(freeIntervals))
    }

    // Daily Goal Constants Tests
    @Test
    fun `DailyGoal DEFAULT_MINUTES is 60`() {
        assertEquals(60, AppConfig.DailyGoal.DEFAULT_MINUTES)
    }

    @Test
    fun `DailyGoal MIN_MINUTES is 15`() {
        assertEquals(15, AppConfig.DailyGoal.MIN_MINUTES)
    }

    @Test
    fun `DailyGoal MAX_MINUTES is 480`() {
        assertEquals(480, AppConfig.DailyGoal.MAX_MINUTES)
    }

    @Test
    fun `DailyGoal STEP_MINUTES is 15`() {
        assertEquals(15, AppConfig.DailyGoal.STEP_MINUTES)
    }

    // UI Constants Tests
    @Test
    fun `UI SEARCH_DEBOUNCE_MS is 300`() {
        assertEquals(300L, AppConfig.UI.SEARCH_DEBOUNCE_MS)
    }

    @Test
    fun `UI ANIMATION_DURATION_MS is 300`() {
        assertEquals(300L, AppConfig.UI.ANIMATION_DURATION_MS)
    }

    @Test
    fun `UI SNACKBAR_DURATION_MS is 3000`() {
        assertEquals(3000L, AppConfig.UI.SNACKBAR_DURATION_MS)
    }

    // Backup Constants Tests
    @Test
    fun `Backup FILE_NAME_PREFIX is iterio_backup`() {
        assertEquals("iterio_backup", AppConfig.Backup.FILE_NAME_PREFIX)
    }

    @Test
    fun `Backup CLOUD_FOLDER_NAME is Iterio`() {
        assertEquals("Iterio", AppConfig.Backup.CLOUD_FOLDER_NAME)
    }

    @Test
    fun `Backup MIME_TYPE is application json`() {
        assertEquals("application/json", AppConfig.Backup.MIME_TYPE)
    }

    // Widget Constants Tests
    @Test
    fun `Widget UPDATE_INTERVAL_MS is 1000`() {
        assertEquals(1000L, AppConfig.Widget.UPDATE_INTERVAL_MS)
    }

    // Range Validation Tests
    @Test
    fun `Timer work duration range is valid`() {
        assertTrue(AppConfig.Timer.MIN_WORK_MINUTES < AppConfig.Timer.MAX_WORK_MINUTES)
        assertTrue(AppConfig.Timer.DEFAULT_WORK_MINUTES >= AppConfig.Timer.MIN_WORK_MINUTES)
        assertTrue(AppConfig.Timer.DEFAULT_WORK_MINUTES <= AppConfig.Timer.MAX_WORK_MINUTES)
    }

    @Test
    fun `Timer break duration range is valid`() {
        assertTrue(AppConfig.Timer.MIN_BREAK_MINUTES < AppConfig.Timer.MAX_BREAK_MINUTES)
        assertTrue(AppConfig.Timer.DEFAULT_SHORT_BREAK_MINUTES >= AppConfig.Timer.MIN_BREAK_MINUTES)
        assertTrue(AppConfig.Timer.DEFAULT_SHORT_BREAK_MINUTES <= AppConfig.Timer.MAX_BREAK_MINUTES)
        assertTrue(AppConfig.Timer.DEFAULT_LONG_BREAK_MINUTES >= AppConfig.Timer.MIN_BREAK_MINUTES)
        assertTrue(AppConfig.Timer.DEFAULT_LONG_BREAK_MINUTES <= AppConfig.Timer.MAX_BREAK_MINUTES)
    }

    @Test
    fun `Timer cycles range is valid`() {
        assertTrue(AppConfig.Timer.MIN_CYCLES < AppConfig.Timer.MAX_CYCLES)
        assertTrue(AppConfig.Timer.DEFAULT_CYCLES >= AppConfig.Timer.MIN_CYCLES)
        assertTrue(AppConfig.Timer.DEFAULT_CYCLES <= AppConfig.Timer.MAX_CYCLES)
    }

    @Test
    fun `DailyGoal range is valid`() {
        assertTrue(AppConfig.DailyGoal.MIN_MINUTES < AppConfig.DailyGoal.MAX_MINUTES)
        assertTrue(AppConfig.DailyGoal.DEFAULT_MINUTES >= AppConfig.DailyGoal.MIN_MINUTES)
        assertTrue(AppConfig.DailyGoal.DEFAULT_MINUTES <= AppConfig.DailyGoal.MAX_MINUTES)
    }
}
