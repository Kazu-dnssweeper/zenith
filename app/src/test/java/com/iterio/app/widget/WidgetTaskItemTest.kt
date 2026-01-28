package com.iterio.app.widget

import org.junit.Assert.*
import org.junit.Test

class WidgetTaskItemTest {

    @Test
    fun `default isCompleted is false`() {
        val item = WidgetTaskItem(name = "Math", groupName = "Science")
        assertFalse(item.isCompleted)
    }

    @Test
    fun `creates item with all fields`() {
        val item = WidgetTaskItem(
            name = "Study Kotlin",
            groupName = "Programming",
            isCompleted = true
        )
        assertEquals("Study Kotlin", item.name)
        assertEquals("Programming", item.groupName)
        assertTrue(item.isCompleted)
    }

    @Test
    fun `copy preserves unchanged fields`() {
        val original = WidgetTaskItem(
            name = "Read Chapter 5",
            groupName = "History",
            isCompleted = false
        )
        val copied = original.copy(isCompleted = true)

        assertEquals("Read Chapter 5", copied.name)
        assertEquals("History", copied.groupName)
        assertTrue(copied.isCompleted)
    }

    @Test
    fun `equality works correctly`() {
        val item1 = WidgetTaskItem(name = "Task", groupName = "Group")
        val item2 = WidgetTaskItem(name = "Task", groupName = "Group")

        assertEquals(item1, item2)
        assertEquals(item1.hashCode(), item2.hashCode())
    }

    @Test
    fun `different names make items unequal`() {
        val item1 = WidgetTaskItem(name = "Task A", groupName = "Group")
        val item2 = WidgetTaskItem(name = "Task B", groupName = "Group")

        assertNotEquals(item1, item2)
    }

    @Test
    fun `empty groupName is valid`() {
        val item = WidgetTaskItem(name = "Task", groupName = "")
        assertEquals("", item.groupName)
    }
}
