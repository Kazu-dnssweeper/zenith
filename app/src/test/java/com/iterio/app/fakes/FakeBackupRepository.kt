package com.iterio.app.fakes

import android.net.Uri
import com.iterio.app.domain.model.BackupData
import com.iterio.app.domain.model.ImportResult
import com.iterio.app.domain.repository.BackupRepository

/**
 * テスト用の BackupRepository 実装
 */
class FakeBackupRepository : BackupRepository {

    var exportBackupData: BackupData? = null
    var importBackupResult = ImportResult(
        groupsImported = 0,
        tasksImported = 0,
        sessionsImported = 0,
        reviewTasksImported = 0,
        settingsImported = 0,
        statsImported = 0
    )
    var shouldFailExport = false
    var shouldFailImport = false
    var shouldFailRead = false
    var shouldFailWrite = false
    var storedData: BackupData? = null

    override suspend fun exportBackup(): BackupData {
        if (shouldFailExport) {
            throw RuntimeException("Export failed")
        }
        return exportBackupData ?: throw RuntimeException("No backup data set")
    }

    override suspend fun importBackup(data: BackupData): ImportResult {
        if (shouldFailImport) {
            throw RuntimeException("Import failed")
        }
        storedData = data
        return importBackupResult
    }

    override suspend fun writeToFile(data: BackupData, uri: Uri): Result<Unit> {
        if (shouldFailWrite) {
            return Result.failure(RuntimeException("Write failed"))
        }
        storedData = data
        return Result.success(Unit)
    }

    override suspend fun readFromFile(uri: Uri): Result<BackupData> {
        if (shouldFailRead) {
            return Result.failure(RuntimeException("Read failed"))
        }
        return storedData?.let { Result.success(it) }
            ?: Result.failure(RuntimeException("No data stored"))
    }

    override fun serializeToJson(data: BackupData): String {
        return """{"version":${data.version}}"""
    }

    override fun deserializeFromJson(json: String): Result<BackupData> {
        return storedData?.let { Result.success(it) }
            ?: Result.failure(RuntimeException("No data to deserialize"))
    }

    // Test helpers
    fun reset() {
        exportBackupData = null
        importBackupResult = ImportResult(
            groupsImported = 0,
            tasksImported = 0,
            sessionsImported = 0,
            reviewTasksImported = 0,
            settingsImported = 0,
            statsImported = 0
        )
        shouldFailExport = false
        shouldFailImport = false
        shouldFailRead = false
        shouldFailWrite = false
        storedData = null
    }
}
