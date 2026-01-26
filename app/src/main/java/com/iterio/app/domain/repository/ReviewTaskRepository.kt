package com.iterio.app.domain.repository

import com.iterio.app.domain.model.ReviewTask
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface ReviewTaskRepository {
    suspend fun insert(task: ReviewTask): Long
    suspend fun insertAll(tasks: List<ReviewTask>)
    suspend fun update(task: ReviewTask)
    suspend fun delete(task: ReviewTask)
    suspend fun getById(id: Long): ReviewTask?
    fun getTasksForSession(studySessionId: Long): Flow<List<ReviewTask>>
    fun getTasksForTask(taskId: Long): Flow<List<ReviewTask>>
    fun getPendingTasksForDate(date: LocalDate): Flow<List<ReviewTask>>
    fun getAllTasksForDate(date: LocalDate): Flow<List<ReviewTask>>
    fun getOverdueAndTodayTasks(date: LocalDate): Flow<List<ReviewTask>>
    suspend fun getPendingTaskCountForDate(date: LocalDate): Int
    suspend fun markAsCompleted(taskId: Long)
    suspend fun markAsIncomplete(taskId: Long)
    suspend fun reschedule(taskId: Long, newDate: LocalDate)
    suspend fun deleteTasksForSession(studySessionId: Long)
    suspend fun deleteTasksForTask(taskId: Long)

    /**
     * 日付範囲の復習タスク数を取得（カレンダー表示用）
     */
    suspend fun getTaskCountByDateRange(startDate: LocalDate, endDate: LocalDate): Map<LocalDate, Int>

    /**
     * 全ての復習タスクを詳細情報付きで取得
     */
    fun getAllWithDetails(): Flow<List<ReviewTask>>

    /**
     * 復習タスクの総数を取得
     */
    suspend fun getTotalCount(): Int

    /**
     * 未完了の復習タスク数を取得
     */
    suspend fun getIncompleteCount(): Int

    /**
     * 全ての復習タスクを削除
     */
    suspend fun deleteAll()
}
