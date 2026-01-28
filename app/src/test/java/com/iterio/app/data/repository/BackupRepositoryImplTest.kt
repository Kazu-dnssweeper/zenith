package com.iterio.app.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.iterio.app.data.encryption.EncryptionManager
import com.iterio.app.data.local.IterioDatabase
import com.iterio.app.data.local.dao.DailyStatsDao
import com.iterio.app.data.local.dao.ReviewTaskDao
import com.iterio.app.data.local.dao.SettingsDao
import com.iterio.app.data.local.dao.StudySessionDao
import com.iterio.app.data.local.dao.SubjectGroupDao
import com.iterio.app.data.local.dao.TaskDao
import com.iterio.app.domain.model.BackupData
import com.iterio.app.domain.model.DailyStatsBackup
import com.iterio.app.domain.model.SubjectGroupBackup
import com.iterio.app.domain.model.TaskBackup
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * BackupRepositoryImpl のユニットテスト
 *
 * serializeToJson / deserializeFromJson を中心にテスト
 * （exportBackup / importBackup は Room withTransaction が必要なため統合テスト向き）
 */
class BackupRepositoryImplTest {

    private lateinit var repository: BackupRepositoryImpl
    private lateinit var gson: Gson

    @Before
    fun setup() {
        gson = GsonBuilder().create()

        repository = BackupRepositoryImpl(
            context = mockk(relaxed = true),
            database = mockk(relaxed = true),
            subjectGroupDao = mockk(relaxed = true),
            taskDao = mockk(relaxed = true),
            studySessionDao = mockk(relaxed = true),
            reviewTaskDao = mockk(relaxed = true),
            settingsDao = mockk(relaxed = true),
            dailyStatsDao = mockk(relaxed = true),
            gson = gson,
            encryptionManager = mockk(relaxed = true)
        )
    }

    private fun createSampleBackupData(): BackupData {
        return BackupData(
            version = BackupData.CURRENT_VERSION,
            exportedAt = "2024-01-15T10:30:00",
            subjectGroups = listOf(
                SubjectGroupBackup(
                    id = 1L,
                    name = "数学",
                    colorHex = "#FF0000",
                    displayOrder = 0,
                    createdAt = "2024-01-01T00:00:00"
                )
            ),
            tasks = listOf(
                TaskBackup(
                    id = 1L,
                    groupId = 1L,
                    name = "微分積分",
                    progressNote = "第3章まで完了",
                    progressPercent = 60,
                    nextGoal = "第4章",
                    workDurationMinutes = 30,
                    isActive = true,
                    createdAt = "2024-01-01T00:00:00",
                    updatedAt = "2024-01-15T10:00:00"
                )
            ),
            studySessions = emptyList(),
            reviewTasks = emptyList(),
            settings = mapOf("theme" to "dark"),
            dailyStats = listOf(
                DailyStatsBackup(
                    date = "2024-01-15",
                    totalStudyMinutes = 120,
                    sessionCount = 4,
                    subjectBreakdownJson = """{"数学":60,"英語":60}"""
                )
            )
        )
    }

    // ==================== serializeToJson テスト ====================

    @Test
    fun `serializeToJson produces valid JSON`() {
        val data = createSampleBackupData()

        val json = repository.serializeToJson(data)

        assertNotNull(json)
        assertTrue(json.isNotEmpty())
        assertTrue(json.contains("\"version\""))
        assertTrue(json.contains("\"exportedAt\""))
    }

    @Test
    fun `serializeToJson contains all backup sections`() {
        val data = createSampleBackupData()

        val json = repository.serializeToJson(data)

        assertTrue(json.contains("\"subjectGroups\""))
        assertTrue(json.contains("\"tasks\""))
        assertTrue(json.contains("\"studySessions\""))
        assertTrue(json.contains("\"reviewTasks\""))
        assertTrue(json.contains("\"settings\""))
        assertTrue(json.contains("\"dailyStats\""))
    }

    @Test
    fun `serializeToJson preserves group data`() {
        val data = createSampleBackupData()

        val json = repository.serializeToJson(data)

        assertTrue(json.contains("数学"))
        assertTrue(json.contains("#FF0000"))
    }

    @Test
    fun `serializeToJson preserves task data`() {
        val data = createSampleBackupData()

        val json = repository.serializeToJson(data)

        assertTrue(json.contains("微分積分"))
        assertTrue(json.contains("第3章まで完了"))
    }

    // ==================== deserializeFromJson テスト ====================

