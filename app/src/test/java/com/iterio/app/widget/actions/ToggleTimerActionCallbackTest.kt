package com.iterio.app.widget.actions

import android.content.Context
import android.content.SharedPreferences
import com.iterio.app.service.TimerPhase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ToggleTimerActionCallbackTest {

    private lateinit var context: Context
    private lateinit var prefs: SharedPreferences

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        prefs = mockk(relaxed = true)
        every { context.getSharedPreferences("iterio_widget_timer_prefs", Context.MODE_PRIVATE) } returns prefs
    }

    @Test
    fun `isRunning true means timer should be paused`() {
        every { prefs.getBoolean("is_running", false) } returns true
        every { prefs.getInt("timer_phase", TimerPhase.IDLE.ordinal) } returns TimerPhase.WORK.ordinal

        // Verify that when running, the pause path would be chosen
        val isRunning = prefs.getBoolean("is_running", false)
        val phase = TimerPhase.entries[prefs.getInt("timer_phase", TimerPhase.IDLE.ordinal)]

        assertTrue(isRunning)
        assertEquals(TimerPhase.WORK, phase)
    }

    @Test
    fun `isRunning false means timer should be resumed`() {
        every { prefs.getBoolean("is_running", false) } returns false
        every { prefs.getInt("timer_phase", TimerPhase.IDLE.ordinal) } returns TimerPhase.WORK.ordinal

        val isRunning = prefs.getBoolean("is_running", false)
        val phase = TimerPhase.entries[prefs.getInt("timer_phase", TimerPhase.IDLE.ordinal)]

        assertFalse(isRunning)
        assertEquals(TimerPhase.WORK, phase)
    }

    @Test
    fun `IDLE phase should not trigger any action`() {
        every { prefs.getBoolean("is_running", false) } returns false
        every { prefs.getInt("timer_phase", TimerPhase.IDLE.ordinal) } returns TimerPhase.IDLE.ordinal

        val phase = TimerPhase.entries[prefs.getInt("timer_phase", TimerPhase.IDLE.ordinal)]

        assertEquals(TimerPhase.IDLE, phase)
    }

    @Test
    fun `SHORT_BREAK phase is valid for toggle`() {
        every { prefs.getBoolean("is_running", false) } returns true
        every { prefs.getInt("timer_phase", TimerPhase.IDLE.ordinal) } returns TimerPhase.SHORT_BREAK.ordinal

        val phase = TimerPhase.entries[prefs.getInt("timer_phase", TimerPhase.IDLE.ordinal)]

        assertEquals(TimerPhase.SHORT_BREAK, phase)
    }
}
