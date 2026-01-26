package com.iterio.app.data.mapper

import com.google.gson.Gson
import com.iterio.app.data.local.entity.DailyStatsEntity
import com.iterio.app.domain.model.DailyStats
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

/**
 * DailyStatsMapper のユニットテスト
 */
class DailyStatsMapperTest {

    private lateinit var mapper: DailyStatsMapper
    private lateinit var gson: Gson

    @Before
    fun setup() {
        gson = Gson()
        mapper = DailyStatsMapper(gson)
    }

    // ==================== toDomain テスト ====================

    @Test
    fun `toDomain should map basic fields correctly`() {
        val date = LocalDate.of(2025, 1, 15)
        val entity = DailyStatsEntity(
            date = date,
            totalStudyMinutes = 120,
            sessionCount = 4,
            subjectBreakdownJson = "{}"
        )

        val domain = mapper.toDomain(entity)

        assertEquals(date, domain.date)
        assertEquals(120, domain.totalStudyMinutes)
        assertEquals(4, domain.sessionCount)
    }

    @Test
    fun `toDomain should parse subjectBreakdown JSON correctly`() {
        val entity = DailyStatsEntity(
            date = LocalDate.now(),
            totalStudyMinutes = 90,
            sessionCount = 3,
            subjectBreakdownJson = """{"Mathematics":45,"Physics":30,"Chemistry":15}"""
        )

        val domain = mapper.toDomain(entity)

        assertEquals(3, domain.subjectBreakdown.size)
        assertEquals(45, domain.subjectBreakdown["Mathematics"])
        assertEquals(30, domain.subjectBreakdown["Physics"])
        assertEquals(15, domain.subjectBreakdown["Chemistry"])
    }

    @Test
    fun `toDomain should handle empty JSON as empty map`() {
        val entity = DailyStatsEntity(
            date = LocalDate.now(),
            subjectBreakdownJson = "{}"
        )

        val domain = mapper.toDomain(entity)

        assertTrue(domain.subjectBreakdown.isEmpty())
    }

    @Test
    fun `toDomain should handle blank JSON as empty map`() {
        val entity = DailyStatsEntity(
            date = LocalDate.now(),
            subjectBreakdownJson = ""
        )

        val domain = mapper.toDomain(entity)

        assertTrue(domain.subjectBreakdown.isEmpty())
    }

    @Test
    fun `toDomain should handle malformed JSON gracefully`() {
        val entity = DailyStatsEntity(
            date = LocalDate.now(),
            subjectBreakdownJson = "not valid json"
        )

        val domain = mapper.toDomain(entity)

        assertTrue(domain.subjectBreakdown.isEmpty())
    }

    // ==================== toEntity テスト ====================

    @Test
    fun `toEntity should map basic fields correctly`() {
        val date = LocalDate.of(2025, 1, 15)
        val domain = DailyStats(
            date = date,
            totalStudyMinutes = 120,
            sessionCount = 4,
            subjectBreakdown = emptyMap()
        )

        val entity = mapper.toEntity(domain)

        assertEquals(date, entity.date)
        assertEquals(120, entity.totalStudyMinutes)
        assertEquals(4, entity.sessionCount)
    }

    @Test
    fun `toEntity should serialize subjectBreakdown to JSON`() {
        val domain = DailyStats(
            date = LocalDate.now(),
            totalStudyMinutes = 90,
            sessionCount = 3,
            subjectBreakdown = mapOf(
                "Mathematics" to 45,
                "Physics" to 30,
                "Chemistry" to 15
            )
        )

        val entity = mapper.toEntity(domain)

        // Parse the JSON back and verify
        val parsedMap = gson.fromJson<Map<String, Double>>(
            entity.subjectBreakdownJson,
            Map::class.java
        )
        assertEquals(45.0, parsedMap["Mathematics"])
        assertEquals(30.0, parsedMap["Physics"])
        assertEquals(15.0, parsedMap["Chemistry"])
    }

    @Test
    fun `toEntity should serialize empty map to empty JSON object`() {
        val domain = DailyStats(
            date = LocalDate.now(),
            subjectBreakdown = emptyMap()
        )

        val entity = mapper.toEntity(domain)

        assertEquals("{}", entity.subjectBreakdownJson)
    }

    // ==================== Round Trip テスト ====================

    @Test
    fun `round trip should preserve all data`() {
        val date = LocalDate.of(2025, 2, 10)
        val original = DailyStats(
            date = date,
            totalStudyMinutes = 180,
            sessionCount = 6,
            subjectBreakdown = mapOf(
                "Math" to 60,
                "Science" to 75,
                "History" to 45
            )
        )

        val roundTripped = mapper.toDomain(mapper.toEntity(original))

        assertEquals(original.date, roundTripped.date)
        assertEquals(original.totalStudyMinutes, roundTripped.totalStudyMinutes)
        assertEquals(original.sessionCount, roundTripped.sessionCount)
        assertEquals(original.subjectBreakdown.size, roundTripped.subjectBreakdown.size)
        assertEquals(original.subjectBreakdown["Math"], roundTripped.subjectBreakdown["Math"])
        assertEquals(original.subjectBreakdown["Science"], roundTripped.subjectBreakdown["Science"])
        assertEquals(original.subjectBreakdown["History"], roundTripped.subjectBreakdown["History"])
    }

    @Test
    fun `round trip should preserve empty breakdown`() {
        val original = DailyStats(
            date = LocalDate.now(),
            totalStudyMinutes = 30,
            sessionCount = 1,
            subjectBreakdown = emptyMap()
        )

        val roundTripped = mapper.toDomain(mapper.toEntity(original))

        assertTrue(roundTripped.subjectBreakdown.isEmpty())
    }

    // ==================== List テスト ====================

    @Test
    fun `toDomainList should map all entities`() {
        val entities = listOf(
            DailyStatsEntity(date = LocalDate.of(2025, 1, 1), totalStudyMinutes = 60, sessionCount = 2),
            DailyStatsEntity(date = LocalDate.of(2025, 1, 2), totalStudyMinutes = 90, sessionCount = 3),
            DailyStatsEntity(date = LocalDate.of(2025, 1, 3), totalStudyMinutes = 45, sessionCount = 1)
        )

        val domains = mapper.toDomainList(entities)

        assertEquals(3, domains.size)
        assertEquals(60, domains[0].totalStudyMinutes)
        assertEquals(90, domains[1].totalStudyMinutes)
        assertEquals(45, domains[2].totalStudyMinutes)
    }
}
