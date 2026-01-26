package com.iterio.app.ui.screens.calendar.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.iterio.app.R
import com.iterio.app.domain.model.DailyStats
import com.iterio.app.domain.model.ReviewTask
import com.iterio.app.domain.model.Task
import com.iterio.app.ui.components.IterioCard
import com.iterio.app.ui.theme.AccentSuccess
import com.iterio.app.ui.theme.AccentTeal
import com.iterio.app.ui.theme.SurfaceVariantDark
import com.iterio.app.ui.theme.TextPrimary
import com.iterio.app.ui.theme.TextSecondary
import java.time.LocalDate

@Composable
fun SelectedDateInfo(
    date: LocalDate,
    stats: DailyStats?,
    tasks: List<Task>,
    reviewTasks: List<ReviewTask>,
    onStartTimer: (Long) -> Unit,
    onToggleReviewTaskComplete: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    IterioCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.calendar_study_on_date, date.monthValue, date.dayOfMonth),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            if (stats != null && stats.hasStudied) {
                Text(
                    text = stringResource(R.string.calendar_study_time, stats.formattedTotalTime),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = stringResource(R.string.calendar_session_count, stats.sessionCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                Text(
                    text = stringResource(R.string.calendar_no_study_record),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Task list for this date
            if (tasks.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                Text(
                    text = stringResource(R.string.calendar_tasks_on_day),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tasks.forEach { task ->
                        SelectedDateTaskItem(
                            task = task,
                            onStartTimer = { onStartTimer(task.id) }
                        )
                    }
                }
            }

            // Review task list for this date
            if (reviewTasks.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        tint = AccentTeal,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.calendar_review_tasks),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val completedCount = reviewTasks.count { it.isCompleted }
                    Text(
                        text = stringResource(R.string.calendar_completed_count, completedCount, reviewTasks.size),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (completedCount == reviewTasks.size) AccentSuccess else TextSecondary
                    )
                }
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    reviewTasks.forEach { reviewTask ->
                        SelectedDateReviewTaskItem(
                            reviewTask = reviewTask,
                            onToggleComplete = { onToggleReviewTaskComplete(reviewTask.id, !reviewTask.isCompleted) },
                            onStartTimer = { onStartTimer(reviewTask.taskId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedDateTaskItem(
    task: Task,
    onStartTimer: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = SurfaceVariantDark
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onStartTimer() }
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                task.scheduleLabel?.let { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            IconButton(
                onClick = onStartTimer,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = stringResource(R.string.timer_start),
                    tint = AccentTeal
                )
            }
        }
    }
}

@Composable
private fun SelectedDateReviewTaskItem(
    reviewTask: ReviewTask,
    onToggleComplete: () -> Unit,
    onStartTimer: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (reviewTask.isCompleted) {
            AccentSuccess.copy(alpha = 0.1f)
        } else {
            SurfaceVariantDark
        },
        label = "backgroundColor"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Checkbox for toggle complete
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            if (reviewTask.isCompleted) AccentSuccess
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { onToggleComplete() },
                    contentAlignment = Alignment.Center
                ) {
                    if (reviewTask.isCompleted) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = stringResource(R.string.calendar_completed),
                            tint = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reviewTask.taskName ?: stringResource(R.string.default_task_name),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (reviewTask.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (reviewTask.isCompleted) {
                            TextPrimary.copy(alpha = 0.6f)
                        } else {
                            TextPrimary
                        }
                    )
                    Text(
                        text = reviewTask.reviewLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (reviewTask.isCompleted) {
                            TextSecondary.copy(alpha = 0.6f)
                        } else {
                            AccentTeal
                        }
                    )
                }
            }

            reviewTask.groupName?.let { groupName ->
                Text(
                    text = groupName,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            // Play button for starting timer
            IconButton(
                onClick = onStartTimer,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = stringResource(R.string.timer_start),
                    tint = AccentTeal
                )
            }
        }
    }
}
