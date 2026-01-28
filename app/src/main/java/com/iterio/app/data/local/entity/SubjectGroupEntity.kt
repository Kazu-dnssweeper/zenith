package com.iterio.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "subject_groups",
    indices = [Index(value = ["displayOrder"])]
)
data class SubjectGroupEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val colorHex: String = "#00838F",
    val displayOrder: Int = 0,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
