package com.iterio.app.data.mapper

import com.iterio.app.data.local.entity.StudySessionEntity
import com.iterio.app.domain.model.StudySession
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

/**
 * StudySessionMapper のユニットテスト
 */
class StudySessionMapperTest {

    private lateinit var mapper: StudySessionMapper

    @Before
    fun setup() {
        mapper = StudySessionMapper()
    }

    // ==================== toDomain テスト ====================

    @Test
    fun `toDomain should map all fields correctly`() {
        val startedAt = LocalDateTime.of(2025, 1, 15, 10, 0)
        val endedAt = LocalDateTime.of(2025, 1, 15, 10, 25)
        val entity = StudySessionEntity(
            id = 1L,
            taskId = 10L,
            startedAt = startedAt,
            endedAt = endedAt,
            workDurationMinutes = 25,
            plannedDurationMinutes = 30,
            cyclesCompleted = 1,
            wasInterrupted = false,
            notes = "Good session"
        )

        val domain = mapper.toDomain(entity)

        assertEquals(1L, domain.id)
        assertEquals(10L, domain.taskId)
        assertEquals(startedAt, domain.startedAt)
        assertEquals(endedAt, domain.endedAt)
        assertEquals(25, domain.workDurationMinutes)
        assertEquals(30, domain.plannedDurationMinutes)
        assertEquals(1, domain.cyclesCompleted)
        assertFalse(domain.wasInterrupted)
        assertEquals("Good session", domain.notes)
    }

    @Test
    fun `toDomain should handle null endedAt`() {
        val startedAt = LocalDateTime.of(2025, 1, 15, 10, 0)
        val entity = StudySessionEntity(
            id = 1L,
            taskId = 10L,
            startedAt = startedAt,
            endedAt = null
        )

        val domain = mapper.toDomain(entity)

        assertNull(domain.endedAt)
    }

    @Test
    fun `toDomain should handle interrupted session`() {
        val entity = StudySessionEntity(
            id = 1L,
            taskId = 10L,
            startedAt = LocalDateTime.now(),
            wasInterrupted = true
        )

        val domain = mapper.toDomain(entity)

        assertTrue(domain.wasInterrupted)
    }

    // ==================== toEntity テスト ====================

    @Test
    fun `toEntity should map all fields correctly`() {
        val startedAt = LocalDateTime.of(2025, 1, 15, 10, 0)
        val endedAt = LocalDateTime.of(2025, 1, 15, 10, 25)
        val domain = StudySession(
            id = 1L,
            taskId = 10L,
            startedAt = startedAt,
            endedAt = endedAt,
            workDurationMinutes = 25,
            plannedDurationMinutes = 30,
            cyclesCompleted = 1,
            wasInterrupted = false,
            notes = "Good session"
        )

        val entity = mapper.toEntity(domain)

        assertEquals(1L, entity.id)
        assertEquals(10L, entity.taskId)
        assertEquals(startedAt, entity.startedAt)
        assertEquals(endedAt, entity.endedAt)
        assertEquals(25, entity.workDurationMinutes)
        assertEquals(30, entity.plannedDurationMinutes)
        assertEquals(1, entity.cyclesCompleted)
        assertFalse(entity.wasInterrupted)
        assertEquals("Good session", entity.notes)
    }

    @Test
    fun `toEntity should not include taskName and groupName`() {
        val domain = StudySession(
            id = 1L,
            taskId = 10L,
            taskName = "Study Math",
            groupName = "Mathematics",
            startedAt = LocalDateTime.now()
        )

        val entity = mapper.toEntity(domain)

        assertEquals(10L, entity.taskId)
        // Entity doesn't have taskName or groupName fields - this is expected
    }

    // ==================== Round Trip テスト ====================

    @Test
    fun `round trip should preserve all data`() {
        val startedAt = LocalDateTime.of(2025, 1, 15, 10, 0)
        val endedAt = LocalDateTime.of(2025, 1, 15, 10, 50)
        val original = StudySession(
            id = 1L,
            taskId = 10L,
            startedAt = startedAt,
            endedAt = endedAt,
            workDurationMinutes = 45,
            plannedDurationMinutes = 50,
            cyclesCompleted = 2,
            wasInterrupted = true,
            notes = "Test notes"
        )

        val roundTripped = mapper.toDomain(mapper.toEntity(original))

        assertEquals(original.id, roundTripped.id)
        assertEquals(original.taskId, roundTripped.taskId)
        assertEquals(original.startedAt, roundTripped.startedAt)
        assertEquals(original.endedAt, roundTripped.endedAt)
        assertEquals(original.workDurationMinutes, roundTripped.workDurationMinutes)
        assertEquals(original.plannedDurationMinutes, roundTripped.plannedDurationMinutes)
        assertEquals(original.cyclesCompleted, roundTripped.cyclesCompleted)
        assertEquals(original.wasInterrupted, roundTripped.wasInterrupted)
        assertEquals(original.notes, roundTripped.notes)
    }

    // ==================== List テスト ====================

    @Test
    fun `toDomainList should map all entities`() {
        val now = LocalDateTime.now()
        val entities = listOf(
            StudySessionEntity(id = 1L, taskId = 1L, startedAt = now),
            StudySessionEntity(id = 2L, taskId = 2L, startedAt = now),
            StudySessionEntity(id = 3L, taskId = 3L, startedAt = now)
        )

        val domains = mapper.toDomainList(entities)

        assertEquals(3, domains.size)
        assertEquals(1L, domains[0].id)
        assertEquals(2L, domains[1].id)
        assertEquals(3L, domains[2].id)
    }
}
