package com.iterio.app.widget.actions

import android.content.Context
import android.content.SharedPreferences
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import com.iterio.app.service.TimerPhase
import com.iterio.app.service.TimerService
import com.iterio.app.widget.IterioWidgetStateHelper
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class ToggleTimerActionCallbackTest {

    private lateinit var context: Context
    private lateinit var prefs: SharedPreferences
    private lateinit var glanceId: GlanceId
    private lateinit var params: ActionParameters
    private lateinit var callback: ToggleTimerActionCallback

    @Before
    fun setup() {
        mockkObject(TimerService.Companion)
        mockkObject(IterioWidgetStateHelper)

        context = mockk(relaxed = true)
        prefs = mockk(relaxed = true)
        glanceId = mockk(relaxed = true)
        params = mockk(relaxed = true)
        callback = ToggleTimerActionCallback()

        every { context.getSharedPreferences("iterio_widget_timer_prefs", Context.MODE_PRIVATE) } returns prefs
        every { TimerService.pauseTimer(any()) } returns Unit
        every { TimerService.resumeTimer(any()) } returns Unit
        every { TimerService.stopTimer(any()) } returns Unit
        every { IterioWidgetStateHelper.updateWidget(any()) } returns Unit
    }

    @After
    fun tearDown() {
        unmockkObject(TimerService.Companion)
        unmockkObject(IterioWidgetStateHelper)
    }

    @Test
    fun `onAction pauses timer when running`() = runTest {
        every { prefs.getBoolean("is_running", false) } returns true
        every { prefs.getInt("timer_phase", TimerPhase.IDLE.ordinal) } returns TimerPhase.WORK.ordinal

        callback.onAction(context, glanceId, params)

        verify(exactly = 1) { TimerService.pauseTimer(context) }
        verify(exactly = 0) { TimerService.resumeTimer(any()) }
        verify(exactly = 1) { IterioWidgetStateHelper.updateWidget(context) }
    }

    @Test
    fun `onAction resumes timer when paused`() = runTest {
        every { prefs.getBoolean("is_running", false) } returns false
        every { prefs.getInt("timer_phase", TimerPhase.IDLE.ordinal) } returns TimerPhase.WORK.ordinal

        callback.onAction(context, glanceId, params)

        verify(exactly = 1) { TimerService.resumeTimer(context) }
        verify(exactly = 0) { TimerService.pauseTimer(any()) }
    }

    @Test
    fun `onAction does nothing when IDLE`() = runTest {
        every { prefs.getBoolean("is_running", false) } returns false
        every { prefs.getInt("timer_phase", TimerPhase.IDLE.ordinal) } returns TimerPhase.IDLE.ordinal

        callback.onAction(context, glanceId, params)

        verify(exactly = 0) { TimerService.pauseTimer(any()) }
        verify(exactly = 0) { TimerService.resumeTimer(any()) }
        verify(exactly = 0) { IterioWidgetStateHelper.updateWidget(any()) }
    }

    @Test
    fun `onAction works with SHORT_BREAK phase`() = runTest {
        every { prefs.getBoolean("is_running", false) } returns true
        every { prefs.getInt("timer_phase", TimerPhase.IDLE.ordinal) } returns TimerPhase.SHORT_BREAK.ordinal

        callback.onAction(context, glanceId, params)

        verify(exactly = 1) { TimerService.pauseTimer(context) }
    }
}
