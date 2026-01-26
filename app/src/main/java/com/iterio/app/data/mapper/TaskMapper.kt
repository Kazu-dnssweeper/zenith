package com.iterio.app.data.mapper

import com.iterio.app.data.local.entity.TaskEntity
import com.iterio.app.domain.model.ScheduleType
import com.iterio.app.domain.model.Task
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TaskEntity と Task 間の変換を行うマッパー
 */
@Singleton
class TaskMapper @Inject constructor() : Mapper<TaskEntity, Task> {

    override fun toDomain(entity: TaskEntity): Task {
        return Task(
            id = entity.id,
            groupId = entity.groupId,
            name = entity.name,
            progressNote = entity.progressNote,
            progressPercent = entity.progressPercent,
            nextGoal = entity.nextGoal,
            workDurationMinutes = entity.workDurationMinutes,
            isActive = entity.isActive,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            scheduleType = ScheduleType.fromString(entity.scheduleType),
            repeatDays = Task.parseRepeatDays(entity.repeatDays),
            deadlineDate = entity.deadlineDate,
            specificDate = entity.specificDate,
            lastStudiedAt = entity.lastStudiedAt,
            reviewCount = entity.reviewCount,
            reviewEnabled = entity.reviewEnabled
        )
    }

    override fun toEntity(domain: Task): TaskEntity {
        return TaskEntity(
            id = domain.id,
            groupId = domain.groupId,
            name = domain.name,
            progressNote = domain.progressNote,
            progressPercent = domain.progressPercent,
            nextGoal = domain.nextGoal,
            workDurationMinutes = domain.workDurationMinutes,
            isActive = domain.isActive,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
            scheduleType = if (domain.scheduleType == ScheduleType.NONE) null else domain.scheduleType.name.lowercase(),
            repeatDays = Task.formatRepeatDays(domain.repeatDays),
            deadlineDate = domain.deadlineDate,
            specificDate = domain.specificDate,
            lastStudiedAt = domain.lastStudiedAt,
            reviewCount = domain.reviewCount,
            reviewEnabled = domain.reviewEnabled
        )
    }
}
