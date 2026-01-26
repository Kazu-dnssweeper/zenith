package com.iterio.app.data.local.dao

import androidx.room.*
import com.iterio.app.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE groupId = :groupId AND isActive = 1 ORDER BY createdAt DESC")
    fun getTasksByGroup(groupId: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isActive = 1 ORDER BY updatedAt DESC")
    fun getAllActiveTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("UPDATE tasks SET isActive = 0 WHERE id = :id")
    suspend fun deactivateTask(id: Long)

    @Query("UPDATE tasks SET progressNote = :note, progressPercent = :percent, nextGoal = :goal, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateProgress(
        id: Long,
        note: String?,
        percent: Int?,
        goal: String?,
        updatedAt: java.time.LocalDateTime
    )

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    suspend fun getAllTasks(): List<TaskEntity>

    /**
     * 今日のスケジュール対象タスクを取得
     * - 繰り返し: 今日の曜日が含まれる
     * - 期限: 期限日が今日
     * - 特定日: 今日と一致
     */
    @Query("""
        SELECT * FROM tasks
        WHERE isActive = 1 AND (
            (scheduleType = 'repeat' AND (
                repeatDays = :dayOfWeek
                OR repeatDays LIKE :dayOfWeek || ',%'
                OR repeatDays LIKE '%,' || :dayOfWeek || ',%'
                OR repeatDays LIKE '%,' || :dayOfWeek
            ))
            OR (scheduleType = 'deadline' AND deadlineDate = :today)
            OR (scheduleType = 'specific' AND specificDate = :today)
        )
        ORDER BY updatedAt DESC
    """)
    fun getTodayScheduledTasks(today: String, dayOfWeek: String): Flow<List<TaskEntity>>

    /**
     * 最終学習日時を更新
     */
    @Query("UPDATE tasks SET lastStudiedAt = :studiedAt, updatedAt = :studiedAt WHERE id = :taskId")
    suspend fun updateLastStudiedAt(taskId: Long, studiedAt: java.time.LocalDateTime)

    /**
     * 期限が近いタスクを取得（今日より後〜指定日まで）
     */
    @Query("""
        SELECT * FROM tasks
        WHERE isActive = 1
        AND scheduleType = 'deadline'
        AND deadlineDate > :today
        AND deadlineDate <= :endDate
        ORDER BY deadlineDate ASC
    """)
    fun getUpcomingDeadlineTasks(today: String, endDate: String): Flow<List<TaskEntity>>

    /**
     * 特定日のタスクを取得
     */
    @Query("""
        SELECT * FROM tasks
        WHERE isActive = 1 AND (
            (scheduleType = 'repeat' AND (
                repeatDays = :dayOfWeek
                OR repeatDays LIKE :dayOfWeek || ',%'
                OR repeatDays LIKE '%,' || :dayOfWeek || ',%'
                OR repeatDays LIKE '%,' || :dayOfWeek
            ))
            OR (scheduleType = 'deadline' AND deadlineDate = :date)
            OR (scheduleType = 'specific' AND specificDate = :date)
        )
        ORDER BY name ASC
    """)
    suspend fun getTasksForDate(date: String, dayOfWeek: String): List<TaskEntity>

    /**
     * 日付範囲のタスク数を集計
     */
    @Query("""
        SELECT deadlineDate as date, COUNT(*) as count FROM tasks
        WHERE isActive = 1 AND scheduleType = 'deadline'
        AND deadlineDate BETWEEN :startDate AND :endDate
        GROUP BY deadlineDate
        UNION ALL
        SELECT specificDate as date, COUNT(*) as count FROM tasks
        WHERE isActive = 1 AND scheduleType = 'specific'
        AND specificDate BETWEEN :startDate AND :endDate
        GROUP BY specificDate
    """)
    suspend fun getTaskCountByDateRange(startDate: String, endDate: String): List<DateTaskCount>

    /**
     * 繰り返しタスクを全て取得（カレンダー表示用）
     */
    @Query("SELECT * FROM tasks WHERE isActive = 1 AND scheduleType = 'repeat'")
    suspend fun getRepeatTasks(): List<TaskEntity>
}

data class DateTaskCount(
    val date: String?,
    val count: Int
)
