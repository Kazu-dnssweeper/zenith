package com.iterio.app.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iterio.app.domain.model.Task
import com.iterio.app.ui.components.EmptySectionMessage
import com.iterio.app.ui.components.IterioCard
import com.iterio.app.ui.theme.AccentError
import com.iterio.app.ui.theme.AccentTeal
import com.iterio.app.ui.theme.AccentWarning
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * 期限が近いタスクセクション
 */
@Composable
fun UpcomingDeadlinesSection(
    tasks: List<Task>,
    onStartTimer: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    IterioCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        tint = AccentWarning,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "期限が近いタスク",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (tasks.isNotEmpty()) {
                    Text(
                        text = "${tasks.size}件",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (tasks.isEmpty()) {
                EmptySectionMessage(
                    icon = Icons.Default.DateRange,
                    message = "期限が近いタスクはありません"
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    tasks.forEach { task ->
                        UpcomingDeadlineItem(
                            task = task,
                            onStartTimer = { onStartTimer(task.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UpcomingDeadlineItem(
    task: Task,
    onStartTimer: () -> Unit
) {
    val today = LocalDate.now()
    val daysUntilDeadline = task.deadlineDate?.let {
        ChronoUnit.DAYS.between(today, it).toInt()
    } ?: 0

    val urgencyColor = getUrgencyColor(daysUntilDeadline)
    val dateFormatter = DateTimeFormatter.ofPattern("M/d")

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onStartTimer() },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface
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
                // Urgency indicator
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(urgencyColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = urgencyColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column {
                    Text(
                        text = task.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        task.deadlineDate?.let { deadline ->
                            Text(
                                text = "期限: ${deadline.format(dateFormatter)}（あと${daysUntilDeadline}日）",
                                style = MaterialTheme.typography.bodySmall,
                                color = urgencyColor
                            )
                        }
                    }
                }
            }

            FilledIconButton(
                onClick = onStartTimer,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "タイマー開始",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * 期限までの日数に応じた緊急度の色を返す
 * - 1日以内: AccentError（赤）
 * - 3日以内: AccentWarning（オレンジ）
 * - それ以外: AccentTeal（通常）
 */
private fun getUrgencyColor(daysUntilDeadline: Int): Color {
    return when {
        daysUntilDeadline <= 1 -> AccentError
        daysUntilDeadline <= 3 -> AccentWarning
        else -> AccentTeal
    }
}
