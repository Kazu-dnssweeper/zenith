package com.iterio.app.widget

import com.iterio.app.service.TimerPhase
import org.junit.Assert.*
import org.junit.Test

class WidgetStateTest {

    @Test
    fun `default values are correct`() {
        val state = WidgetState()

        assertEquals(0, state.todayStudyMinutes)
        assertEquals(0, state.currentStreak)
        assertEquals(TimerPhase.IDLE, state.timerPhase)
        assertEquals(0, state.timeRemainingSeconds)
        assertFalse(state.isTimerRunning)
        assertFalse(state.isPremium)
        assertEquals(0, state.pendingReviewCount)
        assertEquals(emptyList<WidgetTaskItem>(), state.todayTasks)
    }

    @Test
    fun `copy preserves unchanged fields`() {
        val original = WidgetState(
            todayStudyMinutes = 120,
            currentStreak = 5,
            timerPhase = TimerPhase.WORK,
            timeRemainingSeconds = 300,
            isTimerRunning = true,
            isPremium = true,
            pendingReviewCount = 3
        )

        val copied = original.copy(todayStudyMinutes = 60)

        assertEquals(60, copied.todayStudyMinutes)
        assertEquals(5, copied.currentStreak)
        assertEquals(TimerPhase.WORK, copied.timerPhase)
        assertEquals(300, copied.timeRemainingSeconds)
        assertTrue(copied.isTimerRunning)
        assertTrue(copied.isPremium)
        assertEquals(3, copied.pendingReviewCount)
    }

    @Test
    fun `copy with pendingReviewCount updates correctly`() {
        val original = WidgetState()
        val updated = original.copy(pendingReviewCount = 7)

        assertEquals(0, original.pendingReviewCount)
        assertEquals(7, updated.pendingReviewCount)
    }

    @Test
    fun `equality works correctly`() {
        val state1 = WidgetState(
            todayStudyMinutes = 30,
            currentStreak = 2,
            pendingReviewCount = 1
        )
        val state2 = WidgetState(
            todayStudyMinutes = 30,
            currentStreak = 2,
            pendingReviewCount = 1
        )

        assertEquals(state1, state2)
        assertEquals(state1.hashCode(), state2.hashCode())
    }

    @Test
    fun `different pendingReviewCount makes states unequal`() {
        val state1 = WidgetState(pendingReviewCount = 0)
        val state2 = WidgetState(pendingReviewCount = 5)

        assertNotEquals(state1, state2)
    }

    @Test
    fun `todayTasks field works correctly`() {
        val tasks = listOf(
            WidgetTaskItem(name = "Math", groupName = "Science"),
            WidgetTaskItem(name = "English", groupName = "Languages")
        )
        val state = WidgetState(todayTasks = tasks)

        assertEquals(2, state.todayTasks.size)
        assertEquals("Math", state.todayTasks[0].name)
        assertEquals("English", state.todayTasks[1].name)
    }

    @Test
    fun `copy with todayTasks creates new instance`() {
        val original = WidgetState()
        val tasks = listOf(WidgetTaskItem(name = "Task", groupName = "Group"))
        val updated = original.copy(todayTasks = tasks)

        assertEquals(0, original.todayTasks.size)
        assertEquals(1, updated.todayTasks.size)
    }
}
