package com.iterio.app.data.local.entity

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * ReviewTask と関連する Task, SubjectGroup の情報を結合した結果を格納するデータクラス
 * Room の JOIN クエリ結果をマッピングするために使用
 */
data class ReviewTaskWithDetails(
    val id: Long,
    val studySessionId: Long,
    val taskId: Long,
    val scheduledDate: LocalDate,
    val reviewNumber: Int,
    val isCompleted: Boolean,
    val completedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val taskName: String?,
    val groupName: String?
)
