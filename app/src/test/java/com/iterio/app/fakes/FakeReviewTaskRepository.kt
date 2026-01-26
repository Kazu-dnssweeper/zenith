package com.iterio.app.fakes

import com.iterio.app.domain.model.ReviewTask
import com.iterio.app.domain.repository.ReviewTaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * テスト用の ReviewTaskRepository 実装
 */
class FakeReviewTaskRepository : ReviewTaskRepository {

    private val tasks = MutableStateFlow<Map<Long, ReviewTask>>(emptyMap())
    private var nextId = 1L

    override suspend fun insert(task: ReviewTask): Long {
        val id = nextId++
        val taskWithId = task.copy(id = id)
        tasks.value = tasks.value + (id to taskWithId)
        return id
    }

    override suspend fun insertAll(tasks: List<ReviewTask>) {
        tasks.forEach { insert(it) }
    }

    override suspend fun update(task: ReviewTask) {
        tasks.value = tasks.value + (task.id to task)
    }

    override suspend fun delete(task: ReviewTask) {
        tasks.value = tasks.value - task.id
    }

    override suspend fun getById(id: Long): ReviewTask? = tasks.value[id]

    override fun getTasksForSession(studySessionId: Long): Flow<List<ReviewTask>> =
        tasks.map { map ->
            map.values.filter { it.studySessionId == studySessionId }
        }

    override fun getTasksForTask(taskId: Long): Flow<List<ReviewTask>> =
        tasks.map { map ->
            map.values.filter { it.taskId == taskId }
        }

    override fun getPendingTasksForDate(date: LocalDate): Flow<List<ReviewTask>> =
        tasks.map { map ->
            map.values.filter {
                it.scheduledDate == date && !it.isCompleted
            }
        }

    override fun getAllTasksForDate(date: LocalDate): Flow<List<ReviewTask>> =
        tasks.map { map ->
            map.values.filter { it.scheduledDate == date }
        }

    override fun getOverdueAndTodayTasks(date: LocalDate): Flow<List<ReviewTask>> =
        tasks.map { map ->
            map.values.filter {
                !it.isCompleted && it.scheduledDate <= date
            }
        }

    override suspend fun getPendingTaskCountForDate(date: LocalDate): Int =
        tasks.value.values.count {
            it.scheduledDate == date && !it.isCompleted
        }

    override suspend fun markAsCompleted(taskId: Long) {
        val task = tasks.value[taskId] ?: return
        val completed = task.copy(
            isCompleted = true,
            completedAt = LocalDateTime.now()
        )
        tasks.value = tasks.value + (taskId to completed)
    }

    override suspend fun markAsIncomplete(taskId: Long) {
        val task = tasks.value[taskId] ?: return
        val incomplete = task.copy(
            isCompleted = false,
            completedAt = null
        )
        tasks.value = tasks.value + (taskId to incomplete)
    }

    override suspend fun reschedule(taskId: Long, newDate: LocalDate) {
        val task = tasks.value[taskId] ?: return
        val rescheduled = task.copy(scheduledDate = newDate)
        tasks.value = tasks.value + (taskId to rescheduled)
    }

    override suspend fun deleteTasksForSession(studySessionId: Long) {
        tasks.value = tasks.value.filterValues { it.studySessionId != studySessionId }
    }

    override suspend fun deleteTasksForTask(taskId: Long) {
        tasks.value = tasks.value.filterValues { it.taskId != taskId }
    }

    override suspend fun getTaskCountByDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): Map<LocalDate, Int> {
        val result = mutableMapOf<LocalDate, Int>()
        tasks.value.values.forEach { task ->
            val date = task.scheduledDate
            if (date >= startDate && date <= endDate) {
                result[date] = (result[date] ?: 0) + 1
            }
        }
        return result
    }

    override fun getAllWithDetails(): Flow<List<ReviewTask>> =
        tasks.map { map -> map.values.toList() }

    override suspend fun getTotalCount(): Int = tasks.value.size

    override suspend fun getIncompleteCount(): Int =
        tasks.value.values.count { !it.isCompleted }

    override suspend fun deleteAll() {
        tasks.value = emptyMap()
    }

    // Test helpers
    fun clear() {
        tasks.value = emptyMap()
        nextId = 1L
    }
}
