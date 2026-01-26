package com.iterio.app.data.repository

import app.cash.turbine.test
import com.iterio.app.data.local.dao.SubjectGroupDao
import com.iterio.app.data.local.entity.SubjectGroupEntity
import com.iterio.app.data.mapper.SubjectGroupMapper
import com.iterio.app.domain.common.Result
import com.iterio.app.domain.model.SubjectGroup
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
 * SubjectGroupRepositoryImpl のユニットテスト
 */
class SubjectGroupRepositoryImplTest {

    private lateinit var subjectGroupDao: SubjectGroupDao
    private lateinit var mapper: SubjectGroupMapper
    private lateinit var repository: SubjectGroupRepositoryImpl

    @Before
    fun setup() {
        subjectGroupDao = mockk()
        mapper = SubjectGroupMapper()
        repository = SubjectGroupRepositoryImpl(subjectGroupDao, mapper)
    }

    @Test
    fun `getAllGroups returns all groups in order`() = runTest {
        val entities = listOf(
            createEntity(id = 1, name = "Math", displayOrder = 0),
            createEntity(id = 2, name = "English", displayOrder = 1)
        )
        every { subjectGroupDao.getAllGroups() } returns flowOf(entities)

        repository.getAllGroups().test {
            val groups = awaitItem()
            assertEquals(2, groups.size)
            assertEquals("Math", groups[0].name)
            assertEquals("English", groups[1].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAllGroups returns empty list when no groups`() = runTest {
        every { subjectGroupDao.getAllGroups() } returns flowOf(emptyList())

        repository.getAllGroups().test {
            val groups = awaitItem()
            assertTrue(groups.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getGroupById returns group when exists`() = runTest {
        val entity = createEntity(id = 1, name = "Science")
        coEvery { subjectGroupDao.getGroupById(1) } returns entity

        val result = repository.getGroupById(1)

        assertTrue(result.isSuccess)
        val group = (result as Result.Success).value
        assertNotNull(group)
        assertEquals("Science", group?.name)
    }

    @Test
    fun `getGroupById returns null when not exists`() = runTest {
        coEvery { subjectGroupDao.getGroupById(999) } returns null

        val result = repository.getGroupById(999)

        assertTrue(result.isSuccess)
        val group = (result as Result.Success).value
        assertNull(group)
    }

    @Test
    fun `insertGroup sets displayOrder and returns id`() = runTest {
        val group = createGroup(id = 0, name = "New Group")
        coEvery { subjectGroupDao.getNextDisplayOrder() } returns 5
        coEvery { subjectGroupDao.insertGroup(any()) } returns 42L

        val result = repository.insertGroup(group)

        assertTrue(result.isSuccess)
        assertEquals(42L, (result as Result.Success).value)
        coVerify { subjectGroupDao.getNextDisplayOrder() }
        coVerify { subjectGroupDao.insertGroup(match { it.displayOrder == 5 }) }
    }

    @Test
    fun `insertGroup uses first order for first group`() = runTest {
        val group = createGroup(id = 0, name = "First Group")
        coEvery { subjectGroupDao.getNextDisplayOrder() } returns 1
        coEvery { subjectGroupDao.insertGroup(any()) } returns 1L

        val result = repository.insertGroup(group)

        assertTrue(result.isSuccess)
        coVerify { subjectGroupDao.insertGroup(match { it.displayOrder == 1 }) }
    }

    @Test
    fun `updateGroup calls dao`() = runTest {
        val group = createGroup(id = 1, name = "Updated Group")
        coEvery { subjectGroupDao.updateGroup(any()) } returns Unit

        repository.updateGroup(group)

        coVerify { subjectGroupDao.updateGroup(any()) }
    }

    @Test
    fun `deleteGroup calls dao`() = runTest {
        val group = createGroup(id = 1, name = "To Delete")
        coEvery { subjectGroupDao.deleteGroup(any()) } returns Unit

        repository.deleteGroup(group)

        coVerify { subjectGroupDao.deleteGroup(any()) }
    }

    @Test
    fun `deleteGroupById calls dao`() = runTest {
        coEvery { subjectGroupDao.deleteGroupById(1) } returns Unit

        repository.deleteGroupById(1)

        coVerify { subjectGroupDao.deleteGroupById(1) }
    }

    @Test
    fun `mapper converts entity to domain correctly`() {
        val entity = createEntity(
            id = 1,
            name = "Test Group",
            colorHex = "#FF5733",
            displayOrder = 2
        )

        val domain = mapper.toDomain(entity)

        assertEquals(1L, domain.id)
        assertEquals("Test Group", domain.name)
        assertEquals("#FF5733", domain.colorHex)
        assertEquals(2, domain.displayOrder)
    }

    @Test
    fun `mapper converts domain to entity correctly`() {
        val domain = createGroup(
            id = 1,
            name = "Test Group",
            colorHex = "#00838F",
            displayOrder = 3
        )

        val entity = mapper.toEntity(domain)

        assertEquals(1L, entity.id)
        assertEquals("Test Group", entity.name)
        assertEquals("#00838F", entity.colorHex)
        assertEquals(3, entity.displayOrder)
    }

    // ==================== Helpers ====================

    private fun createEntity(
        id: Long = 0,
        name: String = "Test Group",
        colorHex: String = "#00838F",
        displayOrder: Int = 0
    ) = SubjectGroupEntity(
        id = id,
        name = name,
        colorHex = colorHex,
        displayOrder = displayOrder,
        createdAt = LocalDateTime.now()
    )

    private fun createGroup(
        id: Long = 0,
        name: String = "Test Group",
        colorHex: String = "#00838F",
        displayOrder: Int = 0
    ) = SubjectGroup(
        id = id,
        name = name,
        colorHex = colorHex,
        displayOrder = displayOrder,
        createdAt = LocalDateTime.now()
    )
}
