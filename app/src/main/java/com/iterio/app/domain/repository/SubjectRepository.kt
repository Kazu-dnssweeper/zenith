package com.iterio.app.domain.repository

import com.iterio.app.domain.common.DomainError
import com.iterio.app.domain.common.Result
import com.iterio.app.domain.model.Subject
import kotlinx.coroutines.flow.Flow

interface SubjectRepository {
    suspend fun insert(subject: Subject): Result<Long, DomainError>
    suspend fun update(subject: Subject): Result<Unit, DomainError>
    suspend fun delete(subject: Subject): Result<Unit, DomainError>
    suspend fun getById(id: Long): Result<Subject?, DomainError>
    fun getAllSubjects(): Flow<List<Subject>>
    fun getTemplateSubjects(): Flow<List<Subject>>
    fun searchSubjects(query: String): Flow<List<Subject>>
}
