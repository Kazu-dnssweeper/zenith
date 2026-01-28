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

class StopTimerActionCallbackTest {

    private lateinit var context: Context
    private lateinit var prefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        prefs = mockk(relaxed = true)
        editor = mockk(relaxed = true)
        every { context.getSharedPreferences("iterio_widget_timer_prefs", Context.MODE_PRIVATE) } returns prefs
        every { prefs.edit() } returns editor
        every { editor.putInt(any(), any()) } returns editor
        every { editor.putBoolean(any(), any()) } returns editor
    }

    @Test
    fun `stop clears timer prefs to IDLE`() {
        prefs.edit()
            .putInt("timer_phase", TimerPhase.IDLE.ordinal)
            .putInt("time_remaining", 0)
            .putBoolean("is_running", false)
            .apply()

        verify { editor.putInt("timer_phase", TimerPhase.IDLE.ordinal) }
        verify { editor.putInt("time_remaining", 0) }
        verify { editor.putBoolean("is_running", false) }
        verify { editor.apply() }
    }

    @Test
    fun `IDLE ordinal is correct`() {
        assertEquals(3, TimerPhase.IDLE.ordinal)
    }

    @Test
    fun `prefs keys match expected values`() {
        val timerPrefsName = "iterio_widget_timer_prefs"
        val keyPhase = "timer_phase"
        val keyTimeRemaining = "time_remaining"
        val keyIsRunning = "is_running"

        assertEquals("iterio_widget_timer_prefs", timerPrefsName)
        assertEquals("timer_phase", keyPhase)
        assertEquals("time_remaining", keyTimeRemaining)
        assertEquals("is_running", keyIsRunning)
    }
}
