package com.iterio.app.domain.model

import com.iterio.app.config.AppConfig
import java.time.LocalDateTime

data class StudySession(
    val id: Long = 0,
    val taskId: Long,
    val taskName: String? = null,
    val groupName: String? = null,
    val startedAt: LocalDateTime,
    val endedAt: LocalDateTime? = null,
    val workDurationMinutes: Int = 0,
    val plannedDurationMinutes: Int = AppConfig.Timer.DEFAULT_WORK_MINUTES,
    val cyclesCompleted: Int = 0,
    val wasInterrupted: Boolean = false,
    val notes: String? = null
)
