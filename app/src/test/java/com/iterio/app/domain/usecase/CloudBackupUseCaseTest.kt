package com.iterio.app.domain.usecase

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.iterio.app.data.cloud.CloudBackupInfo
import com.iterio.app.data.cloud.GoogleDriveManager
import com.iterio.app.domain.model.BackupData
import com.iterio.app.domain.model.ImportResult
import com.iterio.app.domain.model.PremiumFeature
import com.iterio.app.domain.repository.BackupRepository
import com.iterio.app.domain.repository.PremiumRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

/**
 * CloudBackupUseCase のユニットテスト
 */
class CloudBackupUseCaseTest {

    private lateinit var backupRepository: BackupRepository
    private lateinit var googleDriveManager: GoogleDriveManager
    private lateinit var premiumRepository: PremiumRepository
    private lateinit var useCase: CloudBackupUseCase

    @Before
    fun setup() {
        backupRepository = mockk()
        googleDriveManager = mockk()
        premiumRepository = mockk()
        useCase = CloudBackupUseCase(backupRepository, googleDriveManager, premiumRepository)
    }

    // ==================== canUseCloudBackup ====================

    @Test
    fun `canUseCloudBackup returns true for premium user`() = runTest {
        coEvery { premiumRepository.canAccessFeature(PremiumFeature.BACKUP) } returns true

        val result = useCase.canUseCloudBackup()

        assertTrue(result)
    }

    @Test
    fun `canUseCloudBackup returns false for free user`() = runTest {
        coEvery { premiumRepository.canAccessFeature(PremiumFeature.BACKUP) } returns false

        val result = useCase.canUseCloudBackup()

        assertFalse(result)
    }

    // ==================== initializeDrive ====================

    @Test
    fun `initializeDrive calls googleDriveManager`() {
        val mockAccount: GoogleSignInAccount = mockk()
        every { googleDriveManager.initialize(mockAccount) } just runs

        useCase.initializeDrive(mockAccount)

        verify { googleDriveManager.initialize(mockAccount) }
    }

    // ==================== isDriveInitialized ====================

    @Test
    fun `isDriveInitialized returns true when initialized`() {
        every { googleDriveManager.isInitialized() } returns true

        val result = useCase.isDriveInitialized()

        assertTrue(result)
    }

    @Test
    fun `isDriveInitialized returns false when not initialized`() {
        every { googleDriveManager.isInitialized() } returns false

        val result = useCase.isDriveInitialized()

        assertFalse(result)
    }

    // ==================== uploadToCloud ====================

    @Test
    fun `uploadToCloud succeeds for premium user with initialized drive`() = runTest {
        val backupData = createTestBackupData()
        val jsonContent = """{"version":1}"""
        val cloudInfo = CloudBackupInfo(
            fileId = "file123",
            fileName = "backup.json",
            modifiedTime = System.currentTimeMillis(),
            sizeBytes = 1024
        )
        coEvery { premiumRepository.canAccessFeature(PremiumFeature.BACKUP) } returns true
        every { googleDriveManager.isInitialized() } returns true
        coEvery { backupRepository.exportBackup() } returns backupData
        every { backupRepository.serializeToJson(backupData) } returns jsonContent
        coEvery { googleDriveManager.uploadBackup(jsonContent) } returns Result.success(cloudInfo)

        val result = useCase.uploadToCloud()

        assertTrue(result.isSuccess)
        assertEquals("file123", result.getOrNull()?.fileId)
    }

