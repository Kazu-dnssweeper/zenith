package com.iterio.app.data.mapper

import com.iterio.app.data.local.entity.TaskEntity
import com.iterio.app.domain.model.ScheduleType
import com.iterio.app.domain.model.Task
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * TaskMapper のユニットテスト
 *
 * TDD RED Phase: これらのテストは最初は失敗する
 */
class TaskMapperTest {

    private lateinit var mapper: TaskMapper

    @Before
    fun setup() {
        mapper = TaskMapper()
    }

    // ==================== toDomain テスト ====================

    @Test
    fun `toDomain should map all basic fields correctly`() {
        val now = LocalDateTime.of(2025, 1, 15, 10, 30)
        val entity = TaskEntity(
            id = 1L,
            groupId = 2L,
            name = "Test Task",
            progressNote = "In progress",
            progressPercent = 50,
            nextGoal = "Complete chapter 3",
            workDurationMinutes = 45,
            isActive = true,
            createdAt = now,
            updatedAt = now,
            scheduleType = null,
            repeatDays = null,
            deadlineDate = null,
            specificDate = null,
            lastStudiedAt = null
        )

        val domain = mapper.toDomain(entity)

        assertEquals(1L, domain.id)
        assertEquals(2L, domain.groupId)
        assertEquals("Test Task", domain.name)
        assertEquals("In progress", domain.progressNote)
        assertEquals(50, domain.progressPercent)
        assertEquals("Complete chapter 3", domain.nextGoal)
        assertEquals(45, domain.workDurationMinutes)
        assertTrue(domain.isActive)
        assertEquals(now, domain.createdAt)
        assertEquals(now, domain.updatedAt)
    }

    @Test
    fun `toDomain should map null scheduleType to NONE`() {
        val entity = TaskEntity(
            id = 1L,
            groupId = 1L,
            name = "Task",
            scheduleType = null
        )

        val domain = mapper.toDomain(entity)

        assertEquals(ScheduleType.NONE, domain.scheduleType)
    }

    @Test
    fun `toDomain should map repeat scheduleType correctly`() {
        val entity = TaskEntity(
            id = 1L,
            groupId = 1L,
            name = "Task",
            scheduleType = "repeat",
            repeatDays = "1,3,5"
        )

        val domain = mapper.toDomain(entity)

        assertEquals(ScheduleType.REPEAT, domain.scheduleType)
        assertEquals(setOf(1, 3, 5), domain.repeatDays)
    }

    @Test
    fun `toDomain should map deadline scheduleType correctly`() {
        val deadline = LocalDate.of(2025, 2, 28)
        val entity = TaskEntity(
            id = 1L,
            groupId = 1L,
            name = "Task",
            scheduleType = "deadline",
            deadlineDate = deadline
        )

        val domain = mapper.toDomain(entity)

        assertEquals(ScheduleType.DEADLINE, domain.scheduleType)
        assertEquals(deadline, domain.deadlineDate)
    }

    @Test
    fun `toDomain should map specific scheduleType correctly`() {
        val specificDate = LocalDate.of(2025, 3, 15)
        val entity = TaskEntity(
            id = 1L,
            groupId = 1L,
            name = "Task",
            scheduleType = "specific",
            specificDate = specificDate
        )

        val domain = mapper.toDomain(entity)

        assertEquals(ScheduleType.SPECIFIC, domain.scheduleType)
        assertEquals(specificDate, domain.specificDate)
    }

    @Test
    fun `toDomain should handle empty repeatDays as empty set`() {
        val entity = TaskEntity(
            id = 1L,
            groupId = 1L,
            name = "Task",
            scheduleType = "repeat",
            repeatDays = ""
        )

        val domain = mapper.toDomain(entity)

        assertTrue(domain.repeatDays.isEmpty())
    }

