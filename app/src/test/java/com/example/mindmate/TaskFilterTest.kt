package com.example.mindmate

import org.junit.Assert.assertEquals
import org.junit.Test

class TaskFilterTest {
    private val sampleTasks = listOf(
        Task(userEmail = "a@a.com", title = "Math homework", priority = "High", status = "Pending"),
        Task(userEmail = "a@a.com", title = "Read chapter 3", priority = "Low", status = "Completed"),
        Task(userEmail = "a@a.com", title = "Lab report", priority = "High", status = "In Progress")
    )

    private val filterHelper = TaskFilter()

    @Test
    fun `filter returns only tasks matching status`() {
        val result = filterHelper.filterTasks(sampleTasks) {
            it.status == "Completed"
        }
        assertEquals(1, result.size)
        assertEquals("Read chapter 3", result[0].title)
    }

    @Test
    fun `filter returns only tasks matching priority`() {
        val result = filterHelper.filterTasks(sampleTasks) {
            it.priority == "High"
        }
        assertEquals(2, result.size)
    }

    @Test
    fun `filter returns only tasks matching search text`() {
        val result = filterHelper.filterTasks(sampleTasks) {
            it.title.contains("lab", ignoreCase = true)
        }
        assertEquals(1, result.size)
        assertEquals("Lab report", result[0].title)
    }

    @Test
    fun `filter returns empty list when no match found`() {
        val result = filterHelper.filterTasks(sampleTasks) {
            it.title.contains("Chemistry", ignoreCase = true)
        }
        assertEquals(0, result.size)
    }
}
