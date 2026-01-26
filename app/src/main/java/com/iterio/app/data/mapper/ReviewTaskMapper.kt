package com.iterio.app.data.mapper

import com.iterio.app.data.local.entity.ReviewTaskEntity
import com.iterio.app.data.local.entity.ReviewTaskWithDetails
import com.iterio.app.domain.model.ReviewTask
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ReviewTaskEntity と ReviewTask 間の変換を行うマッパー
 */
@Singleton
class ReviewTaskMapper @Inject constructor() : Mapper<ReviewTaskEntity, ReviewTask> {

    override fun toDomain(entity: ReviewTaskEntity): ReviewTask {
        return ReviewTask(
            id = entity.id,
            studySessionId = entity.studySessionId,
            taskId = entity.taskId,
            scheduledDate = entity.scheduledDate,
            reviewNumber = entity.reviewNumber,
            isCompleted = entity.isCompleted,
            completedAt = entity.completedAt,
            createdAt = entity.createdAt
        )
    }

    override fun toEntity(domain: ReviewTask): ReviewTaskEntity {
        return ReviewTaskEntity(
            id = domain.id,
            studySessionId = domain.studySessionId,
            taskId = domain.taskId,
            scheduledDate = domain.scheduledDate,
            reviewNumber = domain.reviewNumber,
            isCompleted = domain.isCompleted,
            completedAt = domain.completedAt,
            createdAt = domain.createdAt
        )
    }

    /**
     * JOIN結果（ReviewTaskWithDetails）からドメインモデルに変換
     * taskName と groupName を含む
     */
    fun toDomainFromDetails(entity: ReviewTaskWithDetails): ReviewTask {
        return ReviewTask(
            id = entity.id,
            studySessionId = entity.studySessionId,
            taskId = entity.taskId,
            scheduledDate = entity.scheduledDate,
            reviewNumber = entity.reviewNumber,
            isCompleted = entity.isCompleted,
            completedAt = entity.completedAt,
            createdAt = entity.createdAt,
            taskName = entity.taskName,
            groupName = entity.groupName
        )
    }

    /**
     * JOIN結果リストからドメインモデルリストに変換
     */
    fun toDomainListFromDetails(entities: List<ReviewTaskWithDetails>): List<ReviewTask> {
        return entities.map { toDomainFromDetails(it) }
    }
}
