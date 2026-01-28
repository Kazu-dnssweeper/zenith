package com.iterio.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = SubjectGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["groupId"]),
        Index(value = ["isActive", "scheduleType"]),
        Index(value = ["deadlineDate"]),
        Index(value = ["specificDate"])
    ]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val groupId: Long,
    val name: String,
    val progressNote: String? = null,
    val progressPercent: Int? = null,
    val nextGoal: String? = null,
    val workDurationMinutes: Int? = null,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    // スケジュール関連
    val scheduleType: String? = null,  // "none", "repeat", "deadline", "specific"
    val repeatDays: String? = null,     // "1,3,5" (月=1, 日=7)
    val deadlineDate: LocalDate? = null,
    val specificDate: LocalDate? = null,
    val lastStudiedAt: LocalDateTime? = null,
    // 復習回数（null = プランのデフォルト使用）
    val reviewCount: Int? = null,
    // 復習タスク生成の有効/無効（デフォルトはtrue）
    val reviewEnabled: Boolean = true
)
