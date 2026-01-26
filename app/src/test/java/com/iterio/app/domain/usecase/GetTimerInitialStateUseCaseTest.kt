package com.iterio.app.domain.usecase

import com.iterio.app.domain.common.Result
import com.iterio.app.domain.model.PomodoroSettings
import com.iterio.app.domain.model.Task
import com.iterio.app.domain.repository.SettingsRepository
import com.iterio.app.domain.repository.TaskRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * GetTimerInitialStateUseCase のユニットテスト
 */
class GetTimerInitialStateUseCaseTest {

    private lateinit var taskRepository: TaskRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: GetTimerInitialStateUseCase

    @Before
    fun setup() {
        taskRepository = mockk()
        settingsRepository = mockk()
        useCase = GetTimerInitialStateUseCase(taskRepository, settingsRepository)
    }

    @Test
    fun `returns task and settings when task exists`() = runTest {
        val task = Task(id = 1L, groupId = 1L, name = "Math Study")
        val settings = PomodoroSettings(workDurationMinutes = 25)
        coEvery { taskRepository.getTaskById(1L) } returns Result.Success(task)
        coEvery { settingsRepository.getPomodoroSettings() } returns Result.Success(settings)
        coEvery { settingsRepository.getAllowedApps() } returns Result.Success(emptyList())

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        assertEquals(task, result.getOrNull()?.task)
        assertEquals(settings, result.getOrNull()?.settings)
    }

    @Test
    fun `returns failure when task not found`() = runTest {
        coEvery { taskRepository.getTaskById(999L) } returns Result.Success(null)
        coEvery { settingsRepository.getPomodoroSettings() } returns Result.Success(PomodoroSettings())
        coEvery { settingsRepository.getAllowedApps() } returns Result.Success(emptyList())

        val result = useCase(999L)

        assertTrue(result.isFailure)
    }

    @Test
    fun `uses task specific work duration when available`() = runTest {
        val task = Task(id = 1L, groupId = 1L, name = "Task", workDurationMinutes = 45)
        val settings = PomodoroSettings(workDurationMinutes = 25)
        coEvery { taskRepository.getTaskById(1L) } returns Result.Success(task)
        coEvery { settingsRepository.getPomodoroSettings() } returns Result.Success(settings)
        coEvery { settingsRepository.getAllowedApps() } returns Result.Success(emptyList())

        val result = useCase(1L)

        assertEquals(45, result.getOrNull()?.effectiveWorkDurationMinutes)
    }

    @Test
    fun `uses settings default when task has no specific duration`() = runTest {
        val task = Task(id = 1L, groupId = 1L, name = "Task", workDurationMinutes = null)
        val settings = PomodoroSettings(workDurationMinutes = 30)
        coEvery { taskRepository.getTaskById(1L) } returns Result.Success(task)
        coEvery { settingsRepository.getPomodoroSettings() } returns Result.Success(settings)
        coEvery { settingsRepository.getAllowedApps() } returns Result.Success(emptyList())

        val result = useCase(1L)

        assertEquals(30, result.getOrNull()?.effectiveWorkDurationMinutes)
    }

    @Test
    fun `returns allowed apps from settings`() = runTest {
        val task = Task(id = 1L, groupId = 1L, name = "Task")
        val settings = PomodoroSettings()
        val allowedApps = listOf("com.app1", "com.app2")
        coEvery { taskRepository.getTaskById(1L) } returns Result.Success(task)
        coEvery { settingsRepository.getPomodoroSettings() } returns Result.Success(settings)
        coEvery { settingsRepository.getAllowedApps() } returns Result.Success(allowedApps)

        val result = useCase(1L)

        assertEquals(setOf("com.app1", "com.app2"), result.getOrNull()?.defaultAllowedApps)
    }

    @Test
    fun `calculates total time in seconds correctly`() = runTest {
        val task = Task(id = 1L, groupId = 1L, name = "Task", workDurationMinutes = 25)
        val settings = PomodoroSettings()
        coEvery { taskRepository.getTaskById(1L) } returns Result.Success(task)
        coEvery { settingsRepository.getPomodoroSettings() } returns Result.Success(settings)
        coEvery { settingsRepository.getAllowedApps() } returns Result.Success(emptyList())

        val result = useCase(1L)

        assertEquals(25 * 60, result.getOrNull()?.totalTimeSeconds)
    }
}
