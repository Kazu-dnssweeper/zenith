package com.iterio.app.data.repository

import app.cash.turbine.test
import com.iterio.app.data.local.dao.SubjectDao
import com.iterio.app.data.local.entity.SubjectEntity
import com.iterio.app.domain.common.Result
import com.iterio.app.domain.model.Subject
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

/**
 * SubjectRepositoryImpl のユニットテスト
 */
class SubjectRepositoryImplTest {

    private lateinit var subjectDao: SubjectDao
    private lateinit var repository: SubjectRepositoryImpl

    @Before
    fun setup() {
        subjectDao = mockk()
        repository = SubjectRepositoryImpl(subjectDao)
    }

    @Test
    fun `insert sets displayOrder and returns id`() = runTest {
        val subject = createSubject(id = 0, name = "Math")
        coEvery { subjectDao.getMaxDisplayOrder() } returns 5
        coEvery { subjectDao.insert(any()) } returns 42L

        val result = repository.insert(subject)

        assertTrue(result.isSuccess)
        assertEquals(42L, (result as Result.Success).value)
        coVerify { subjectDao.getMaxDisplayOrder() }
        coVerify { subjectDao.insert(match { it.displayOrder == 6 }) }
    }

    @Test
    fun `insert uses 1 when no existing subjects`() = runTest {
        val subject = createSubject(id = 0, name = "First Subject")
        coEvery { subjectDao.getMaxDisplayOrder() } returns null
        coEvery { subjectDao.insert(any()) } returns 1L

        val result = repository.insert(subject)

        assertTrue(result.isSuccess)
        coVerify { subjectDao.insert(match { it.displayOrder == 1 }) }
    }

    @Test
    fun `update calls dao`() = runTest {
        val subject = createSubject(id = 1, name = "Updated Subject")
        coEvery { subjectDao.update(any()) } returns Unit

        repository.update(subject)

        coVerify { subjectDao.update(any()) }
    }

    @Test
    fun `delete calls dao`() = runTest {
        val subject = createSubject(id = 1, name = "To Delete")
        coEvery { subjectDao.delete(any()) } returns Unit

        repository.delete(subject)

        coVerify { subjectDao.delete(any()) }
    }

    @Test
    fun `getById returns subject when exists`() = runTest {
        val entity = createEntity(id = 1, name = "Science")
        coEvery { subjectDao.getById(1) } returns entity

        val result = repository.getById(1)

        assertTrue(result.isSuccess)
        val subject = (result as Result.Success).value
        assertNotNull(subject)
        assertEquals("Science", subject?.name)
        assertEquals(1L, subject?.id)
    }

    @Test
    fun `getById returns null when not exists`() = runTest {
        coEvery { subjectDao.getById(999) } returns null

        val result = repository.getById(999)

        assertTrue(result.isSuccess)
        val subject = (result as Result.Success).value
        assertNull(subject)
    }

    @Test
    fun `getAllSubjects returns flow of subjects`() = runTest {
        val entities = listOf(
            createEntity(id = 1, name = "Math", displayOrder = 0),
            createEntity(id = 2, name = "English", displayOrder = 1)
        )
        every { subjectDao.getAllSubjects() } returns flowOf(entities)

        repository.getAllSubjects().test {
            val subjects = awaitItem()
            assertEquals(2, subjects.size)
            assertEquals("Math", subjects[0].name)
            assertEquals("English", subjects[1].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAllSubjects returns empty list when no subjects`() = runTest {
        every { subjectDao.getAllSubjects() } returns flowOf(emptyList())

        repository.getAllSubjects().test {
            val subjects = awaitItem()
            assertTrue(subjects.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getTemplateSubjects returns only templates`() = runTest {
        val entities = listOf(
            createEntity(id = 1, name = "Template 1", isTemplate = true),
            createEntity(id = 2, name = "Template 2", isTemplate = true)
        )
        every { subjectDao.getTemplateSubjects() } returns flowOf(entities)

        repository.getTemplateSubjects().test {
            val subjects = awaitItem()
            assertEquals(2, subjects.size)
            assertTrue(subjects.all { it.isTemplate })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchSubjects returns matching subjects`() = runTest {
        val entities = listOf(
            createEntity(id = 1, name = "Mathematics"),
            createEntity(id = 2, name = "Mathematical Analysis")
        )
        every { subjectDao.searchSubjects("Math") } returns flowOf(entities)

        repository.searchSubjects("Math").test {
            val subjects = awaitItem()
            assertEquals(2, subjects.size)
            assertTrue(subjects.all { it.name.contains("Math") })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchSubjects returns empty when no matches`() = runTest {
        every { subjectDao.searchSubjects("xyz") } returns flowOf(emptyList())

        repository.searchSubjects("xyz").test {
            val subjects = awaitItem()
            assertTrue(subjects.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `entity to domain conversion preserves all fields`() = runTest {
        val createdAt = LocalDateTime.of(2024, 1, 15, 10, 30)
        val entity = SubjectEntity(
            id = 1,
            name = "Test Subject",
            colorHex = "#FF5733",
            isTemplate = true,
            displayOrder = 5,
            createdAt = createdAt
        )
        coEvery { subjectDao.getById(1) } returns entity

        val result = repository.getById(1)

        assertTrue(result.isSuccess)
        val subject = (result as Result.Success).value
        assertNotNull(subject)
        assertEquals(1L, subject?.id)
        assertEquals("Test Subject", subject?.name)
        assertEquals("#FF5733", subject?.colorHex)
        assertTrue(subject?.isTemplate == true)
        assertEquals(5, subject?.displayOrder)
        assertEquals(createdAt, subject?.createdAt)
    }

    @Test
    fun `domain to entity conversion preserves all fields`() = runTest {
        val createdAt = LocalDateTime.of(2024, 1, 15, 10, 30)
        val subject = Subject(
            id = 2,
            name = "New Subject",
            colorHex = "#00838F",
            isTemplate = false,
            displayOrder = 3,
            createdAt = createdAt
        )
        coEvery { subjectDao.update(any()) } returns Unit

        repository.update(subject)

        coVerify {
            subjectDao.update(match {
                it.id == 2L &&
                it.name == "New Subject" &&
                it.colorHex == "#00838F" &&
                !it.isTemplate &&
                it.displayOrder == 3 &&
                it.createdAt == createdAt
            })
        }
    }

    // ==================== Helpers ====================

    private fun createEntity(
        id: Long = 0,
        name: String = "Test Subject",
        colorHex: String = "#00838F",
        isTemplate: Boolean = false,
        displayOrder: Int = 0
    ) = SubjectEntity(
        id = id,
        name = name,
        colorHex = colorHex,
        isTemplate = isTemplate,
        displayOrder = displayOrder,
        createdAt = LocalDateTime.now()
    )

    private fun createSubject(
        id: Long = 0,
        name: String = "Test Subject",
        colorHex: String = "#00838F",
        isTemplate: Boolean = false,
        displayOrder: Int = 0
    ) = Subject(
        id = id,
        name = name,
        colorHex = colorHex,
        isTemplate = isTemplate,
        displayOrder = displayOrder,
        createdAt = LocalDateTime.now()
    )
}
