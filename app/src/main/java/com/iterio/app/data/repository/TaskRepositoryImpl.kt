package com.iterio.app.data.repository

import com.iterio.app.data.local.dao.TaskDao
import com.iterio.app.data.mapper.TaskMapper
import com.iterio.app.domain.common.DomainError
import com.iterio.app.domain.common.Result
import com.iterio.app.domain.model.Task
import com.iterio.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val mapper: TaskMapper
) : TaskRepository {

    override fun getTasksByGroup(groupId: Long): Flow<List<Task>> {
        return taskDao.getTasksByGroup(groupId).map { entities ->
            mapper.toDomainList(entities)
        }
    }

    override fun getAllActiveTasks(): Flow<List<Task>> {
        return taskDao.getAllActiveTasks().map { entities ->
            mapper.toDomainList(entities)
        }
    }

    override suspend fun getTaskById(id: Long): Result<Task?, DomainError> =
        Result.catchingSuspend {
            taskDao.getTaskById(id)?.let { mapper.toDomain(it) }
        }

    override suspend fun insertTask(task: Task): Result<Long, DomainError> =
        Result.catchingSuspend {
            taskDao.insertTask(mapper.toEntity(task))
        }

    override suspend fun updateTask(task: Task): Result<Unit, DomainError> =
        Result.catchingSuspend {
            taskDao.updateTask(mapper.toEntity(task).copy(updatedAt = LocalDateTime.now()))
        }

    override suspend fun deleteTask(task: Task): Result<Unit, DomainError> =
        Result.catchingSuspend {
            taskDao.deleteTask(mapper.toEntity(task))
        }

    override suspend fun deactivateTask(id: Long): Result<Unit, DomainError> =
        Result.catchingSuspend {
            taskDao.deactivateTask(id)
        }

    override suspend fun updateProgress(id: Long, note: String?, percent: Int?, goal: String?): Result<Unit, DomainError> =
        Result.catchingSuspend {
            taskDao.updateProgress(id, note, percent, goal, LocalDateTime.now())
        }

    override fun getTodayScheduledTasks(today: LocalDate): Flow<List<Task>> {
        val dayOfWeek = today.dayOfWeek.value.toString()  // 1=月曜, 7=日曜
        val todayStr = today.toString()  // ISO format (yyyy-MM-dd)

        return taskDao.getTodayScheduledTasks(todayStr, dayOfWeek).map { entities ->
            mapper.toDomainList(entities)
        }
    }

    override suspend fun updateLastStudiedAt(taskId: Long, studiedAt: LocalDateTime): Result<Unit, DomainError> =
        Result.catchingSuspend {
            taskDao.updateLastStudiedAt(taskId, studiedAt)
        }

    override fun getUpcomingDeadlineTasks(startDate: LocalDate, endDate: LocalDate): Flow<List<Task>> {
        val startDateStr = startDate.toString()
        val endDateStr = endDate.toString()

        return taskDao.getUpcomingDeadlineTasks(startDateStr, endDateStr).map { entities ->
            mapper.toDomainList(entities)
        }
    }

    override suspend fun getTasksForDate(date: LocalDate): Result<List<Task>, DomainError> =
        Result.catchingSuspend {
            val dateStr = date.toString()
            val dayOfWeek = date.dayOfWeek.value.toString()
            mapper.toDomainList(taskDao.getTasksForDate(dateStr, dayOfWeek))
        }

    override suspend fun getTaskCountByDateRange(startDate: LocalDate, endDate: LocalDate): Result<Map<LocalDate, Int>, DomainError> =
        Result.catchingSuspend {
            val startDateStr = startDate.toString()
            val endDateStr = endDate.toString()
            val result = mutableMapOf<LocalDate, Int>()

            // 1. deadline/specificタスクをカウント（既存ロジック）
            val counts = taskDao.getTaskCountByDateRange(startDateStr, endDateStr)
            for (item in counts) {
                item.date?.let { dateStr ->
                    try {
                        val date = LocalDate.parse(dateStr)
                        result[date] = (result[date] ?: 0) + item.count
                    } catch (e: Exception) {
                        // Skip invalid dates
                    }
                }
            }

            // 2. 繰り返しタスクをカウント（新規ロジック）
            val repeatTasks = taskDao.getRepeatTasks()
            var currentDate = startDate
            while (!currentDate.isAfter(endDate)) {
                val dayOfWeek = currentDate.dayOfWeek.value  // 1=月曜〜7=日曜
                val repeatCount = repeatTasks.count { task ->
                    Task.parseRepeatDays(task.repeatDays).contains(dayOfWeek)
                }
                if (repeatCount > 0) {
                    result[currentDate] = (result[currentDate] ?: 0) + repeatCount
                }
                currentDate = currentDate.plusDays(1)
            }

            result.toMap()
        }
}
