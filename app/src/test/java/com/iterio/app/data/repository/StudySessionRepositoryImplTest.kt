package com.iterio.app.data.repository

import app.cash.turbine.test
import com.iterio.app.data.local.dao.StudySessionDao
import com.iterio.app.data.local.entity.StudySessionEntity
import com.iterio.app.data.mapper.StudySessionMapper
import com.iterio.app.domain.common.Result
import com.iterio.app.domain.model.StudySession
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * StudySessionRepositoryImpl のユニットテスト
 */
class StudySessionRepositoryImplTest {

    private lateinit var studySessionDao: StudySessionDao
    private lateinit var mapper: StudySessionMapper
    private lateinit var repository: StudySessionRepositoryImpl

    @Before
    fun setup() {
        studySessionDao = mockk()
        mapper = StudySessionMapper()
        repository = StudySessionRepositoryImpl(studySessionDao, mapper)
    }

    @Test
    fun `getAllSessions returns all sessions`() = runTest {
        val entities = listOf(
            createEntity(id = 1, taskId = 1),
            createEntity(id = 2, taskId = 2)
        )
        every { studySessionDao.getAllSessions() } returns flowOf(entities)

        repository.getAllSessions().test {
            val sessions = awaitItem()
            assertEquals(2, sessions.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getSessionsByTask returns sessions for specific task`() = runTest {
        val entities = listOf(
            createEntity(id = 1, taskId = 5),
            createEntity(id = 2, taskId = 5)
        )
        every { studySessionDao.getSessionsByTask(5) } returns flowOf(entities)

        repository.getSessionsByTask(5).test {
            val sessions = awaitItem()
            assertEquals(2, sessions.size)
            assertTrue(sessions.all { it.taskId == 5L })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getSessionsForDay returns sessions for specific day`() = runTest {
        val today = LocalDate.now()
        val entities = listOf(
            createEntity(id = 1, startedAt = today.atTime(10, 0)),
            createEntity(id = 2, startedAt = today.atTime(14, 0))
        )
        every { studySessionDao.getSessionsForDay(any(), any()) } returns flowOf(entities)

        repository.getSessionsForDay(today).test {
            val sessions = awaitItem()
            assertEquals(2, sessions.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getSessionById returns session when exists`() = runTest {
        val entity = createEntity(id = 1, taskId = 5, workDurationMinutes = 25)
        coEvery { studySessionDao.getSessionById(1) } returns entity

        val result = repository.getSessionById(1)

        assertTrue(result.isSuccess)
        val session = (result as Result.Success).value
        assertNotNull(session)
        assertEquals(25, session?.workDurationMinutes)
    }

    @Test
    fun `getSessionById returns null when not exists`() = runTest {
        coEvery { studySessionDao.getSessionById(999) } returns null

        val result = repository.getSessionById(999)

        assertTrue(result.isSuccess)
        val session = (result as Result.Success).value
        assertNull(session)
    }

    @Test
    fun `getTotalMinutesForDay returns correct total`() = runTest {
        val today = LocalDate.now()
        coEvery { studySessionDao.getTotalMinutesForDay(any(), any()) } returns 120

        val result = repository.getTotalMinutesForDay(today)

        assertTrue(result.isSuccess)
        assertEquals(120, (result as Result.Success).value)
    }

    @Test
    fun `getTotalMinutesForDay returns 0 for no sessions`() = runTest {
        val today = LocalDate.now()
        coEvery { studySessionDao.getTotalMinutesForDay(any(), any()) } returns 0

        val result = repository.getTotalMinutesForDay(today)

        assertTrue(result.isSuccess)
        assertEquals(0, (result as Result.Success).value)
    }

    @Test
    fun `getTotalCyclesForDay returns correct total`() = runTest {
        val today = LocalDate.now()
        coEvery { studySessionDao.getTotalCyclesForDay(any(), any()) } returns 4

        val result = repository.getTotalCyclesForDay(today)

        assertTrue(result.isSuccess)
        assertEquals(4, (result as Result.Success).value)
    }

    @Test
    fun `insertSession calls dao and returns id`() = runTest {
        val session = createSession(id = 0, taskId = 1)
        coEvery { studySessionDao.insertSession(any()) } returns 42L

        val result = repository.insertSession(session)

        assertTrue(result.isSuccess)
        assertEquals(42L, (result as Result.Success).value)
        coVerify { studySessionDao.insertSession(any()) }
    }

    @Test
    fun `updateSession calls dao`() = runTest {
        val session = createSession(id = 1, taskId = 1)
        coEvery { studySessionDao.updateSession(any()) } returns Unit

        repository.updateSession(session)

        coVerify { studySessionDao.updateSession(any()) }
    }

    @Test
    fun `deleteSession calls dao`() = runTest {
        val session = createSession(id = 1, taskId = 1)
        coEvery { studySessionDao.deleteSession(any()) } returns Unit

        repository.deleteSession(session)

        coVerify { studySessionDao.deleteSession(any()) }
    }

    @Test
    fun `finishSession calls dao with correct parameters`() = runTest {
        val endedAtSlot = slot<LocalDateTime>()
        coEvery {
            studySessionDao.finishSession(
                id = 1,
                endedAt = capture(endedAtSlot),
                durationMinutes = 25,
                cycles = 1,
                interrupted = false
            )
        } returns Unit

        repository.finishSession(1, 25, 1, false)

        coVerify {
            studySessionDao.finishSession(
                id = 1,
                endedAt = any(),
                durationMinutes = 25,
                cycles = 1,
                interrupted = false
            )
        }
        assertNotNull(endedAtSlot.captured)
    }

    @Test
    fun `finishSession with interrupted flag`() = runTest {
        coEvery { studySessionDao.finishSession(any(), any(), any(), any(), any()) } returns Unit

        repository.finishSession(1, 15, 0, true)

        coVerify {
            studySessionDao.finishSession(
                id = 1,
                endedAt = any(),
                durationMinutes = 15,
                cycles = 0,
                interrupted = true
            )
        }
    }

    @Test
    fun `getSessionCount returns correct count`() = runTest {
        coEvery { studySessionDao.getSessionCount() } returns 150

        val result = repository.getSessionCount()

        assertTrue(result.isSuccess)
        assertEquals(150, (result as Result.Success).value)
    }

    @Test
    fun `getSessionCount returns 0 for empty database`() = runTest {
        coEvery { studySessionDao.getSessionCount() } returns 0

        val result = repository.getSessionCount()

        assertTrue(result.isSuccess)
        assertEquals(0, (result as Result.Success).value)
    }

    // ==================== Helpers ====================

    private fun createEntity(
        id: Long = 0,
        taskId: Long = 1,
        startedAt: LocalDateTime = LocalDateTime.now(),
        endedAt: LocalDateTime? = null,
        workDurationMinutes: Int = 25,
        cyclesCompleted: Int = 1,
        wasInterrupted: Boolean = false
    ) = StudySessionEntity(
        id = id,
        taskId = taskId,
        startedAt = startedAt,
        endedAt = endedAt,
        workDurationMinutes = workDurationMinutes,
        cyclesCompleted = cyclesCompleted,
        wasInterrupted = wasInterrupted
    )

    private fun createSession(
        id: Long = 0,
        taskId: Long = 1,
        startedAt: LocalDateTime = LocalDateTime.now(),
        workDurationMinutes: Int = 25
    ) = StudySession(
        id = id,
        taskId = taskId,
        startedAt = startedAt,
        workDurationMinutes = workDurationMinutes
    )
}
