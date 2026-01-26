package com.iterio.app.domain.usecase

import com.iterio.app.domain.common.DomainError
import com.iterio.app.domain.common.Result
import com.iterio.app.domain.model.PomodoroSettings
import com.iterio.app.domain.model.Task
import com.iterio.app.domain.repository.SettingsRepository
import com.iterio.app.domain.repository.TaskRepository
import com.iterio.app.util.TimeConstants
import javax.inject.Inject

/**
 * タイマー画面の初期状態を取得する UseCase
 *
 * - タスク情報の取得
 * - ポモドーロ設定の取得
 * - 有効な作業時間の計算（タスク固有 or デフォルト）
 * - 許可アプリリストの取得
 */
class GetTimerInitialStateUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val settingsRepository: SettingsRepository
) {
    /**
     * タイマー初期状態を取得
     *
     * @param taskId 対象タスクのID
     * @return 初期状態データまたはエラー
     */
    suspend operator fun invoke(taskId: Long): Result<TimerInitialState, DomainError> {
        val taskResult = taskRepository.getTaskById(taskId)
        return taskResult.flatMap { task ->
            if (task == null) {
                return@flatMap Result.Failure(DomainError.NotFoundError("Task not found: $taskId"))
            }

            val settingsResult = settingsRepository.getPomodoroSettings()
            val allowedAppsResult = settingsRepository.getAllowedApps()

            settingsResult.flatMap { settings ->
                val allowedApps = allowedAppsResult.getOrDefault(emptyList()).toSet()

                // タスク固有の作業時間があればそれを使用、なければ設定のデフォルト値
                val effectiveWorkDuration = task.workDurationMinutes ?: settings.workDurationMinutes
                val totalTimeSeconds = effectiveWorkDuration * TimeConstants.SECONDS_PER_MINUTE

                Result.Success(
                    TimerInitialState(
                        task = task,
                        settings = settings,
                        effectiveWorkDurationMinutes = effectiveWorkDuration,
                        totalTimeSeconds = totalTimeSeconds,
                        defaultAllowedApps = allowedApps
                    )
                )
            }
        }
    }
}

/**
 * タイマー初期状態データ
 */
data class TimerInitialState(
    val task: Task,
    val settings: PomodoroSettings,
    val effectiveWorkDurationMinutes: Int,
    val totalTimeSeconds: Int,
    val defaultAllowedApps: Set<String>
)
