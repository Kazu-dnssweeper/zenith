package com.iterio.app.domain.repository

import com.iterio.app.domain.common.DomainError
import com.iterio.app.domain.common.Result
import com.iterio.app.domain.model.ReviewTask
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface ReviewTaskRepository {
    suspend fun insert(task: ReviewTask): Result<Long, DomainError>
    suspend fun insertAll(tasks: List<ReviewTask>): Result<Unit, DomainError>
    suspend fun update(task: ReviewTask): Result<Unit, DomainError>
    suspend fun delete(task: ReviewTask): Result<Unit, DomainError>
    suspend fun getById(id: Long): Result<ReviewTask?, DomainError>
    fun getTasksForSession(studySessionId: Long): Flow<List<ReviewTask>>
    fun getTasksForTask(taskId: Long): Flow<List<ReviewTask>>
    fun getPendingTasksForDate(date: LocalDate): Flow<List<ReviewTask>>
    fun getAllTasksForDate(date: LocalDate): Flow<List<ReviewTask>>
    fun getOverdueAndTodayTasks(date: LocalDate): Flow<List<ReviewTask>>
    suspend fun getPendingTaskCountForDate(date: LocalDate): Result<Int, DomainError>
    suspend fun markAsCompleted(taskId: Long): Result<Unit, DomainError>
    suspend fun markAsIncomplete(taskId: Long): Result<Unit, DomainError>
    suspend fun reschedule(taskId: Long, newDate: LocalDate): Result<Unit, DomainError>
    suspend fun deleteTasksForSession(studySessionId: Long): Result<Unit, DomainError>
    suspend fun deleteTasksForTask(taskId: Long): Result<Unit, DomainError>

    /**
     * 日付範囲の復習タスク数を取得（カレンダー表示用）
     */
    suspend fun getTaskCountByDateRange(startDate: LocalDate, endDate: LocalDate): Result<Map<LocalDate, Int>, DomainError>

    /**
     * 全ての復習タスクを詳細情報付きで取得
     */
    fun getAllWithDetails(): Flow<List<ReviewTask>>

    /**
     * 復習タスクの総数を取得
     */
    suspend fun getTotalCount(): Result<Int, DomainError>

    /**
     * 未完了の復習タスク数を取得
     */
    suspend fun getIncompleteCount(): Result<Int, DomainError>

    /**
     * 全ての復習タスクを削除
     */
    suspend fun deleteAll(): Result<Unit, DomainError>
}
