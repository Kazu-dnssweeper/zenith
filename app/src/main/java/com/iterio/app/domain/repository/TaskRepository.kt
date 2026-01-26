package com.iterio.app.domain.repository

import com.iterio.app.domain.model.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

interface TaskRepository {
    fun getTasksByGroup(groupId: Long): Flow<List<Task>>
    fun getAllActiveTasks(): Flow<List<Task>>
    suspend fun getTaskById(id: Long): Task?
    suspend fun insertTask(task: Task): Long
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(task: Task)
    suspend fun deactivateTask(id: Long)
    suspend fun updateProgress(id: Long, note: String?, percent: Int?, goal: String?)

    /**
     * 今日のスケジュール対象タスクを取得
     */
    fun getTodayScheduledTasks(today: LocalDate): Flow<List<Task>>

    /**
     * 最終学習日時を更新
     */
    suspend fun updateLastStudiedAt(taskId: Long, studiedAt: LocalDateTime)

    /**
     * 期限が近いタスクを取得
     */
    fun getUpcomingDeadlineTasks(startDate: LocalDate, endDate: LocalDate): Flow<List<Task>>

    /**
     * 特定日のタスクを取得
     */
    suspend fun getTasksForDate(date: LocalDate): List<Task>

    /**
     * 日付範囲のタスク数を取得
     */
    suspend fun getTaskCountByDateRange(startDate: LocalDate, endDate: LocalDate): Map<LocalDate, Int>
}
