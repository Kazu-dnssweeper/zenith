package com.iterio.app.fakes

import com.iterio.app.domain.model.StudySession
import com.iterio.app.domain.repository.StudySessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * テスト用の StudySessionRepository 実装
 */
class FakeStudySessionRepository : StudySessionRepository {

    private val sessions = MutableStateFlow<Map<Long, StudySession>>(emptyMap())
    private var nextId = 1L

    override fun getAllSessions(): Flow<List<StudySession>> =
        sessions.map { it.values.toList() }

    override fun getSessionsByTask(taskId: Long): Flow<List<StudySession>> =
        sessions.map { map ->
            map.values.filter { it.taskId == taskId }
        }

    override fun getSessionsForDay(date: LocalDate): Flow<List<StudySession>> =
        sessions.map { map ->
            map.values.filter { it.startedAt.toLocalDate() == date }
        }

    override suspend fun getSessionById(id: Long): StudySession? =
        sessions.value[id]

    override suspend fun getTotalMinutesForDay(date: LocalDate): Int =
        sessions.value.values
            .filter { it.startedAt.toLocalDate() == date }
            .sumOf { it.workDurationMinutes }

    override suspend fun getTotalCyclesForDay(date: LocalDate): Int =
        sessions.value.values
            .filter { it.startedAt.toLocalDate() == date }
            .sumOf { it.cyclesCompleted }

    override suspend fun insertSession(session: StudySession): Long {
        val id = nextId++
        val sessionWithId = session.copy(id = id)
        sessions.value = sessions.value + (id to sessionWithId)
        return id
    }

    override suspend fun updateSession(session: StudySession) {
        sessions.value = sessions.value + (session.id to session)
    }

    override suspend fun deleteSession(session: StudySession) {
        sessions.value = sessions.value - session.id
    }

    override suspend fun finishSession(
        id: Long,
        durationMinutes: Int,
        cycles: Int,
        interrupted: Boolean
    ) {
        val session = sessions.value[id] ?: return
        val finished = session.copy(
            workDurationMinutes = durationMinutes,
            cyclesCompleted = cycles,
            wasInterrupted = interrupted,
            endedAt = LocalDateTime.now()
        )
        sessions.value = sessions.value + (id to finished)
    }

    override suspend fun getSessionCount(): Int = sessions.value.size

    // Test helpers
    fun clear() {
        sessions.value = emptyMap()
        nextId = 1L
    }
}
