package com.iterio.app.fakes

import com.iterio.app.domain.model.StudySession
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * FakeStudySessionRepository のテスト
 */
class FakeStudySessionRepositoryTest {

    private lateinit var repository: FakeStudySessionRepository

    @Before
    fun setup() {
        repository = FakeStudySessionRepository()
    }

    // Insert Tests

    @Test
    fun `insertSession returns incremented id`() = runTest {
        val session1 = createSession(taskId = 1L)
        val session2 = createSession(taskId = 2L)

        val id1 = repository.insertSession(session1)
        val id2 = repository.insertSession(session2)

        assertEquals(1L, id1)
        assertEquals(2L, id2)
    }

    @Test
    fun `insertSession adds session to repository`() = runTest {
        val session = createSession(taskId = 1L)

        val id = repository.insertSession(session)
        val retrieved = repository.getSessionById(id)

        assertNotNull(retrieved)
        assertEquals(1L, retrieved?.taskId)
    }

    // Get Tests

    @Test
    fun `getSessionById returns null for non-existent id`() = runTest {
        val result = repository.getSessionById(999L)
        assertNull(result)
    }

    @Test
    fun `getAllSessions returns all sessions`() = runTest {
        repository.insertSession(createSession(taskId = 1L))
        repository.insertSession(createSession(taskId = 2L))

        val sessions = repository.getAllSessions().first()

        assertEquals(2, sessions.size)
    }

    @Test
    fun `getSessionsByTask returns sessions for specific task`() = runTest {
        repository.insertSession(createSession(taskId = 1L))
        repository.insertSession(createSession(taskId = 1L))
        repository.insertSession(createSession(taskId = 2L))

        val sessions = repository.getSessionsByTask(1L).first()

        assertEquals(2, sessions.size)
        assertTrue(sessions.all { it.taskId == 1L })
    }

    @Test
    fun `getSessionsForDay returns sessions for specific day`() = runTest {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        repository.insertSession(createSession(taskId = 1L, startedAt = today.atTime(10, 0)))
        repository.insertSession(createSession(taskId = 2L, startedAt = today.atTime(14, 0)))
        repository.insertSession(createSession(taskId = 3L, startedAt = yesterday.atTime(10, 0)))

        val sessions = repository.getSessionsForDay(today).first()

        assertEquals(2, sessions.size)
    }

    // Update Tests

    @Test
    fun `updateSession updates existing session`() = runTest {
        val id = repository.insertSession(createSession(taskId = 1L, notes = "Original"))

        val session = repository.getSessionById(id)!!
        repository.updateSession(session.copy(notes = "Updated"))

        val updated = repository.getSessionById(id)
        assertEquals("Updated", updated?.notes)
    }

    @Test
    fun `finishSession updates session properly`() = runTest {
        val id = repository.insertSession(createSession(taskId = 1L))

        repository.finishSession(id, durationMinutes = 25, cycles = 4, interrupted = false)

        val session = repository.getSessionById(id)
        assertEquals(25, session?.workDurationMinutes)
        assertEquals(4, session?.cyclesCompleted)
        assertFalse(session?.wasInterrupted ?: true)
        assertNotNull(session?.endedAt)
    }

    // Delete Tests

    @Test
    fun `deleteSession removes session`() = runTest {
        val id = repository.insertSession(createSession(taskId = 1L))

        val session = repository.getSessionById(id)!!
        repository.deleteSession(session)

        assertNull(repository.getSessionById(id))
    }

    // Stats Tests

    @Test
    fun `getTotalMinutesForDay returns sum of duration`() = runTest {
        val today = LocalDate.now()

        repository.insertSession(createSession(taskId = 1L, startedAt = today.atTime(10, 0)))
        repository.finishSession(1L, durationMinutes = 25, cycles = 1, interrupted = false)

        repository.insertSession(createSession(taskId = 2L, startedAt = today.atTime(14, 0)))
        repository.finishSession(2L, durationMinutes = 30, cycles = 1, interrupted = false)

        val totalMinutes = repository.getTotalMinutesForDay(today)

        assertEquals(55, totalMinutes)
    }

    @Test
    fun `getTotalCyclesForDay returns sum of cycles`() = runTest {
        val today = LocalDate.now()

        repository.insertSession(createSession(taskId = 1L, startedAt = today.atTime(10, 0)))
        repository.finishSession(1L, durationMinutes = 25, cycles = 4, interrupted = false)

        repository.insertSession(createSession(taskId = 2L, startedAt = today.atTime(14, 0)))
        repository.finishSession(2L, durationMinutes = 30, cycles = 2, interrupted = false)

        val totalCycles = repository.getTotalCyclesForDay(today)

        assertEquals(6, totalCycles)
    }

    @Test
    fun `getSessionCount returns correct count`() = runTest {
        repository.insertSession(createSession(taskId = 1L))
        repository.insertSession(createSession(taskId = 2L))
        repository.insertSession(createSession(taskId = 3L))

        assertEquals(3, repository.getSessionCount())
    }

    // Helper function
    private fun createSession(
        taskId: Long,
        startedAt: LocalDateTime = LocalDateTime.now(),
        notes: String? = null
    ) = StudySession(
        taskId = taskId,
        startedAt = startedAt,
        notes = notes
    )
}