    @Test
    fun `toDomain should handle null repeatDays as empty set`() {
        val entity = TaskEntity(
            id = 1L,
            groupId = 1L,
            name = "Task",
            scheduleType = "repeat",
            repeatDays = null
        )

        val domain = mapper.toDomain(entity)

        assertTrue(domain.repeatDays.isEmpty())
    }

    @Test
    fun `toDomain should map lastStudiedAt correctly`() {
        val lastStudied = LocalDateTime.of(2025, 1, 10, 14, 0)
        val entity = TaskEntity(
            id = 1L,
            groupId = 1L,
            name = "Task",
            lastStudiedAt = lastStudied
        )

        val domain = mapper.toDomain(entity)

        assertEquals(lastStudied, domain.lastStudiedAt)
    }

    // ==================== toEntity テスト ====================

    @Test
    fun `toEntity should map all basic fields correctly`() {
        val now = LocalDateTime.of(2025, 1, 15, 10, 30)
        val domain = Task(
            id = 1L,
            groupId = 2L,
            name = "Test Task",
            progressNote = "In progress",
            progressPercent = 50,
            nextGoal = "Complete chapter 3",
            workDurationMinutes = 45,
            isActive = true,
            createdAt = now,
            updatedAt = now,
            scheduleType = ScheduleType.NONE,
            repeatDays = emptySet(),
            deadlineDate = null,
            specificDate = null,
            lastStudiedAt = null
        )

        val entity = mapper.toEntity(domain)

        assertEquals(1L, entity.id)
        assertEquals(2L, entity.groupId)
        assertEquals("Test Task", entity.name)
        assertEquals("In progress", entity.progressNote)
        assertEquals(50, entity.progressPercent)
        assertEquals("Complete chapter 3", entity.nextGoal)
        assertEquals(45, entity.workDurationMinutes)
        assertTrue(entity.isActive)
        assertEquals(now, entity.createdAt)
        assertEquals(now, entity.updatedAt)
    }

    @Test
    fun `toEntity should map NONE scheduleType to null`() {
        val domain = Task(
            id = 1L,
            groupId = 1L,
            name = "Task",
            scheduleType = ScheduleType.NONE
        )

        val entity = mapper.toEntity(domain)

        assertNull(entity.scheduleType)
    }

    @Test
    fun `toEntity should map REPEAT scheduleType correctly`() {
        val domain = Task(
            id = 1L,
            groupId = 1L,
            name = "Task",
            scheduleType = ScheduleType.REPEAT,
            repeatDays = setOf(1, 3, 5)
        )

        val entity = mapper.toEntity(domain)

        assertEquals("repeat", entity.scheduleType)
        assertEquals("1,3,5", entity.repeatDays)
    }

    @Test
    fun `toEntity should map DEADLINE scheduleType correctly`() {
        val deadline = LocalDate.of(2025, 2, 28)
        val domain = Task(
            id = 1L,
            groupId = 1L,
            name = "Task",
            scheduleType = ScheduleType.DEADLINE,
            deadlineDate = deadline
        )

        val entity = mapper.toEntity(domain)

        assertEquals("deadline", entity.scheduleType)
        assertEquals(deadline, entity.deadlineDate)
    }

    @Test
    fun `toEntity should map SPECIFIC scheduleType correctly`() {
        val specificDate = LocalDate.of(2025, 3, 15)
        val domain = Task(
            id = 1L,
            groupId = 1L,
            name = "Task",
            scheduleType = ScheduleType.SPECIFIC,
            specificDate = specificDate
        )

        val entity = mapper.toEntity(domain)

        assertEquals("specific", entity.scheduleType)
        assertEquals(specificDate, entity.specificDate)
    }

    @Test
    fun `toEntity should map empty repeatDays to null`() {
        val domain = Task(
            id = 1L,
            groupId = 1L,
            name = "Task",
            scheduleType = ScheduleType.REPEAT,
            repeatDays = emptySet()
        )

        val entity = mapper.toEntity(domain)

        assertNull(entity.repeatDays)
    }

