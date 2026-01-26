package com.iterio.app.fakes

import app.cash.turbine.test
import com.iterio.app.domain.common.Result
import com.iterio.app.testutil.CoroutineTestRule
import com.iterio.app.testutil.TestDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FakeSubjectGroupRepositoryTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var repository: FakeSubjectGroupRepository

    @Before
    fun setup() {
        repository = FakeSubjectGroupRepository()
    }

    @Test
    fun `getAllGroups returns empty flow initially`() = runTest {
        repository.getAllGroups().test {
            val groups = awaitItem()
            assertTrue(groups.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `insertGroup adds group and returns id`() = runTest {
        val group = TestDataFactory.createSubjectGroup(name = "Math")

        val id = (repository.insertGroup(group) as Result.Success).value

        assertEquals(1L, id)
        val saved = (repository.getGroupById(id) as Result.Success).value
        assertNotNull(saved)
        assertEquals("Math", saved?.name)
    }

    @Test
    fun `getAllGroups emits updates after insert`() = runTest {
        repository.getAllGroups().test {
            // Initial empty state
            assertEquals(0, awaitItem().size)

            // Insert a group
            repository.insertGroup(TestDataFactory.createSubjectGroup(name = "Science"))

            // Should emit update
            val updated = awaitItem()
            assertEquals(1, updated.size)
            assertEquals("Science", updated[0].name)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateGroup modifies existing group`() = runTest {
        val id = (repository.insertGroup(TestDataFactory.createSubjectGroup(name = "English")) as Result.Success).value
        val inserted = (repository.getGroupById(id) as Result.Success).value!!

        repository.updateGroup(inserted.copy(name = "Japanese"))

        val updated = (repository.getGroupById(id) as Result.Success).value
        assertEquals("Japanese", updated?.name)
    }

    @Test
    fun `deleteGroup removes group`() = runTest {
        val id = (repository.insertGroup(TestDataFactory.createSubjectGroup(name = "History")) as Result.Success).value

        val group = (repository.getGroupById(id) as Result.Success).value!!
        repository.deleteGroup(group)

        assertNull((repository.getGroupById(id) as Result.Success).value)
    }

    @Test
    fun `deleteGroupById removes group by id`() = runTest {
        val id = (repository.insertGroup(TestDataFactory.createSubjectGroup(name = "Physics")) as Result.Success).value

        repository.deleteGroupById(id)

        assertNull((repository.getGroupById(id) as Result.Success).value)
    }

    @Test
    fun `getAllGroups returns groups sorted by displayOrder`() = runTest {
        val group1 = TestDataFactory.createSubjectGroup(name = "C", displayOrder = 2)
        val group2 = TestDataFactory.createSubjectGroup(name = "A", displayOrder = 0)
        val group3 = TestDataFactory.createSubjectGroup(name = "B", displayOrder = 1)

        repository.setGroups(listOf(group1, group2, group3))

        repository.getAllGroups().test {
            val groups = awaitItem()
            assertEquals("A", groups[0].name)
            assertEquals("B", groups[1].name)
            assertEquals("C", groups[2].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clear removes all data`() = runTest {
        repository.insertGroup(TestDataFactory.createSubjectGroup(name = "Test1"))
        repository.insertGroup(TestDataFactory.createSubjectGroup(name = "Test2"))

        repository.clear()

        assertTrue(repository.getGroupsSnapshot().isEmpty())
    }

    @Test
    fun `setGroups replaces all data`() = runTest {
        repository.insertGroup(TestDataFactory.createSubjectGroup(name = "Old"))

        val newGroups = TestDataFactory.createSubjectGroups(3)
        repository.setGroups(newGroups)

        assertEquals(3, repository.getGroupsSnapshot().size)
        assertEquals("Group 1", repository.getGroupsSnapshot()[0].name)
    }
}
