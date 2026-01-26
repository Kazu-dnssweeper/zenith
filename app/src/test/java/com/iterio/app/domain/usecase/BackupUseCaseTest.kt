package com.iterio.app.domain.usecase

import android.net.Uri
import com.iterio.app.domain.common.DomainError
import com.iterio.app.domain.common.Result
import com.iterio.app.domain.model.BackupData
import com.iterio.app.domain.model.ImportResult
import com.iterio.app.domain.model.PremiumFeature
import com.iterio.app.domain.repository.BackupRepository
import com.iterio.app.domain.repository.PremiumRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

/**
 * BackupUseCase のユニットテスト
 */
class BackupUseCaseTest {

    private lateinit var backupRepository: BackupRepository
    private lateinit var premiumRepository: PremiumRepository
    private lateinit var useCase: BackupUseCase
    private lateinit var mockUri: Uri

    @Before
    fun setup() {
        backupRepository = mockk()
        premiumRepository = mockk()
        mockUri = mockk()
        useCase = BackupUseCase(backupRepository, premiumRepository)
    }

    // ==================== canUseBackup ====================

    @Test
    fun `canUseBackup returns true when premium user`() = runTest {
        coEvery { premiumRepository.canAccessFeature(PremiumFeature.BACKUP) } returns Result.Success(true)

        val result = useCase.canUseBackup()

        assertTrue(result)
    }

    @Test
    fun `canUseBackup returns false when free user`() = runTest {
        coEvery { premiumRepository.canAccessFeature(PremiumFeature.BACKUP) } returns Result.Success(false)

        val result = useCase.canUseBackup()

        assertFalse(result)
    }

    // ==================== exportBackup ====================

    @Test
    fun `exportBackup succeeds for premium user`() = runTest {
        val backupData = createTestBackupData()
        coEvery { premiumRepository.canAccessFeature(PremiumFeature.BACKUP) } returns Result.Success(true)
        coEvery { backupRepository.exportBackup() } returns backupData
        coEvery { backupRepository.writeToFile(backupData, mockUri) } returns kotlin.Result.success(Unit)

        val result = useCase.exportBackup(mockUri)

        assertTrue(result.isSuccess)
        coVerify { backupRepository.exportBackup() }
        coVerify { backupRepository.writeToFile(backupData, mockUri) }
    }

    @Test
    fun `exportBackup fails for free user with PremiumRequiredException`() = runTest {
        coEvery { premiumRepository.canAccessFeature(PremiumFeature.BACKUP) } returns Result.Success(false)

        val result = useCase.exportBackup(mockUri)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is PremiumRequiredException)
    }

    @Test
    fun `exportBackup propagates repository exception`() = runTest {
        coEvery { premiumRepository.canAccessFeature(PremiumFeature.BACKUP) } returns Result.Success(true)
        coEvery { backupRepository.exportBackup() } throws RuntimeException("Export error")

        val result = useCase.exportBackup(mockUri)

        assertTrue(result.isFailure)
        assertEquals("Export error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `exportBackup propagates write failure`() = runTest {
        val backupData = createTestBackupData()
        coEvery { premiumRepository.canAccessFeature(PremiumFeature.BACKUP) } returns Result.Success(true)
        coEvery { backupRepository.exportBackup() } returns backupData
        coEvery { backupRepository.writeToFile(backupData, mockUri) } returns kotlin.Result.failure(RuntimeException("Write failed"))

        val result = useCase.exportBackup(mockUri)

        assertTrue(result.isFailure)
        assertEquals("Write failed", result.exceptionOrNull()?.message)
    }

    // ==================== importBackup ====================

    @Test
    fun `importBackup succeeds for premium user`() = runTest {
        val backupData = createTestBackupData()
        val importResult = ImportResult(
            groupsImported = 2,
            tasksImported = 5,
            sessionsImported = 3,
            reviewTasksImported = 1,
            settingsImported = 1,
            statsImported = 7
        )
        coEvery { premiumRepository.canAccessFeature(PremiumFeature.BACKUP) } returns Result.Success(true)
        coEvery { backupRepository.readFromFile(mockUri) } returns kotlin.Result.success(backupData)
        coEvery { backupRepository.importBackup(backupData) } returns importResult

        val result = useCase.importBackup(mockUri)

        assertTrue(result.isSuccess)
        assertEquals(5, result.getOrNull()?.tasksImported)
        assertEquals(2, result.getOrNull()?.groupsImported)
    }

    @Test
    fun `importBackup fails for free user with PremiumRequiredException`() = runTest {
        coEvery { premiumRepository.canAccessFeature(PremiumFeature.BACKUP) } returns Result.Success(false)

        val result = useCase.importBackup(mockUri)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is PremiumRequiredException)
    }

    @Test
    fun `importBackup propagates read failure`() = runTest {
        coEvery { premiumRepository.canAccessFeature(PremiumFeature.BACKUP) } returns Result.Success(true)
        coEvery { backupRepository.readFromFile(mockUri) } returns kotlin.Result.failure(RuntimeException("Read failed"))

        val result = useCase.importBackup(mockUri)

        assertTrue(result.isFailure)
        assertEquals("Read failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `importBackup propagates import exception`() = runTest {
        val backupData = createTestBackupData()
        coEvery { premiumRepository.canAccessFeature(PremiumFeature.BACKUP) } returns Result.Success(true)
        coEvery { backupRepository.readFromFile(mockUri) } returns kotlin.Result.success(backupData)
        coEvery { backupRepository.importBackup(backupData) } throws RuntimeException("Import error")

        val result = useCase.importBackup(mockUri)

        assertTrue(result.isFailure)
        assertEquals("Import error", result.exceptionOrNull()?.message)
    }

    // ==================== readBackupPreview ====================

    @Test
    fun `readBackupPreview succeeds for premium user`() = runTest {
        val backupData = createTestBackupData()
        coEvery { premiumRepository.canAccessFeature(PremiumFeature.BACKUP) } returns Result.Success(true)
        coEvery { backupRepository.readFromFile(mockUri) } returns kotlin.Result.success(backupData)

        val result = useCase.readBackupPreview(mockUri)

        assertTrue(result.isSuccess)
        assertEquals(backupData, result.getOrNull())
    }

    @Test
    fun `readBackupPreview fails for free user`() = runTest {
        coEvery { premiumRepository.canAccessFeature(PremiumFeature.BACKUP) } returns Result.Success(false)

        val result = useCase.readBackupPreview(mockUri)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is PremiumRequiredException)
    }

    // ==================== Helper ====================

    private fun createTestBackupData() = BackupData(
        version = 1,
        exportedAt = LocalDateTime.now().toString(),
        subjectGroups = emptyList(),
        tasks = emptyList(),
        studySessions = emptyList(),
        reviewTasks = emptyList(),
        settings = emptyMap(),
        dailyStats = emptyList()
    )
}