    @Test
    fun `uploadToCloud fails for free user`() = runTest {
        coEvery { premiumRepository.canAccessFeature(PremiumFeature.BACKUP) } returns false

        val result = useCase.uploadToCloud()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is PremiumRequiredException)
    }

    @Test
    fun `uploadToCloud fails when drive not initialized`() = runTest {
        coEvery { premiumRepository.canAccessFeature(PremiumFeature.BACKUP) } returns true
        every { googleDriveManager.isInitialized() } returns false

        val result = useCase.uploadToCloud()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun `uploadToCloud propagates export exception`() = runTest {
        coEvery { premiumRepository.canAccessFeature(PremiumFeature.BACKUP) } returns true
        every { googleDriveManager.isInitialized() } returns true
        coEvery { backupRepository.exportBackup() } throws RuntimeException("Export failed")

        val result = useCase.uploadToCloud()

        assertTrue(result.isFailure)
        assertEquals("Export failed", result.exceptionOrNull()?.message)
    }

    // ==================== downloadFromCloud ====================

    @Test
    fun `downloadFromCloud succeeds for premium user`() = runTest {
        val jsonContent = """{"version":1}"""
        val backupData = createTestBackupData()
        val importResult = ImportResult(
            groupsImported = 2,
            tasksImported = 5,
            sessionsImported = 3,
            reviewTasksImported = 1,
            settingsImported = 1,
            statsImported = 7
        )
        coEvery { premiumRepository.canAccessFeature(PremiumFeature.BACKUP) } returns true
        every { googleDriveManager.isInitialized() } returns true
        coEvery { googleDriveManager.downloadBackup() } returns Result.success(jsonContent)
        every { backupRepository.deserializeFromJson(jsonContent) } returns Result.success(backupData)
        coEvery { backupRepository.importBackup(backupData) } returns importResult

        val result = useCase.downloadFromCloud()

        assertTrue(result.isSuccess)
        assertEquals(5, result.getOrNull()?.tasksImported)
    }

    @Test
    fun `downloadFromCloud fails for free user`() = runTest {
        coEvery { premiumRepository.canAccessFeature(PremiumFeature.BACKUP) } returns false

        val result = useCase.downloadFromCloud()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is PremiumRequiredException)
    }

    @Test
    fun `downloadFromCloud fails when drive not initialized`() = runTest {
        coEvery { premiumRepository.canAccessFeature(PremiumFeature.BACKUP) } returns true
        every { googleDriveManager.isInitialized() } returns false

        val result = useCase.downloadFromCloud()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun `downloadFromCloud propagates download failure`() = runTest {
        coEvery { premiumRepository.canAccessFeature(PremiumFeature.BACKUP) } returns true
        every { googleDriveManager.isInitialized() } returns true
        coEvery { googleDriveManager.downloadBackup() } returns Result.failure(RuntimeException("Download failed"))

        val result = useCase.downloadFromCloud()

        assertTrue(result.isFailure)
        assertEquals("Download failed", result.exceptionOrNull()?.message)
    }

    // ==================== getCloudBackupInfo ====================

    @Test
    fun `getCloudBackupInfo succeeds when initialized`() = runTest {
        val cloudInfo = CloudBackupInfo(
            fileId = "file123",
            fileName = "backup.json",
            modifiedTime = System.currentTimeMillis(),
            sizeBytes = 2048
        )
        every { googleDriveManager.isInitialized() } returns true
        coEvery { googleDriveManager.getBackupInfo() } returns Result.success(cloudInfo)

        val result = useCase.getCloudBackupInfo()

        assertTrue(result.isSuccess)
        assertEquals("file123", result.getOrNull()?.fileId)
    }

    @Test
    fun `getCloudBackupInfo fails when not initialized`() = runTest {
        every { googleDriveManager.isInitialized() } returns false

        val result = useCase.getCloudBackupInfo()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }

    // ==================== deleteCloudBackup ====================

    @Test
    fun `deleteCloudBackup succeeds for premium user`() = runTest {
        coEvery { premiumRepository.canAccessFeature(PremiumFeature.BACKUP) } returns true
        every { googleDriveManager.isInitialized() } returns true
        coEvery { googleDriveManager.deleteBackup() } returns Result.success(Unit)

        val result = useCase.deleteCloudBackup()

        assertTrue(result.isSuccess)
    }

    @Test
    fun `deleteCloudBackup fails for free user`() = runTest {
        coEvery { premiumRepository.canAccessFeature(PremiumFeature.BACKUP) } returns false

        val result = useCase.deleteCloudBackup()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is PremiumRequiredException)
    }

    @Test
    fun `deleteCloudBackup fails when not initialized`() = runTest {
        coEvery { premiumRepository.canAccessFeature(PremiumFeature.BACKUP) } returns true
        every { googleDriveManager.isInitialized() } returns false

        val result = useCase.deleteCloudBackup()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }

    // ==================== cleanup ====================

    @Test
    fun `cleanup calls googleDriveManager cleanup`() {
        every { googleDriveManager.cleanup() } just runs

        useCase.cleanup()

        verify { googleDriveManager.cleanup() }
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
