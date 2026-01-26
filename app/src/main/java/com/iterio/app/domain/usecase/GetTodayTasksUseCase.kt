package com.iterio.app.domain.usecase

import com.iterio.app.domain.model.ReviewTask
import com.iterio.app.domain.model.Task
import com.iterio.app.domain.repository.ReviewTaskRepository
import com.iterio.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import javax.inject.Inject

/**
 * 今日のタスクを取得する UseCase
 *
 * - 今日予定されているタスク
 * - 今日と過去の未完了レビュータスク
 */
class GetTodayTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val reviewTaskRepository: ReviewTaskRepository
) {
    /**
     * 今日のタスクとレビュータスクを取得
     *
     * @param date 対象日（デフォルトは今日）
     * @return タスク情報の Flow
     */
    operator fun invoke(date: LocalDate = LocalDate.now()): Flow<TodayTasksResult> {
        return combine(
            taskRepository.getTodayScheduledTasks(date),
            reviewTaskRepository.getOverdueAndTodayTasks(date)
        ) { scheduledTasks, reviewTasks ->
            TodayTasksResult(
                scheduledTasks = scheduledTasks,
                reviewTasks = reviewTasks
            )
        }
    }
}

/**
 * 今日のタスク結果データ
 */
data class TodayTasksResult(
    val scheduledTasks: List<Task>,
    val reviewTasks: List<ReviewTask>
) {
    /**
     * 合計タスク数
     */
    val totalCount: Int
        get() = scheduledTasks.size + reviewTasks.size
}
