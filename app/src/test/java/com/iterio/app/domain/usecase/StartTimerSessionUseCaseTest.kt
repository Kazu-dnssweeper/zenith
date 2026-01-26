package com.iterio.app.domain.usecase

import com.iterio.app.domain.common.DomainError
import com.iterio.app.domain.common.Result
import com.iterio.app.domain.model.PomodoroSettings
import com.iterio.app.domain.model.StudySession
import com.iterio.app.domain.model.Task
import com.iterio.app.domain.repository.StudySessionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

/**
 * StartTimerSessionUseCase のユニットテスト
 */
class StartTimerSessionUseCaseTest {

    private lateinit var studySessionRepository: StudySessionRepository
    private lateinit var useCase: StartTimerSessionUseCase

    @Before
    fun setup() {
        studySessionRepository = mockk()
        useCase = StartTimerSessionUseCase(studySessionRepository)
    }

    @Test
    fun `creates session with task id`() = runTest {
        val task = Task(id = 10L, groupId = 1L, name = "Study")
        val sessionSlot = slot<StudySession>()
        coEvery { studySessionRepository.insertSession(capture(sessionSlot)) } returns Result.Success(1L)

        useCase(task, PomodoroSettings(), 4)

        assertEquals(10L, sessionSlot.captured.taskId)
    }

    @Test
    fun `creates session with correct planned duration`() = runTest {
        val task = Task(id = 1L, groupId = 1L, name = "Study", workDurationMinutes = 25)
        val settings = PomodoroSettings(workDurationMinutes = 30)
        val sessionSlot = slot<StudySession>()
        coEvery { studySessionRepository.insertSession(capture(sessionSlot)) } returns Result.Success(1L)

        useCase(task, settings, 4)

        // Uses task-specific duration (25) * cycles (4) = 100 minutes
        assertEquals(100, sessionSlot.captured.plannedDurationMinutes)
    }

    @Test
    fun `uses settings duration when task has no specific duration`() = runTest {
        val task = Task(id = 1L, groupId = 1L, name = "Study", workDurationMinutes = null)
        val settings = PomodoroSettings(workDurationMinutes = 30)
        val sessionSlot = slot<StudySession>()
        coEvery { studySessionRepository.insertSession(capture(sessionSlot)) } returns Result.Success(1L)

        useCase(task, settings, 2)

        // Uses settings duration (30) * cycles (2) = 60 minutes
        assertEquals(60, sessionSlot.captured.plannedDurationMinutes)
    }

    @Test
    fun `returns session id on success`() = runTest {
        val task = Task(id = 1L, groupId = 1L, name = "Study")
        coEvery { studySessionRepository.insertSession(any()) } returns Result.Success(42L)

        val result = useCase(task, PomodoroSettings(), 4)

        assertTrue(result.isSuccess)
        assertEquals(42L, result.getOrNull())
    }

    @Test
    fun `sets start time close to now`() = runTest {
        val task = Task(id = 1L, groupId = 1L, name = "Study")
        val sessionSlot = slot<StudySession>()
        coEvery { studySessionRepository.insertSession(capture(sessionSlot)) } returns Result.Success(1L)

        val before = LocalDateTime.now()
        useCase(task, PomodoroSettings(), 4)
        val after = LocalDateTime.now()

        val startedAt = sessionSlot.captured.startedAt
        assertTrue(startedAt >= before.minusSeconds(1))
        assertTrue(startedAt <= after.plusSeconds(1))
    }

    @Test
    fun `uses custom start time when provided`() = runTest {
        val task = Task(id = 1L, groupId = 1L, name = "Study")
        val customStartTime = LocalDateTime.of(2025, 1, 15, 10, 0)
        val sessionSlot = slot<StudySession>()
        coEvery { studySessionRepository.insertSession(capture(sessionSlot)) } returns Result.Success(1L)

        useCase(task, PomodoroSettings(), 4, customStartTime)

        assertEquals(customStartTime, sessionSlot.captured.startedAt)
    }

    @Test
    fun `returns failure when repository throws exception`() = runTest {
        val task = Task(id = 1L, groupId = 1L, name = "Study")
        coEvery { studySessionRepository.insertSession(any()) } returns Result.Failure(DomainError.DatabaseError("DB error"))

        val result = useCase(task, PomodoroSettings(), 4)

        assertTrue(result.isFailure)
    }

    @Test
    fun `calls repository insert exactly once`() = runTest {
        val task = Task(id = 1L, groupId = 1L, name = "Study")
        coEvery { studySessionRepository.insertSession(any()) } returns Result.Success(1L)

        useCase(task, PomodoroSettings(), 4)

        coVerify(exactly = 1) { studySessionRepository.insertSession(any()) }
    }
}
