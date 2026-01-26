package com.iterio.app.domain.usecase

import com.iterio.app.domain.common.DomainError
import com.iterio.app.domain.common.Result
import com.iterio.app.domain.model.PomodoroSettings
import com.iterio.app.domain.model.ReviewTask
import com.iterio.app.domain.model.Task
import com.iterio.app.domain.repository.ReviewTaskRepository
import com.iterio.app.domain.repository.StudySessionRepository
import com.iterio.app.domain.repository.TaskRepository
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * タイマーセッションを終了する UseCase
 *
 * - セッションの終了処理
 * - レビュータスクの生成（条件を満たす場合）
 */
class FinishTimerSessionUseCase @Inject constructor(
    private val studySessionRepository: StudySessionRepository,
    private val reviewTaskRepository: ReviewTaskRepository,
    private val taskRepository: TaskRepository
) {
    /**
     * セッションを終了し、必要に応じてレビュータスクを生成
     *
     * @param params 終了パラメータ
     * @return 成功またはエラー
     */
    suspend operator fun invoke(params: Params): Result<Unit, DomainError> {
        Timber.d("FinishTimerSession: reviewEnabled=${params.settings.reviewEnabled}, " +
                "isInterrupted=${params.isInterrupted}, " +
                "reviewIntervals=${params.reviewIntervals}")
        return try {
            // セッション完了時は totalCycles、それ以外は currentCycle を使用
            val cyclesCompleted = if (params.isSessionCompleted) {
                params.totalCycles
            } else {
                params.currentCycle
            }

            // セッションを終了
            studySessionRepository.finishSession(
                id = params.sessionId,
                durationMinutes = params.totalWorkMinutes,
                cycles = cyclesCompleted,
                interrupted = params.isInterrupted
            )

            // タスクの最終学習日時を更新
            taskRepository.updateLastStudiedAt(params.task.id, LocalDateTime.now())

            // レビュータスク生成条件:
            // - グローバルのレビュー機能が有効
            // - タスク個別のレビュー機能が有効
            // - 中断されていない
            // - レビュー間隔が設定されている
            if (params.settings.reviewEnabled &&
                params.task.reviewEnabled &&
                !params.isInterrupted &&
                params.reviewIntervals.isNotEmpty()
            ) {
                val today = LocalDate.now()
                val reviewTasks = params.reviewIntervals.mapIndexed { index, interval ->
                    ReviewTask(
                        studySessionId = params.sessionId,
                        taskId = params.task.id,
                        scheduledDate = today.plusDays(interval.toLong()),
                        reviewNumber = index + 1
                    )
                }
                Timber.d("Inserting ${reviewTasks.size} review tasks for taskId=${params.task.id}")
                reviewTaskRepository.insertAll(reviewTasks)
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(DomainError.DatabaseError("Failed to finish session", e))
        }
    }

    /**
     * セッション終了パラメータ
     */
    data class Params(
        val sessionId: Long,
        val task: Task,
        val settings: PomodoroSettings,
        val totalWorkMinutes: Int,
        val currentCycle: Int,
        val totalCycles: Int,
        val isInterrupted: Boolean,
        val isSessionCompleted: Boolean = false,
        val isPremium: Boolean,
        val reviewIntervals: List<Int>
    )
}
