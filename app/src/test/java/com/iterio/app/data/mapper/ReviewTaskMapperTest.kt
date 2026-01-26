package com.iterio.app.data.mapper

import com.iterio.app.data.local.entity.ReviewTaskEntity
import com.iterio.app.domain.model.ReviewTask
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * ReviewTaskMapper のユニットテスト
 */
class ReviewTaskMapperTest {

    private lateinit var mapper: ReviewTaskMapper

    @Before
    fun setup() {
        mapper = ReviewTaskMapper()
    }

    // ==================== toDomain テスト ====================

    @Test
    fun `toDomain should map all fields correctly`() {
        val scheduledDate = LocalDate.of(2025, 1, 20)
        val completedAt = LocalDateTime.of(2025, 1, 20, 14, 30)
        val createdAt = LocalDateTime.of(2025, 1, 15, 10, 0)
        val entity = ReviewTaskEntity(
            id = 1L,
            studySessionId = 100L,
            taskId = 50L,
            scheduledDate = scheduledDate,
            reviewNumber = 3,
            isCompleted = true,
            completedAt = completedAt,
            createdAt = createdAt
        )

        val domain = mapper.toDomain(entity)

        assertEquals(1L, domain.id)
        assertEquals(100L, domain.studySessionId)
        assertEquals(50L, domain.taskId)
        assertEquals(scheduledDate, domain.scheduledDate)
        assertEquals(3, domain.reviewNumber)
        assertTrue(domain.isCompleted)
        assertEquals(completedAt, domain.completedAt)
        assertEquals(createdAt, domain.createdAt)
    }

    @Test
    fun `toDomain should handle incomplete task`() {
        val entity = ReviewTaskEntity(
            id = 1L,
            studySessionId = 100L,
            taskId = 50L,
            scheduledDate = LocalDate.now(),
            reviewNumber = 1,
            isCompleted = false,
            completedAt = null
        )

        val domain = mapper.toDomain(entity)

        assertFalse(domain.isCompleted)
        assertNull(domain.completedAt)
    }

    @Test
    fun `toDomain should map all review numbers correctly`() {
        for (reviewNum in 1..6) {
            val entity = ReviewTaskEntity(
                id = reviewNum.toLong(),
                studySessionId = 1L,
                taskId = 1L,
                scheduledDate = LocalDate.now(),
                reviewNumber = reviewNum
            )

            val domain = mapper.toDomain(entity)

            assertEquals(reviewNum, domain.reviewNumber)
        }
    }

    // ==================== toEntity テスト ====================

    @Test
    fun `toEntity should map all fields correctly`() {
        val scheduledDate = LocalDate.of(2025, 1, 20)
        val completedAt = LocalDateTime.of(2025, 1, 20, 14, 30)
        val createdAt = LocalDateTime.of(2025, 1, 15, 10, 0)
        val domain = ReviewTask(
            id = 1L,
            studySessionId = 100L,
            taskId = 50L,
            scheduledDate = scheduledDate,
            reviewNumber = 3,
            isCompleted = true,
            completedAt = completedAt,
            createdAt = createdAt
        )

        val entity = mapper.toEntity(domain)

        assertEquals(1L, entity.id)
        assertEquals(100L, entity.studySessionId)
        assertEquals(50L, entity.taskId)
        assertEquals(scheduledDate, entity.scheduledDate)
        assertEquals(3, entity.reviewNumber)
        assertTrue(entity.isCompleted)
        assertEquals(completedAt, entity.completedAt)
        assertEquals(createdAt, entity.createdAt)
    }

    @Test
    fun `toEntity should not include taskName and groupName`() {
        val domain = ReviewTask(
            id = 1L,
            studySessionId = 100L,
            taskId = 50L,
            scheduledDate = LocalDate.now(),
            reviewNumber = 1,
            taskName = "Math Study",
            groupName = "Mathematics"
        )

        val entity = mapper.toEntity(domain)

        assertEquals(50L, entity.taskId)
        // Entity doesn't have taskName or groupName fields - this is expected
    }

    // ==================== Round Trip テスト ====================

    @Test
    fun `round trip should preserve all data`() {
        val scheduledDate = LocalDate.of(2025, 2, 1)
        val completedAt = LocalDateTime.of(2025, 2, 1, 18, 0)
        val createdAt = LocalDateTime.of(2025, 1, 25, 12, 0)
        val original = ReviewTask(
            id = 5L,
            studySessionId = 200L,
            taskId = 100L,
            scheduledDate = scheduledDate,
            reviewNumber = 4,
            isCompleted = true,
            completedAt = completedAt,
            createdAt = createdAt
        )

        val roundTripped = mapper.toDomain(mapper.toEntity(original))

        assertEquals(original.id, roundTripped.id)
        assertEquals(original.studySessionId, roundTripped.studySessionId)
        assertEquals(original.taskId, roundTripped.taskId)
        assertEquals(original.scheduledDate, roundTripped.scheduledDate)
        assertEquals(original.reviewNumber, roundTripped.reviewNumber)
        assertEquals(original.isCompleted, roundTripped.isCompleted)
        assertEquals(original.completedAt, roundTripped.completedAt)
        assertEquals(original.createdAt, roundTripped.createdAt)
    }

    // ==================== List テスト ====================

    @Test
    fun `toDomainList should map all entities`() {
        val today = LocalDate.now()
        val entities = listOf(
            ReviewTaskEntity(id = 1L, studySessionId = 1L, taskId = 1L, scheduledDate = today, reviewNumber = 1),
            ReviewTaskEntity(id = 2L, studySessionId = 1L, taskId = 1L, scheduledDate = today.plusDays(3), reviewNumber = 2),
            ReviewTaskEntity(id = 3L, studySessionId = 1L, taskId = 1L, scheduledDate = today.plusDays(7), reviewNumber = 3)
        )

        val domains = mapper.toDomainList(entities)

        assertEquals(3, domains.size)
        assertEquals(1, domains[0].reviewNumber)
        assertEquals(2, domains[1].reviewNumber)
        assertEquals(3, domains[2].reviewNumber)
    }

    @Test
    fun `toEntityList should map all domains`() {
        val today = LocalDate.now()
        val domains = listOf(
            ReviewTask(id = 1L, studySessionId = 1L, taskId = 1L, scheduledDate = today, reviewNumber = 1),
            ReviewTask(id = 2L, studySessionId = 1L, taskId = 1L, scheduledDate = today.plusDays(3), reviewNumber = 2)
        )

        val entities = mapper.toEntityList(domains)

        assertEquals(2, entities.size)
        assertEquals(1, entities[0].reviewNumber)
        assertEquals(2, entities[1].reviewNumber)
    }
}