    @Test
    fun `deserializeFromJson roundtrip succeeds`() {
        val original = createSampleBackupData()
        val json = repository.serializeToJson(original)

        val result = repository.deserializeFromJson(json)

        assertTrue(result.isSuccess)
        val restored = result.getOrNull()!!
        assertEquals(original.version, restored.version)
        assertEquals(original.exportedAt, restored.exportedAt)
        assertEquals(original.subjectGroups.size, restored.subjectGroups.size)
        assertEquals(original.tasks.size, restored.tasks.size)
        assertEquals(original.settings, restored.settings)
        assertEquals(original.dailyStats.size, restored.dailyStats.size)
    }

    @Test
    fun `deserializeFromJson preserves subject group details`() {
        val original = createSampleBackupData()
        val json = repository.serializeToJson(original)

        val result = repository.deserializeFromJson(json)

        val group = result.getOrNull()!!.subjectGroups.first()
        assertEquals(1L, group.id)
        assertEquals("数学", group.name)
        assertEquals("#FF0000", group.colorHex)
        assertEquals(0, group.displayOrder)
    }

    @Test
    fun `deserializeFromJson preserves task details`() {
        val original = createSampleBackupData()
        val json = repository.serializeToJson(original)

        val result = repository.deserializeFromJson(json)

        val task = result.getOrNull()!!.tasks.first()
        assertEquals(1L, task.id)
        assertEquals("微分積分", task.name)
        assertEquals(60, task.progressPercent)
        assertEquals(30, task.workDurationMinutes)
        assertTrue(task.isActive)
    }

    @Test
    fun `deserializeFromJson fails for version mismatch`() {
        // Create JSON with future version
        val futureVersion = BackupData.CURRENT_VERSION + 1
        val json = """{"version":$futureVersion,"exportedAt":"2024-01-01T00:00:00","subjectGroups":[],"tasks":[],"studySessions":[],"reviewTasks":[],"settings":{},"dailyStats":[]}"""

        val result = repository.deserializeFromJson(json)

        assertTrue(result.isFailure)
    }

    @Test
    fun `deserializeFromJson fails for invalid JSON`() {
        val result = repository.deserializeFromJson("{invalid json!!!")

        assertTrue(result.isFailure)
    }

    @Test
    fun `deserializeFromJson fails for empty string`() {
        val result = repository.deserializeFromJson("")

        assertTrue(result.isFailure)
    }

    @Test
    fun `deserializeFromJson succeeds with empty collections`() {
        val json = """{"version":1,"exportedAt":"2024-01-01T00:00:00","subjectGroups":[],"tasks":[],"studySessions":[],"reviewTasks":[],"settings":{},"dailyStats":[]}"""

        val result = repository.deserializeFromJson(json)

        assertTrue(result.isSuccess)
        val data = result.getOrNull()!!
        assertTrue(data.subjectGroups.isEmpty())
        assertTrue(data.tasks.isEmpty())
        assertTrue(data.studySessions.isEmpty())
        assertTrue(data.reviewTasks.isEmpty())
        assertTrue(data.settings.isEmpty())
        assertTrue(data.dailyStats.isEmpty())
    }

    @Test
    fun `deserializeFromJson succeeds with current version`() {
        val json = """{"version":${BackupData.CURRENT_VERSION},"exportedAt":"2024-01-01T00:00:00","subjectGroups":[],"tasks":[],"studySessions":[],"reviewTasks":[],"settings":{},"dailyStats":[]}"""

        val result = repository.deserializeFromJson(json)

        assertTrue(result.isSuccess)
        assertEquals(BackupData.CURRENT_VERSION, result.getOrNull()!!.version)
    }

    // ==================== serializeToJson → deserializeFromJson 一貫性テスト ====================

    @Test
    fun `serialize then deserialize preserves settings map`() {
        val data = createSampleBackupData().copy(
            settings = mapOf("theme" to "dark", "language" to "ja", "notifications" to "true")
        )
        val json = repository.serializeToJson(data)

        val result = repository.deserializeFromJson(json)

        val settings = result.getOrNull()!!.settings
        assertEquals(3, settings.size)
        assertEquals("dark", settings["theme"])
        assertEquals("ja", settings["language"])
        assertEquals("true", settings["notifications"])
    }

    @Test
    fun `serialize then deserialize preserves daily stats`() {
        val data = createSampleBackupData()
        val json = repository.serializeToJson(data)

        val result = repository.deserializeFromJson(json)

        val stats = result.getOrNull()!!.dailyStats.first()
        assertEquals("2024-01-15", stats.date)
        assertEquals(120, stats.totalStudyMinutes)
        assertEquals(4, stats.sessionCount)
    }
}
