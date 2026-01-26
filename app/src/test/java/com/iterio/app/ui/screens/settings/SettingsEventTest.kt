package com.iterio.app.ui.screens.settings

import org.junit.Assert.*
import org.junit.Test

/**
 * SettingsEvent のユニットテスト
 */
class SettingsEventTest {

    @Test
    fun `ToggleNotifications contains enabled value`() {
        val event = SettingsEvent.ToggleNotifications(true)
        assertTrue(event.enabled)

        val disabledEvent = SettingsEvent.ToggleNotifications(false)
        assertFalse(disabledEvent.enabled)
    }

    @Test
    fun `ToggleReviewIntervals contains enabled value`() {
        val event = SettingsEvent.ToggleReviewIntervals(true)
        assertTrue(event.enabled)
    }

    @Test
    fun `UpdateWorkDuration contains minutes value`() {
        val event = SettingsEvent.UpdateWorkDuration(30)
        assertEquals(30, event.minutes)
    }

    @Test
    fun `UpdateShortBreak contains minutes value`() {
        val event = SettingsEvent.UpdateShortBreak(10)
        assertEquals(10, event.minutes)
    }

    @Test
    fun `UpdateLongBreak contains minutes value`() {
        val event = SettingsEvent.UpdateLongBreak(20)
        assertEquals(20, event.minutes)
    }

    @Test
    fun `UpdateCycles contains cycles value`() {
        val event = SettingsEvent.UpdateCycles(6)
        assertEquals(6, event.cycles)
    }

    @Test
    fun `ToggleFocusMode contains enabled value`() {
        val event = SettingsEvent.ToggleFocusMode(true)
        assertTrue(event.enabled)
    }

    @Test
    fun `ToggleFocusModeStrict contains strict value`() {
        val event = SettingsEvent.ToggleFocusModeStrict(true)
        assertTrue(event.strict)
    }

    @Test
    fun `ToggleAutoLoop contains enabled value`() {
        val event = SettingsEvent.ToggleAutoLoop(true)
        assertTrue(event.enabled)
    }

    @Test
    fun `StartTrial is a singleton object`() {
        val event1 = SettingsEvent.StartTrial
        val event2 = SettingsEvent.StartTrial
        assertSame(event1, event2)
    }

    @Test
    fun `UpdateLanguage contains languageCode value`() {
        val event = SettingsEvent.UpdateLanguage("en")
        assertEquals("en", event.languageCode)
    }

    @Test
    fun `all events are SettingsEvent subtypes`() {
        val events: List<SettingsEvent> = listOf(
            SettingsEvent.ToggleNotifications(true),
            SettingsEvent.ToggleReviewIntervals(true),
            SettingsEvent.UpdateWorkDuration(25),
            SettingsEvent.UpdateShortBreak(5),
            SettingsEvent.UpdateLongBreak(15),
            SettingsEvent.UpdateCycles(4),
            SettingsEvent.ToggleFocusMode(true),
            SettingsEvent.ToggleFocusModeStrict(false),
            SettingsEvent.ToggleAutoLoop(false),
            SettingsEvent.StartTrial,
            SettingsEvent.UpdateLanguage("ja")
        )
        assertEquals(11, events.size)
    }
}
