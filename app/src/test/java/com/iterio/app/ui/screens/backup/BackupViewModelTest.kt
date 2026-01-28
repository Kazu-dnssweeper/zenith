package com.iterio.app.ui.screens.backup

import android.content.Intent
import android.net.Uri
import app.cash.turbine.test
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.iterio.app.data.cloud.CloudBackupInfo
import com.iterio.app.data.cloud.GoogleAuthManager
import com.iterio.app.data.cloud.GoogleSignInState
import com.iterio.app.domain.model.ImportResult
import com.iterio.app.domain.model.SubscriptionStatus
import com.iterio.app.domain.usecase.BackupUseCase
import com.iterio.app.domain.usecase.CloudBackupUseCase
import com.iterio.app.testutil.CoroutineTestRule
import com.iterio.app.ui.premium.PremiumManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * BackupViewModel のユニットテスト
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BackupViewModelTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var backupUseCase: BackupUseCase
    private lateinit var cloudBackupUseCase: CloudBackupUseCase
    private lateinit var googleAuthManager: GoogleAuthManager
    private lateinit var premiumManager: PremiumManager

    private val signInStateFlow = MutableStateFlow<GoogleSignInState>(GoogleSignInState.SignedOut)
    private val currentAccountFlow = MutableStateFlow<GoogleSignInAccount?>(null)
    private val subscriptionStatusFlow = MutableStateFlow(SubscriptionStatus())

    @Before
    fun setup() {
        backupUseCase = mockk()
        cloudBackupUseCase = mockk()
        googleAuthManager = mockk()
        premiumManager = mockk()

        every { googleAuthManager.signInState } returns signInStateFlow
        every { googleAuthManager.currentAccount } returns currentAccountFlow
        every { premiumManager.subscriptionStatus } returns subscriptionStatusFlow
    }

    private fun createViewModel() = BackupViewModel(
        backupUseCase = backupUseCase,
        cloudBackupUseCase = cloudBackupUseCase,
        googleAuthManager = googleAuthManager,
        premiumManager = premiumManager
    )

    @Test
    fun `exportBackup success updates state to ExportSuccess`() = runTest {
        val uri: Uri = mockk()
        coEvery { backupUseCase.exportBackup(uri) } returns Result.success(Unit)

        val vm = createViewModel()
        advanceUntilIdle()

        vm.backupState.test {
            assertEquals(BackupState.Idle, awaitItem())

            vm.exportBackup(uri)
            advanceUntilIdle()

            assertEquals(BackupState.Exporting, awaitItem())
            val successState = awaitItem()
            assertTrue(successState is BackupState.ExportSuccess)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `exportBackup failure updates state to Error`() = runTest {
        val uri: Uri = mockk()
        coEvery { backupUseCase.exportBackup(uri) } returns Result.failure(RuntimeException("Export failed"))

        val vm = createViewModel()
        advanceUntilIdle()

        vm.backupState.test {
            assertEquals(BackupState.Idle, awaitItem())

            vm.exportBackup(uri)
            advanceUntilIdle()

            assertEquals(BackupState.Exporting, awaitItem())
            val errorState = awaitItem()
            assertTrue(errorState is BackupState.Error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `importBackup success updates state to ImportSuccess`() = runTest {
        val uri: Uri = mockk()
        val importResult = ImportResult(
            groupsImported = 5,
            tasksImported = 10,
            sessionsImported = 20,
            reviewTasksImported = 15,
            settingsImported = 5,
            statsImported = 10
        )
        coEvery { backupUseCase.importBackup(uri) } returns Result.success(importResult)

        val vm = createViewModel()
        advanceUntilIdle()

        vm.backupState.test {
            assertEquals(BackupState.Idle, awaitItem())

            vm.importBackup(uri)
            advanceUntilIdle()

            assertEquals(BackupState.Importing, awaitItem())
            val successState = awaitItem()
            assertTrue(successState is BackupState.ImportSuccess)
            assertEquals(importResult, (successState as BackupState.ImportSuccess).result)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `importBackup failure updates state to Error`() = runTest {
        val uri: Uri = mockk()
        coEvery { backupUseCase.importBackup(uri) } returns Result.failure(RuntimeException("Import failed"))

        val vm = createViewModel()
        advanceUntilIdle()

        vm.backupState.test {
            assertEquals(BackupState.Idle, awaitItem())

            vm.importBackup(uri)
            advanceUntilIdle()

            assertEquals(BackupState.Importing, awaitItem())
            val errorState = awaitItem()
            assertTrue(errorState is BackupState.Error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uploadToCloud success updates cloudBackupInfo`() = runTest {
        val cloudBackupInfo = CloudBackupInfo(
            fileId = "file123",
            fileName = "backup.json",
            modifiedTime = System.currentTimeMillis(),
            sizeBytes = 1024L
        )
        coEvery { cloudBackupUseCase.uploadToCloud() } returns Result.success(cloudBackupInfo)

        val vm = createViewModel()
        advanceUntilIdle()

        vm.cloudBackupState.test {
            assertEquals(CloudBackupState.Idle, awaitItem())

            vm.uploadToCloud()
            advanceUntilIdle()

            assertEquals(CloudBackupState.Uploading, awaitItem())
            val successState = awaitItem()
            assertTrue(successState is CloudBackupState.UploadSuccess)
            assertEquals(cloudBackupInfo, (successState as CloudBackupState.UploadSuccess).info)
            cancelAndIgnoreRemainingEvents()
        }

        vm.cloudBackupInfo.test {
            assertEquals(cloudBackupInfo, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uploadToCloud failure updates cloudBackupState to Error`() = runTest {
        coEvery { cloudBackupUseCase.uploadToCloud() } returns Result.failure(RuntimeException("Upload failed"))

        val vm = createViewModel()
        advanceUntilIdle()

        vm.cloudBackupState.test {
            assertEquals(CloudBackupState.Idle, awaitItem())

            vm.uploadToCloud()
            advanceUntilIdle()

            assertEquals(CloudBackupState.Uploading, awaitItem())
            val errorState = awaitItem()
            assertTrue(errorState is CloudBackupState.Error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `downloadFromCloud success updates cloudBackupState`() = runTest {
        val importResult = ImportResult(
            groupsImported = 3,
            tasksImported = 8,
            sessionsImported = 15,
            reviewTasksImported = 10,
            settingsImported = 3,
            statsImported = 5
        )
        coEvery { cloudBackupUseCase.downloadFromCloud() } returns Result.success(importResult)

        val vm = createViewModel()
        advanceUntilIdle()

        vm.cloudBackupState.test {
            assertEquals(CloudBackupState.Idle, awaitItem())

            vm.downloadFromCloud()
            advanceUntilIdle()

            assertEquals(CloudBackupState.Downloading, awaitItem())
            val successState = awaitItem()
            assertTrue(successState is CloudBackupState.DownloadSuccess)
            assertEquals(importResult, (successState as CloudBackupState.DownloadSuccess).result)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `downloadFromCloud failure updates cloudBackupState to Error`() = runTest {
        coEvery { cloudBackupUseCase.downloadFromCloud() } returns Result.failure(RuntimeException("Download failed"))

        val vm = createViewModel()
        advanceUntilIdle()

        vm.cloudBackupState.test {
            assertEquals(CloudBackupState.Idle, awaitItem())

            vm.downloadFromCloud()
            advanceUntilIdle()

            assertEquals(CloudBackupState.Downloading, awaitItem())
            val errorState = awaitItem()
            assertTrue(errorState is CloudBackupState.Error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `googleSignIn initializes Drive and fetches backup info`() = runTest {
        val mockAccount: GoogleSignInAccount = mockk()
        val cloudBackupInfo = CloudBackupInfo(
            fileId = "file456",
            fileName = "backup.json",
            modifiedTime = System.currentTimeMillis(),
            sizeBytes = 2048L
        )

        coEvery { cloudBackupUseCase.initializeDrive(mockAccount) } returns Unit
        coEvery { cloudBackupUseCase.getCloudBackupInfo() } returns Result.success(cloudBackupInfo)

        val vm = createViewModel()
        advanceUntilIdle()

        // Simulate sign-in
        currentAccountFlow.value = mockAccount
        advanceUntilIdle()

        coVerify { cloudBackupUseCase.initializeDrive(mockAccount) }
        coVerify { cloudBackupUseCase.getCloudBackupInfo() }

        vm.cloudBackupInfo.test {
            assertEquals(cloudBackupInfo, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `resetState sets backupState to Idle`() = runTest {
        val uri: Uri = mockk()
        coEvery { backupUseCase.exportBackup(uri) } returns Result.success(Unit)

        val vm = createViewModel()
        advanceUntilIdle()

        vm.exportBackup(uri)
        advanceUntilIdle()
        vm.resetState()

        vm.backupState.test {
            assertEquals(BackupState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `resetCloudState sets cloudBackupState to Idle`() = runTest {
        val cloudBackupInfo = CloudBackupInfo(
            fileId = "file789",
            fileName = "backup.json",
            modifiedTime = System.currentTimeMillis(),
            sizeBytes = 1024L
        )
        coEvery { cloudBackupUseCase.uploadToCloud() } returns Result.success(cloudBackupInfo)

        val vm = createViewModel()
        advanceUntilIdle()

        vm.uploadToCloud()
        advanceUntilIdle()
        vm.resetCloudState()

        vm.cloudBackupState.test {
            assertEquals(CloudBackupState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `signOutGoogle clears cloudBackupInfo and calls cleanup`() = runTest {
        coEvery { googleAuthManager.signOut() } returns Unit
        coEvery { cloudBackupUseCase.cleanup() } returns Unit

        val vm = createViewModel()
        advanceUntilIdle()

        vm.signOutGoogle()
        advanceUntilIdle()

        coVerify { googleAuthManager.signOut() }
        coVerify { cloudBackupUseCase.cleanup() }

        vm.cloudBackupInfo.test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `startTrial calls premiumManager startTrial`() = runTest {
        coEvery { premiumManager.startTrial() } returns Unit

        val vm = createViewModel()
        advanceUntilIdle()

        vm.startTrial()
        advanceUntilIdle()

        coVerify { premiumManager.startTrial() }
    }

    @Test
    fun `onEvent ExportBackup calls exportBackup`() = runTest {
        val uri: Uri = mockk()
        coEvery { backupUseCase.exportBackup(uri) } returns Result.success(Unit)

        val vm = createViewModel()
        advanceUntilIdle()

        vm.onEvent(BackupEvent.ExportBackup(uri))
        advanceUntilIdle()

        coVerify { backupUseCase.exportBackup(uri) }
    }

    @Test
    fun `onEvent ImportBackup calls importBackup`() = runTest {
        val uri: Uri = mockk()
        val importResult = ImportResult(0, 0, 0, 0, 0, 0)
        coEvery { backupUseCase.importBackup(uri) } returns Result.success(importResult)

        val vm = createViewModel()
        advanceUntilIdle()

        vm.onEvent(BackupEvent.ImportBackup(uri))
        advanceUntilIdle()

        coVerify { backupUseCase.importBackup(uri) }
    }

    // ========== onEvent 全ブランチテスト ==========

    @Test
    fun `onEvent ResetState resets backup state to Idle`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onEvent(BackupEvent.ResetState)

        vm.backupState.test {
            assertEquals(BackupState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onEvent HandleGoogleSignInResult delegates to googleAuthManager`() = runTest {
        val mockIntent: Intent = mockk()
        every { googleAuthManager.handleSignInResult(any()) } returns Unit

        val vm = createViewModel()
        advanceUntilIdle()

        vm.onEvent(BackupEvent.HandleGoogleSignInResult(mockIntent))

        io.mockk.verify { googleAuthManager.handleSignInResult(mockIntent) }
    }

    @Test
    fun `onEvent SignOutGoogle calls signOutGoogle`() = runTest {
        coEvery { googleAuthManager.signOut() } returns Unit
        coEvery { cloudBackupUseCase.cleanup() } returns Unit

        val vm = createViewModel()
        advanceUntilIdle()

        vm.onEvent(BackupEvent.SignOutGoogle)
        advanceUntilIdle()

        coVerify { googleAuthManager.signOut() }
        coVerify { cloudBackupUseCase.cleanup() }
    }

    @Test
    fun `onEvent UploadToCloud calls uploadToCloud`() = runTest {
        val info = CloudBackupInfo("id", "name", 0L, 0L)
        coEvery { cloudBackupUseCase.uploadToCloud() } returns Result.success(info)

        val vm = createViewModel()
        advanceUntilIdle()

        vm.onEvent(BackupEvent.UploadToCloud)
        advanceUntilIdle()

        coVerify { cloudBackupUseCase.uploadToCloud() }
    }

    @Test
    fun `onEvent DownloadFromCloud calls downloadFromCloud`() = runTest {
        val result = ImportResult(0, 0, 0, 0, 0, 0)
        coEvery { cloudBackupUseCase.downloadFromCloud() } returns Result.success(result)

        val vm = createViewModel()
        advanceUntilIdle()

        vm.onEvent(BackupEvent.DownloadFromCloud)
        advanceUntilIdle()

        coVerify { cloudBackupUseCase.downloadFromCloud() }
    }

    @Test
    fun `onEvent ResetCloudState resets cloud state to Idle`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onEvent(BackupEvent.ResetCloudState)

        vm.cloudBackupState.test {
            assertEquals(CloudBackupState.Idle, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onEvent StartTrial calls premiumManager startTrial`() = runTest {
        coEvery { premiumManager.startTrial() } returns Unit

        val vm = createViewModel()
        advanceUntilIdle()

        vm.onEvent(BackupEvent.StartTrial)
        advanceUntilIdle()

        coVerify { premiumManager.startTrial() }
    }

    // ========== プロパティ・メソッドアクセステスト ==========

    @Test
    fun `getGoogleSignInIntent delegates to googleAuthManager`() = runTest {
        val mockIntent: Intent = mockk()
        every { googleAuthManager.getSignInIntent() } returns mockIntent

        val vm = createViewModel()

        val intent = vm.getGoogleSignInIntent()

        assertEquals(mockIntent, intent)
    }

    @Test
    fun `handleGoogleSignInResult delegates to googleAuthManager`() = runTest {
        val mockIntent: Intent = mockk()
        every { googleAuthManager.handleSignInResult(any()) } returns Unit

        val vm = createViewModel()

        vm.handleGoogleSignInResult(mockIntent)

        io.mockk.verify { googleAuthManager.handleSignInResult(mockIntent) }
    }

    @Test
    fun `googleSignInState property exposes GoogleAuthManager state`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        assertNotNull(vm.googleSignInState)
        assertEquals(GoogleSignInState.SignedOut, vm.googleSignInState.value)
    }

    @Test
    fun `subscriptionStatus property exposes PremiumManager state`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        assertNotNull(vm.subscriptionStatus)
    }

    @Test
    fun `isPremium property starts false by default`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()

        assertNotNull(vm.isPremium)
        assertFalse(vm.isPremium.value)
    }
}
