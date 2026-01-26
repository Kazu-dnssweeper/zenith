package com.iterio.app.ui.screens.settings.allowedapps

import app.cash.turbine.test
import com.iterio.app.domain.common.Result
import com.iterio.app.domain.model.AllowedApp
import com.iterio.app.domain.repository.SettingsRepository
import com.iterio.app.util.InstalledAppsHelper
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * AllowedAppsViewModel のユニットテスト
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AllowedAppsViewModelTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var installedAppsHelper: InstalledAppsHelper
    private lateinit var viewModel: AllowedAppsViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val mockApps = listOf(
        AllowedApp(packageName = "com.example.app1", appName = "App 1", icon = null),
        AllowedApp(packageName = "com.example.app2", appName = "App 2", icon = null),
        AllowedApp(packageName = "com.example.app3", appName = "App 3", icon = null),
        AllowedApp(packageName = "com.test.chrome", appName = "Chrome Browser", icon = null)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        settingsRepository = mockk(relaxed = true)
        installedAppsHelper = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): AllowedAppsViewModel {
        return AllowedAppsViewModel(
            settingsRepository = settingsRepository,
            installedAppsHelper = installedAppsHelper
        )
    }

    // 初期状態のロードテスト

    @Test
    fun `initial state has isLoading true`() = runTest {
        // Arrange
        coEvery { settingsRepository.getAllowedApps() } returns Result.Success(emptyList<String>())
        coEvery { installedAppsHelper.getInstalledUserApps() } returns emptyList()
        every { installedAppsHelper.validatePackages(any()) } returns emptyList()

        // Act
        viewModel = createViewModel()

        // Assert - 初期状態はisLoading = true
        assertTrue("初期状態はisLoading=trueであるべき", viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadData loads installed apps and saved selection`() = runTest {
        // Arrange
        val savedApps = listOf("com.example.app1", "com.example.app2")
        coEvery { settingsRepository.getAllowedApps() } returns Result.Success(savedApps)
        coEvery { installedAppsHelper.getInstalledUserApps() } returns mockApps
        every { installedAppsHelper.validatePackages(savedApps) } returns savedApps

        // Act
        viewModel = createViewModel()
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertFalse("ロード完了後はisLoading=falseであるべき", state.isLoading)
        assertEquals("インストール済みアプリが読み込まれるべき", mockApps, state.installedApps)
        assertEquals("保存済み選択が復元されるべき", savedApps.toSet(), state.selectedPackages)
    }

    @Test
    fun `loadData filters deleted apps from saved selection`() = runTest {
        // Arrange
        val savedApps = listOf("com.example.app1", "com.deleted.app", "com.example.app2")
        val validApps = listOf("com.example.app1", "com.example.app2")
        coEvery { settingsRepository.getAllowedApps() } returns Result.Success(savedApps)
        coEvery { installedAppsHelper.getInstalledUserApps() } returns mockApps
        every { installedAppsHelper.validatePackages(savedApps) } returns validApps

        // Act
        viewModel = createViewModel()
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertEquals("削除済みアプリがフィルタリングされるべき", validApps.toSet(), state.selectedPackages)
        coVerify { settingsRepository.setAllowedApps(validApps) }
    }

    // toggleAppSelection のテスト

    @Test
    fun `toggleAppSelection adds app to selection`() = runTest {
        // Arrange
        coEvery { settingsRepository.getAllowedApps() } returns Result.Success(emptyList<String>())
        coEvery { installedAppsHelper.getInstalledUserApps() } returns mockApps
        every { installedAppsHelper.validatePackages(any()) } returns emptyList()

        viewModel = createViewModel()
        advanceUntilIdle()

        // Act
        viewModel.toggleAppSelection("com.example.app1")
        advanceUntilIdle()

        // Assert
        assertTrue(
            "アプリが選択に追加されるべき",
            viewModel.uiState.value.selectedPackages.contains("com.example.app1")
        )
        coVerify { settingsRepository.setAllowedApps(listOf("com.example.app1")) }
    }

    @Test
    fun `toggleAppSelection removes app from selection`() = runTest {
        // Arrange
        val savedApps = listOf("com.example.app1")
        coEvery { settingsRepository.getAllowedApps() } returns Result.Success(savedApps)
        coEvery { installedAppsHelper.getInstalledUserApps() } returns mockApps
        every { installedAppsHelper.validatePackages(savedApps) } returns savedApps

        viewModel = createViewModel()
        advanceUntilIdle()

        // Act
        viewModel.toggleAppSelection("com.example.app1")
        advanceUntilIdle()

        // Assert
        assertFalse(
            "アプリが選択から削除されるべき",
            viewModel.uiState.value.selectedPackages.contains("com.example.app1")
        )
        coVerify { settingsRepository.setAllowedApps(emptyList()) }
    }

    // updateSearchQuery のテスト（デバウンス動作）

    @Test
    fun `updateSearchQuery updates immediate query instantly`() = runTest {
        // Arrange
        coEvery { settingsRepository.getAllowedApps() } returns Result.Success(emptyList<String>())
        coEvery { installedAppsHelper.getInstalledUserApps() } returns mockApps
        every { installedAppsHelper.validatePackages(any()) } returns emptyList()

        viewModel = createViewModel()
        advanceUntilIdle()

        // Act
        viewModel.updateSearchQuery("Chrome")

        // Assert - immediateSearchQueryは即時更新される
        assertEquals("即時クエリが更新されるべき", "Chrome", viewModel.uiState.value.immediateSearchQuery)
    }

    @Test
    fun `updateSearchQuery debounces actual search query`() = runTest {
        // Arrange
        coEvery { settingsRepository.getAllowedApps() } returns Result.Success(emptyList<String>())
        coEvery { installedAppsHelper.getInstalledUserApps() } returns mockApps
        every { installedAppsHelper.validatePackages(any()) } returns emptyList()

        viewModel = createViewModel()
        advanceUntilIdle()

        // Act
        viewModel.updateSearchQuery("Chr")
        // デバウンス時間(300ms)より前
        advanceTimeBy(100)

        // Assert - まだ検索クエリは更新されていない
        assertEquals("検索クエリはまだ空であるべき", "", viewModel.uiState.value.searchQuery)

        // デバウンス時間を超過
        advanceTimeBy(250)

        // Assert - デバウンス後に更新される
        assertEquals("検索クエリが更新されるべき", "Chr", viewModel.uiState.value.searchQuery)
    }

    @Test
    fun `updateSearchQuery cancels previous debounce job`() = runTest {
        // Arrange
        coEvery { settingsRepository.getAllowedApps() } returns Result.Success(emptyList<String>())
        coEvery { installedAppsHelper.getInstalledUserApps() } returns mockApps
        every { installedAppsHelper.validatePackages(any()) } returns emptyList()

        viewModel = createViewModel()
        advanceUntilIdle()

        // Act
        viewModel.updateSearchQuery("Chr")
        advanceTimeBy(100)
        viewModel.updateSearchQuery("Chrome") // 新しい検索で前のジョブをキャンセル
        advanceTimeBy(350)

        // Assert - 最後の検索クエリのみが反映される
        assertEquals("最後のクエリのみが反映されるべき", "Chrome", viewModel.uiState.value.searchQuery)
    }

    // selectAll / deselectAll のテスト

    @Test
    fun `selectAll selects all filtered apps`() = runTest {
        // Arrange
        coEvery { settingsRepository.getAllowedApps() } returns Result.Success(emptyList<String>())
        coEvery { installedAppsHelper.getInstalledUserApps() } returns mockApps
        every { installedAppsHelper.validatePackages(any()) } returns emptyList()

        viewModel = createViewModel()
        advanceUntilIdle()

        // Act
        viewModel.selectAll()
        advanceUntilIdle()

        // Assert
        val expected = mockApps.map { it.packageName }.toSet()
        assertEquals("全アプリが選択されるべき", expected, viewModel.uiState.value.selectedPackages)
    }

    @Test
    fun `deselectAll deselects all filtered apps`() = runTest {
        // Arrange
        val savedApps = mockApps.map { it.packageName }
        coEvery { settingsRepository.getAllowedApps() } returns Result.Success(savedApps)
        coEvery { installedAppsHelper.getInstalledUserApps() } returns mockApps
        every { installedAppsHelper.validatePackages(savedApps) } returns savedApps

        viewModel = createViewModel()
        advanceUntilIdle()

        // Act
        viewModel.deselectAll()
        advanceUntilIdle()

        // Assert
        assertTrue("全アプリが選択解除されるべき", viewModel.uiState.value.selectedPackages.isEmpty())
    }

    @Test
    fun `selectAll only affects filtered apps when search is active`() = runTest {
        // Arrange
        coEvery { settingsRepository.getAllowedApps() } returns Result.Success(emptyList<String>())
        coEvery { installedAppsHelper.getInstalledUserApps() } returns mockApps
        every { installedAppsHelper.validatePackages(any()) } returns emptyList()

        viewModel = createViewModel()
        advanceUntilIdle()

        // 検索でフィルタリング
        viewModel.updateSearchQuery("Chrome")
        advanceUntilIdle()

        // Act
        viewModel.selectAll()
        advanceUntilIdle()

        // Assert - 検索にマッチするChromeのみが選択される
        assertEquals(
            "フィルタされたアプリのみが選択されるべき",
            setOf("com.test.chrome"),
            viewModel.uiState.value.selectedPackages
        )
    }

    // getFilteredApps のテスト

    @Test
    fun `getFilteredApps returns all apps when no search query`() = runTest {
        // Arrange
        coEvery { settingsRepository.getAllowedApps() } returns Result.Success(emptyList<String>())
        coEvery { installedAppsHelper.getInstalledUserApps() } returns mockApps
        every { installedAppsHelper.validatePackages(any()) } returns emptyList()

        viewModel = createViewModel()
        advanceUntilIdle()

        // Act
        val filtered = viewModel.getFilteredApps()

        // Assert
        assertEquals("全アプリが返されるべき", mockApps, filtered)
    }

    @Test
    fun `getFilteredApps filters by app name`() = runTest {
        // Arrange
        coEvery { settingsRepository.getAllowedApps() } returns Result.Success(emptyList<String>())
        coEvery { installedAppsHelper.getInstalledUserApps() } returns mockApps
        every { installedAppsHelper.validatePackages(any()) } returns emptyList()

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateSearchQuery("Chrome")
        advanceUntilIdle()

        // Act
        val filtered = viewModel.getFilteredApps()

        // Assert
        assertEquals("Chromeのみがマッチするべき", 1, filtered.size)
        assertEquals("com.test.chrome", filtered[0].packageName)
    }

    @Test
    fun `getFilteredApps filters by package name`() = runTest {
        // Arrange
        coEvery { settingsRepository.getAllowedApps() } returns Result.Success(emptyList<String>())
        coEvery { installedAppsHelper.getInstalledUserApps() } returns mockApps
        every { installedAppsHelper.validatePackages(any()) } returns emptyList()

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateSearchQuery("com.example")
        advanceUntilIdle()

        // Act
        val filtered = viewModel.getFilteredApps()

        // Assert
        assertEquals("3つのexampleアプリがマッチするべき", 3, filtered.size)
    }

    @Test
    fun `getFilteredApps is case insensitive`() = runTest {
        // Arrange
        coEvery { settingsRepository.getAllowedApps() } returns Result.Success(emptyList<String>())
        coEvery { installedAppsHelper.getInstalledUserApps() } returns mockApps
        every { installedAppsHelper.validatePackages(any()) } returns emptyList()

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.updateSearchQuery("CHROME")
        advanceUntilIdle()

        // Act
        val filtered = viewModel.getFilteredApps()

        // Assert
        assertEquals("大文字小文字を無視してマッチするべき", 1, filtered.size)
    }

    // StateFlow テスト (Turbine使用)

    @Test
    fun `uiState emits loading then loaded state`() = runTest {
        // Arrange
        coEvery { settingsRepository.getAllowedApps() } returns Result.Success(emptyList<String>())
        coEvery { installedAppsHelper.getInstalledUserApps() } returns mockApps
        every { installedAppsHelper.validatePackages(any()) } returns emptyList()

        viewModel = createViewModel()

        // Assert with Turbine
        viewModel.uiState.test {
            // 初期状態（isLoading = true）
            val loading = awaitItem()
            assertTrue("最初はローディング中であるべき", loading.isLoading)

            advanceUntilIdle()

            // ロード完了状態
            val loaded = awaitItem()
            assertFalse("ロード完了後はローディング中でないべき", loaded.isLoading)
            assertEquals("インストール済みアプリがセットされるべき", mockApps, loaded.installedApps)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
