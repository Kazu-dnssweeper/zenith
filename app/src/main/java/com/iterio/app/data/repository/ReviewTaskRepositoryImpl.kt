package com.iterio.app.data.repository

import com.iterio.app.data.local.dao.ReviewTaskDao
import com.iterio.app.data.mapper.ReviewTaskMapper
import com.iterio.app.domain.model.ReviewTask
import com.iterio.app.domain.repository.ReviewTaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewTaskRepositoryImpl @Inject constructor(
    private val reviewTaskDao: ReviewTaskDao,
    private val mapper: ReviewTaskMapper
) : ReviewTaskRepository {

    override suspend fun insert(task: ReviewTask): Long {
        return reviewTaskDao.insert(mapper.toEntity(task))
    }

    override suspend fun insertAll(tasks: List<ReviewTask>) {
        reviewTaskDao.insertAll(mapper.toEntityList(tasks))
    }

    override suspend fun update(task: ReviewTask) {
        reviewTaskDao.update(mapper.toEntity(task))
    }

    override suspend fun delete(task: ReviewTask) {
        reviewTaskDao.delete(mapper.toEntity(task))
    }

    override suspend fun getById(id: Long): ReviewTask? {
        return reviewTaskDao.getById(id)?.let { mapper.toDomain(it) }
    }

    override fun getTasksForSession(studySessionId: Long): Flow<List<ReviewTask>> {
        return reviewTaskDao.getTasksForSession(studySessionId).map { entities ->
            mapper.toDomainList(entities)
        }
    }

    override fun getTasksForTask(taskId: Long): Flow<List<ReviewTask>> {
        return reviewTaskDao.getTasksForTask(taskId).map { entities ->
            mapper.toDomainList(entities)
        }
    }

    override fun getPendingTasksForDate(date: LocalDate): Flow<List<ReviewTask>> {
        return reviewTaskDao.getPendingTasksForDateWithDetails(date).map { entities ->
            mapper.toDomainListFromDetails(entities)
        }
    }

    override fun getAllTasksForDate(date: LocalDate): Flow<List<ReviewTask>> {
        return reviewTaskDao.getAllTasksForDateWithDetails(date).map { entities ->
            mapper.toDomainListFromDetails(entities)
        }
    }

    override fun getOverdueAndTodayTasks(date: LocalDate): Flow<List<ReviewTask>> {
        return reviewTaskDao.getOverdueAndTodayTasksWithDetails(date).map { entities ->
            mapper.toDomainListFromDetails(entities)
        }
    }

    override suspend fun getPendingTaskCountForDate(date: LocalDate): Int {
        return reviewTaskDao.getPendingTaskCountForDate(date)
    }

    override suspend fun markAsCompleted(taskId: Long) {
        reviewTaskDao.markAsCompleted(taskId, LocalDateTime.now())
    }

    override suspend fun markAsIncomplete(taskId: Long) {
        reviewTaskDao.markAsIncomplete(taskId)
    }

    override suspend fun reschedule(taskId: Long, newDate: LocalDate) {
        reviewTaskDao.reschedule(taskId, newDate)
    }

    override suspend fun deleteTasksForSession(studySessionId: Long) {
        reviewTaskDao.deleteTasksForSession(studySessionId)
    }

    override suspend fun deleteTasksForTask(taskId: Long) {
        reviewTaskDao.deleteTasksForTask(taskId)
    }

    override suspend fun getTaskCountByDateRange(startDate: LocalDate, endDate: LocalDate): Map<LocalDate, Int> {
        return reviewTaskDao.getTaskCountByDateRange(startDate, endDate)
            .associate { it.date to it.count }
    }

    override fun getAllWithDetails(): Flow<List<ReviewTask>> {
        return reviewTaskDao.getAllWithDetails().map { entities ->
            mapper.toDomainListFromDetails(entities)
        }
    }

    override suspend fun getTotalCount(): Int {
        return reviewTaskDao.getTotalCount()
    }

    override suspend fun getIncompleteCount(): Int {
        return reviewTaskDao.getIncompleteCount()
    }

    override suspend fun deleteAll() {
        reviewTaskDao.deleteAll()
    }
}
