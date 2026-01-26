package com.iterio.app.data.repository

import com.iterio.app.data.local.dao.SubjectDao
import com.iterio.app.data.local.entity.SubjectEntity
import com.iterio.app.domain.common.DomainError
import com.iterio.app.domain.common.Result
import com.iterio.app.domain.model.Subject
import com.iterio.app.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubjectRepositoryImpl @Inject constructor(
    private val subjectDao: SubjectDao
) : SubjectRepository {

    override suspend fun insert(subject: Subject): Result<Long, DomainError> =
        Result.catchingSuspend {
            val maxOrder = subjectDao.getMaxDisplayOrder() ?: 0
            val entity = subject.toEntity().copy(displayOrder = maxOrder + 1)
            subjectDao.insert(entity)
        }

    override suspend fun update(subject: Subject): Result<Unit, DomainError> =
        Result.catchingSuspend {
            subjectDao.update(subject.toEntity())
        }

    override suspend fun delete(subject: Subject): Result<Unit, DomainError> =
        Result.catchingSuspend {
            subjectDao.delete(subject.toEntity())
        }

    override suspend fun getById(id: Long): Result<Subject?, DomainError> =
        Result.catchingSuspend {
            subjectDao.getById(id)?.toDomain()
        }

    override fun getAllSubjects(): Flow<List<Subject>> {
        return subjectDao.getAllSubjects().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTemplateSubjects(): Flow<List<Subject>> {
        return subjectDao.getTemplateSubjects().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun searchSubjects(query: String): Flow<List<Subject>> {
        return subjectDao.searchSubjects(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    private fun Subject.toEntity(): SubjectEntity {
        return SubjectEntity(
            id = id,
            name = name,
            colorHex = colorHex,
            isTemplate = isTemplate,
            displayOrder = displayOrder,
            createdAt = createdAt
        )
    }

    private fun SubjectEntity.toDomain(): Subject {
        return Subject(
            id = id,
            name = name,
            colorHex = colorHex,
            isTemplate = isTemplate,
            displayOrder = displayOrder,
            createdAt = createdAt
        )
    }
}
