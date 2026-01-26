package com.iterio.app.data.mapper

import com.iterio.app.data.local.entity.StudySessionEntity
import com.iterio.app.domain.model.StudySession
import javax.inject.Inject
import javax.inject.Singleton

/**
 * StudySessionEntity と StudySession 間の変換を行うマッパー
 */
@Singleton
class StudySessionMapper @Inject constructor() : Mapper<StudySessionEntity, StudySession> {

    override fun toDomain(entity: StudySessionEntity): StudySession {
        return StudySession(
            id = entity.id,
            taskId = entity.taskId,
            startedAt = entity.startedAt,
            endedAt = entity.endedAt,
            workDurationMinutes = entity.workDurationMinutes,
            plannedDurationMinutes = entity.plannedDurationMinutes,
            cyclesCompleted = entity.cyclesCompleted,
            wasInterrupted = entity.wasInterrupted,
            notes = entity.notes
        )
    }

    override fun toEntity(domain: StudySession): StudySessionEntity {
        return StudySessionEntity(
            id = domain.id,
            taskId = domain.taskId,
            startedAt = domain.startedAt,
            endedAt = domain.endedAt,
            workDurationMinutes = domain.workDurationMinutes,
            plannedDurationMinutes = domain.plannedDurationMinutes,
            cyclesCompleted = domain.cyclesCompleted,
            wasInterrupted = domain.wasInterrupted,
            notes = domain.notes
        )
    }
}