    @Test
    fun `toEntity should sort repeatDays`() {
        val domain = Task(
            id = 1L,
            groupId = 1L,
            name = "Task",
            scheduleType = ScheduleType.REPEAT,
            repeatDays = setOf(5, 1, 3, 7)
        )

        val entity = mapper.toEntity(domain)

        assertEquals("1,3,5,7", entity.repeatDays)
    }

    // ==================== Round Trip テスト ====================

    @Test
    fun `round trip should preserve all data for basic task`() {
        val now = LocalDateTime.of(2025, 1, 15, 10, 30)
        val original = Task(
            id = 1L,
            groupId = 2L,
            name = "Test Task",
            progressNote = "Note",
            progressPercent = 75,
            nextGoal = "Goal",
            workDurationMinutes = 30,
            isActive = true,
            createdAt = now,
            updatedAt = now,
            scheduleType = ScheduleType.NONE,
            repeatDays = emptySet(),
            deadlineDate = null,
            specificDate = null,
            lastStudiedAt = null
        )

        val roundTripped = mapper.toDomain(mapper.toEntity(original))

        assertEquals(original.id, roundTripped.id)
        assertEquals(original.groupId, roundTripped.groupId)
        assertEquals(original.name, roundTripped.name)
        assertEquals(original.progressNote, roundTripped.progressNote)
        assertEquals(original.progressPercent, roundTripped.progressPercent)
        assertEquals(original.nextGoal, roundTripped.nextGoal)
        assertEquals(original.workDurationMinutes, roundTripped.workDurationMinutes)
        assertEquals(original.isActive, roundTripped.isActive)
        assertEquals(original.scheduleType, roundTripped.scheduleType)
    }

    @Test
    fun `round trip should preserve repeat task data`() {
        val original = Task(
            id = 1L,
            groupId = 1L,
            name = "Repeat Task",
            scheduleType = ScheduleType.REPEAT,
            repeatDays = setOf(1, 3, 5)
        )

        val roundTripped = mapper.toDomain(mapper.toEntity(original))

        assertEquals(original.scheduleType, roundTripped.scheduleType)
        assertEquals(original.repeatDays, roundTripped.repeatDays)
    }

    @Test
    fun `round trip should preserve deadline task data`() {
        val deadline = LocalDate.of(2025, 6, 30)
        val original = Task(
            id = 1L,
            groupId = 1L,
            name = "Deadline Task",
            scheduleType = ScheduleType.DEADLINE,
            deadlineDate = deadline
        )

        val roundTripped = mapper.toDomain(mapper.toEntity(original))

        assertEquals(original.scheduleType, roundTripped.scheduleType)
        assertEquals(original.deadlineDate, roundTripped.deadlineDate)
    }

    // ==================== List テスト ====================

    @Test
    fun `toDomainList should map all entities`() {
        val entities = listOf(
            TaskEntity(id = 1L, groupId = 1L, name = "Task 1"),
            TaskEntity(id = 2L, groupId = 1L, name = "Task 2"),
            TaskEntity(id = 3L, groupId = 2L, name = "Task 3")
        )

        val domains = mapper.toDomainList(entities)

        assertEquals(3, domains.size)
        assertEquals("Task 1", domains[0].name)
        assertEquals("Task 2", domains[1].name)
        assertEquals("Task 3", domains[2].name)
    }

    @Test
    fun `toEntityList should map all domains`() {
        val domains = listOf(
            Task(id = 1L, groupId = 1L, name = "Task 1"),
            Task(id = 2L, groupId = 1L, name = "Task 2")
        )

        val entities = mapper.toEntityList(domains)

        assertEquals(2, entities.size)
        assertEquals("Task 1", entities[0].name)
        assertEquals("Task 2", entities[1].name)
    }

    @Test
    fun `toDomainList should return empty list for empty input`() {
        val entities = emptyList<TaskEntity>()

        val domains = mapper.toDomainList(entities)

        assertTrue(domains.isEmpty())
    }
}
