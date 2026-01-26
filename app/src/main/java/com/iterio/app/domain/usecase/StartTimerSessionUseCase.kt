package com.iterio.app.domain.usecase

import com.iterio.app.domain.common.DomainError
import com.iterio.app.domain.common.Result
import com.iterio.app.domain.model.PomodoroSettings
import com.iterio.app.domain.model.StudySession
import com.iterio.app.domain.model.Task
import com.iterio.app.domain.repository.StudySessionRepository
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * タイマーセッションを開始する UseCase
 *
 * 新しい StudySession を作成し、データベースに保存する
 */
class StartTimerSessionUseCase @Inject constructor(
    private val studySessionRepository: StudySessionRepository
) {
    /**
     * 新しいセッションを作成
     *
     * @param task 対象タスク
     * @param settings ポモドーロ設定
     * @param cycles セッションのサイクル数
     * @param startTime セッション開始時刻（省略時は現在時刻）
     * @return 作成されたセッションのID
     */
    suspend operator fun invoke(
        task: Task,
        settings: PomodoroSettings,
        cycles: Int,
        startTime: LocalDateTime = LocalDateTime.now()
    ): Result<Long, DomainError> {
        return try {
            // タスク固有の作業時間があればそれを使用
            val workDurationMinutes = task.workDurationMinutes ?: settings.workDurationMinutes
            val plannedDuration = workDurationMinutes * cycles

            val session = StudySession(
                taskId = task.id,
                startedAt = startTime,
                plannedDurationMinutes = plannedDuration
            )

            val sessionId = studySessionRepository.insertSession(session)
            Result.Success(sessionId)
        } catch (e: Exception) {
            Result.Failure(DomainError.DatabaseError("Failed to create session", e))
        }
    }
}
