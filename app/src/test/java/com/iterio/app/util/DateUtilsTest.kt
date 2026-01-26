package com.iterio.app.util

import org.junit.Assert.*
import org.junit.Test

/**
 * DateUtils のユニットテスト
 */
class DateUtilsTest {

    @Test
    fun `WEEKDAY_LABELS has 7 days`() {
        assertEquals(7, DateUtils.WEEKDAY_LABELS.size)
    }

    @Test
    fun `WEEKDAY_LABELS starts with Monday`() {
        assertEquals("月", DateUtils.WEEKDAY_LABELS[0])
    }

    @Test
    fun `WEEKDAY_LABELS ends with Sunday`() {
        assertEquals("日", DateUtils.WEEKDAY_LABELS[6])
    }

    @Test
    fun `WEEKDAY_LABELS contains all days in order`() {
        val expected = listOf("月", "火", "水", "木", "金", "土", "日")
        assertEquals(expected, DateUtils.WEEKDAY_LABELS)
    }

    @Test
    fun `WEEKDAY_LABELS index matches DayOfWeek value minus 1`() {
        // DayOfWeek.MONDAY.value = 1, index should be 0
        assertEquals("月", DateUtils.WEEKDAY_LABELS[1 - 1])
        // DayOfWeek.TUESDAY.value = 2, index should be 1
        assertEquals("火", DateUtils.WEEKDAY_LABELS[2 - 1])
        // DayOfWeek.FRIDAY.value = 5, index should be 4
        assertEquals("金", DateUtils.WEEKDAY_LABELS[5 - 1])
        // DayOfWeek.SUNDAY.value = 7, index should be 6
        assertEquals("日", DateUtils.WEEKDAY_LABELS[7 - 1])
    }
}
