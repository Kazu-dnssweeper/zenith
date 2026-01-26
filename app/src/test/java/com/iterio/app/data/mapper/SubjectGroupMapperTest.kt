package com.iterio.app.data.mapper

import com.iterio.app.data.local.entity.SubjectGroupEntity
import com.iterio.app.domain.model.SubjectGroup
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

/**
 * SubjectGroupMapper のユニットテスト
 */
class SubjectGroupMapperTest {

    private lateinit var mapper: SubjectGroupMapper

    @Before
    fun setup() {
        mapper = SubjectGroupMapper()
    }

    // ==================== toDomain テスト ====================

    @Test
    fun `toDomain should map all fields correctly`() {
        val createdAt = LocalDateTime.of(2025, 1, 15, 10, 0)
        val entity = SubjectGroupEntity(
            id = 1L,
            name = "Mathematics",
            colorHex = "#FF5722",
            displayOrder = 3,
            createdAt = createdAt
        )

        val domain = mapper.toDomain(entity)

        assertEquals(1L, domain.id)
        assertEquals("Mathematics", domain.name)
        assertEquals("#FF5722", domain.colorHex)
        assertEquals(3, domain.displayOrder)
        assertEquals(createdAt, domain.createdAt)
    }

    @Test
    fun `toDomain should handle default color`() {
        val entity = SubjectGroupEntity(
            id = 1L,
            name = "Science",
            colorHex = "#00838F"
        )

        val domain = mapper.toDomain(entity)

        assertEquals("#00838F", domain.colorHex)
    }

    // ==================== toEntity テスト ====================

    @Test
    fun `toEntity should map all fields correctly`() {
        val createdAt = LocalDateTime.of(2025, 1, 15, 10, 0)
        val domain = SubjectGroup(
            id = 1L,
            name = "Mathematics",
            colorHex = "#FF5722",
            displayOrder = 3,
            createdAt = createdAt
        )

        val entity = mapper.toEntity(domain)

        assertEquals(1L, entity.id)
        assertEquals("Mathematics", entity.name)
        assertEquals("#FF5722", entity.colorHex)
        assertEquals(3, entity.displayOrder)
        assertEquals(createdAt, entity.createdAt)
    }

    // ==================== Round Trip テスト ====================

    @Test
    fun `round trip should preserve all data`() {
        val createdAt = LocalDateTime.of(2025, 1, 15, 10, 0)
        val original = SubjectGroup(
            id = 5L,
            name = "Physics",
            colorHex = "#4CAF50",
            displayOrder = 2,
            createdAt = createdAt
        )

        val roundTripped = mapper.toDomain(mapper.toEntity(original))

        assertEquals(original.id, roundTripped.id)
        assertEquals(original.name, roundTripped.name)
        assertEquals(original.colorHex, roundTripped.colorHex)
        assertEquals(original.displayOrder, roundTripped.displayOrder)
        assertEquals(original.createdAt, roundTripped.createdAt)
    }

    // ==================== List テスト ====================

    @Test
    fun `toDomainList should map all entities`() {
        val entities = listOf(
            SubjectGroupEntity(id = 1L, name = "Math"),
            SubjectGroupEntity(id = 2L, name = "Science"),
            SubjectGroupEntity(id = 3L, name = "History")
        )

        val domains = mapper.toDomainList(entities)

        assertEquals(3, domains.size)
        assertEquals("Math", domains[0].name)
        assertEquals("Science", domains[1].name)
        assertEquals("History", domains[2].name)
    }

    @Test
    fun `toEntityList should map all domains`() {
        val domains = listOf(
            SubjectGroup(id = 1L, name = "Math"),
            SubjectGroup(id = 2L, name = "Science")
        )

        val entities = mapper.toEntityList(domains)

        assertEquals(2, entities.size)
        assertEquals("Math", entities[0].name)
        assertEquals("Science", entities[1].name)
    }
}
