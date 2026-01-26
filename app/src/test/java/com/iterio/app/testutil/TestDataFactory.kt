package com.iterio.app.testutil

import com.iterio.app.domain.model.AllowedApp
import com.iterio.app.domain.model.PomodoroSettings
import com.iterio.app.domain.model.ScheduleType
import com.iterio.app.domain.model.SubjectGroup
import com.iterio.app.domain.model.Task
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * テストデータ生成用ファクトリ
 *
 * 使用例:
 * ```
 * val task = TestDataFactory.createTask(name = "Test Task")
 * val group = TestDataFactory.createSubjectGroup(name = "Math")
 * ```
 */
object TestDataFactory {

    // ==================== SubjectGroup ====================

    fun createSubjectGroup(
        id: Long = 0L,
        name: String = "Test Group",
        colorHex: String = "#00838F",
        displayOrder: Int = 0,
        createdAt: LocalDateTime = LocalDateTime.now()
    ) = SubjectGroup(
        id = id,
        name = name,
        colorHex = colorHex,
        displayOrder = displayOrder,
        createdAt = createdAt
    )

    fun createSubjectGroups(count: Int): List<SubjectGroup> {
        return (1..count).map { index ->
            createSubjectGroup(
                id = index.toLong(),
                name = "Group $index",
                displayOrder = index - 1
            )
        }
    }

    // ==================== Task ====================

    fun createTask(
        id: Long = 0L,
        groupId: Long = 1L,
        groupName: String? = "Test Group",
        groupColor: String? = "#00838F",
        name: String = "Test Task",
        progressNote: String? = null,
        progressPercent: Int? = null,
        nextGoal: String? = null,
        workDurationMinutes: Int? = null,
        isActive: Boolean = true,
        createdAt: LocalDateTime = LocalDateTime.now(),
        updatedAt: LocalDateTime = LocalDateTime.now(),
        scheduleType: ScheduleType = ScheduleType.NONE,
        repeatDays: Set<Int> = emptySet(),
        deadlineDate: LocalDate? = null,
        specificDate: LocalDate? = null,
        lastStudiedAt: LocalDateTime? = null
    ) = Task(
        id = id,
        groupId = groupId,
        groupName = groupName,
        groupColor = groupColor,
        name = name,
        progressNote = progressNote,
        progressPercent = progressPercent,
        nextGoal = nextGoal,
        workDurationMinutes = workDurationMinutes,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt,
        scheduleType = scheduleType,
        repeatDays = repeatDays,
        deadlineDate = deadlineDate,
        specificDate = specificDate,
        lastStudiedAt = lastStudiedAt
    )

    fun createTasks(count: Int, groupId: Long = 1L): List<Task> {
        return (1..count).map { index ->
            createTask(
                id = index.toLong(),
                groupId = groupId,
                name = "Task $index"
            )
        }
    }

    fun createRepeatTask(
        id: Long = 0L,
        groupId: Long = 1L,
        name: String = "Repeat Task",
        repeatDays: Set<Int> = setOf(1, 3, 5) // Mon, Wed, Fri
    ) = createTask(
        id = id,
        groupId = groupId,
        name = name,
        scheduleType = ScheduleType.REPEAT,
        repeatDays = repeatDays
    )

    fun createDeadlineTask(
        id: Long = 0L,
        groupId: Long = 1L,
        name: String = "Deadline Task",
        deadlineDate: LocalDate = LocalDate.now().plusDays(7)
    ) = createTask(
        id = id,
        groupId = groupId,
        name = name,
        scheduleType = ScheduleType.DEADLINE,
        deadlineDate = deadlineDate
    )

    fun createSpecificDateTask(
        id: Long = 0L,
        groupId: Long = 1L,
        name: String = "Specific Date Task",
        specificDate: LocalDate = LocalDate.now()
    ) = createTask(
        id = id,
        groupId = groupId,
        name = name,
        scheduleType = ScheduleType.SPECIFIC,
        specificDate = specificDate
    )

    // ==================== PomodoroSettings ====================

    fun createPomodoroSettings(
        workDurationMinutes: Int = 25,
        shortBreakMinutes: Int = 5,
        longBreakMinutes: Int = 15,
        cyclesBeforeLongBreak: Int = 4,
        focusModeEnabled: Boolean = true,
        focusModeStrict: Boolean = false,
        autoLoopEnabled: Boolean = false,
        reviewEnabled: Boolean = true,
        reviewIntervals: List<Int> = listOf(1, 3, 7, 14, 30, 60),
        notificationsEnabled: Boolean = true
    ) = PomodoroSettings(
        workDurationMinutes = workDurationMinutes,
        shortBreakMinutes = shortBreakMinutes,
        longBreakMinutes = longBreakMinutes,
        cyclesBeforeLongBreak = cyclesBeforeLongBreak,
        focusModeEnabled = focusModeEnabled,
        focusModeStrict = focusModeStrict,
        autoLoopEnabled = autoLoopEnabled,
        reviewEnabled = reviewEnabled,
        reviewIntervals = reviewIntervals,
        notificationsEnabled = notificationsEnabled
    )

    // ==================== AllowedApp ====================

    fun createAllowedApp(
        packageName: String = "com.example.app",
        appName: String = "Test App",
        icon: android.graphics.drawable.Drawable? = null
    ) = AllowedApp(
        packageName = packageName,
        appName = appName,
        icon = icon
    )

    fun createAllowedApps(count: Int): List<AllowedApp> {
        return (1..count).map { index ->
            createAllowedApp(
                packageName = "com.example.app$index",
                appName = "App $index"
            )
        }
    }
}
