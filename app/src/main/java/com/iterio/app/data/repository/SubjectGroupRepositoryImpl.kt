package com.iterio.app.data.repository

import com.iterio.app.data.local.dao.SubjectGroupDao
import com.iterio.app.data.mapper.SubjectGroupMapper
import com.iterio.app.domain.model.SubjectGroup
import com.iterio.app.domain.repository.SubjectGroupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubjectGroupRepositoryImpl @Inject constructor(
    private val subjectGroupDao: SubjectGroupDao,
    private val mapper: SubjectGroupMapper
) : SubjectGroupRepository {

    override fun getAllGroups(): Flow<List<SubjectGroup>> {
        return subjectGroupDao.getAllGroups().map { entities ->
            mapper.toDomainList(entities)
        }
    }

    override suspend fun getGroupById(id: Long): SubjectGroup? {
        return subjectGroupDao.getGroupById(id)?.let { mapper.toDomain(it) }
    }

    override suspend fun insertGroup(group: SubjectGroup): Long {
        val displayOrder = subjectGroupDao.getNextDisplayOrder()
        return subjectGroupDao.insertGroup(mapper.toEntity(group).copy(displayOrder = displayOrder))
    }

    override suspend fun updateGroup(group: SubjectGroup) {
        subjectGroupDao.updateGroup(mapper.toEntity(group))
    }

    override suspend fun deleteGroup(group: SubjectGroup) {
        subjectGroupDao.deleteGroup(mapper.toEntity(group))
    }

    override suspend fun deleteGroupById(id: Long) {
        subjectGroupDao.deleteGroupById(id)
    }
}
