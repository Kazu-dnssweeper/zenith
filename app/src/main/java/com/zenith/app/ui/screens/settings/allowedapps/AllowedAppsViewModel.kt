package com.zenith.app.ui.screens.settings.allowedapps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenith.app.domain.model.AllowedApp
import com.zenith.app.domain.repository.SettingsRepository
import com.zenith.app.util.InstalledAppsHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AllowedAppsUiState(
    val isLoading: Boolean = true,
    val installedApps: List<AllowedApp> = emptyList(),
    val selectedPackages: Set<String> = emptySet(),
    val searchQuery: String = "",
    val immediateSearchQuery: String = "" // UI表示用（即時更新）
)

@HiltViewModel
class AllowedAppsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val installedAppsHelper: InstalledAppsHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(AllowedAppsUiState())
    val uiState: StateFlow<AllowedAppsUiState> = _uiState.asStateFlow()

    // 検索デバウンス用
    private var searchJob: Job? = null
    private val SEARCH_DEBOUNCE_MS = 300L

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // 並行して許可アプリ設定とインストール済みアプリを取得
            val savedAllowedApps = settingsRepository.getAllowedApps()
            val installedApps = installedAppsHelper.getInstalledUserApps()

            // 削除されたアプリを検証してフィルタリング
            val validatedPackages = installedAppsHelper.validatePackages(savedAllowedApps)

            // 削除されたアプリがあれば設定を更新
            if (validatedPackages.size != savedAllowedApps.size) {
                settingsRepository.setAllowedApps(validatedPackages)
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    installedApps = installedApps,
                    selectedPackages = validatedPackages.toSet()
                )
            }
        }
    }

    fun toggleAppSelection(packageName: String) {
        _uiState.update { state ->
            val newSelected = if (state.selectedPackages.contains(packageName)) {
                state.selectedPackages - packageName
            } else {
                state.selectedPackages + packageName
            }
            state.copy(selectedPackages = newSelected)
        }
        saveSettings()
    }

    /**
     * 検索クエリを更新（デバウンス付き）
     * UIのテキストフィールドは即時更新し、フィルタリングは300ms後に実行
     */
    fun updateSearchQuery(query: String) {
        // UI表示用は即時更新
        _uiState.update { it.copy(immediateSearchQuery = query) }

        // フィルタリングはデバウンス
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            _uiState.update { it.copy(searchQuery = query) }
        }
    }

    fun selectAll() {
        _uiState.update { state ->
            val filteredApps = getFilteredApps(state)
            state.copy(selectedPackages = state.selectedPackages + filteredApps.map { it.packageName })
        }
        saveSettings()
    }

    fun deselectAll() {
        _uiState.update { state ->
            val filteredApps = getFilteredApps(state)
            state.copy(selectedPackages = state.selectedPackages - filteredApps.map { it.packageName }.toSet())
        }
        saveSettings()
    }

    fun getFilteredApps(): List<AllowedApp> {
        return getFilteredApps(_uiState.value)
    }

    private fun getFilteredApps(state: AllowedAppsUiState): List<AllowedApp> {
        return if (state.searchQuery.isBlank()) {
            state.installedApps
        } else {
            state.installedApps.filter {
                it.appName.contains(state.searchQuery, ignoreCase = true) ||
                        it.packageName.contains(state.searchQuery, ignoreCase = true)
            }
        }
    }

    private fun saveSettings() {
        viewModelScope.launch {
            settingsRepository.setAllowedApps(_uiState.value.selectedPackages.toList())
        }
    }
}
