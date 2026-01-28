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
import io.mockk.verifyOrder
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class StopTimerActionCallbackTest {

    private lateinit var context: Context
    private lateinit var prefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var glanceId: GlanceId
    private lateinit var params: ActionParameters
    private lateinit var callback: StopTimerActionCallback

    @Before
    fun setup() {
        mockkObject(TimerService.Companion)
        mockkObject(IterioWidgetStateHelper)

        context = mockk(relaxed = true)
        prefs = mockk(relaxed = true)
        editor = mockk(relaxed = true)
        glanceId = mockk(relaxed = true)
        params = mockk(relaxed = true)
        callback = StopTimerActionCallback()

        every { context.getSharedPreferences("iterio_widget_timer_prefs", Context.MODE_PRIVATE) } returns prefs
        every { prefs.edit() } returns editor
        every { editor.putInt(any(), any()) } returns editor
        every { editor.putBoolean(any(), any()) } returns editor
        every { TimerService.stopTimer(any()) } returns Unit
        every { IterioWidgetStateHelper.updateWidget(any()) } returns Unit
    }

    @After
    fun tearDown() {
        unmockkObject(TimerService.Companion)
        unmockkObject(IterioWidgetStateHelper)
    }

    @Test
    fun `onAction calls stopTimer`() = runTest {
        callback.onAction(context, glanceId, params)

        verify(exactly = 1) { TimerService.stopTimer(context) }
    }

    @Test
    fun `onAction clears SharedPrefs to IDLE`() = runTest {
        callback.onAction(context, glanceId, params)

        verify { editor.putInt("timer_phase", TimerPhase.IDLE.ordinal) }
        verify { editor.putInt("time_remaining", 0) }
        verify { editor.putBoolean("is_running", false) }
        verify(exactly = 1) { editor.apply() }
    }

    @Test
    fun `onAction calls updateWidget`() = runTest {
        callback.onAction(context, glanceId, params)

        verify(exactly = 1) { IterioWidgetStateHelper.updateWidget(context) }
    }

    @Test
    fun `onAction executes in correct order`() = runTest {
        callback.onAction(context, glanceId, params)

        verifyOrder {
            TimerService.stopTimer(context)
            prefs.edit()
            editor.apply()
            IterioWidgetStateHelper.updateWidget(context)
        }
    }
}
