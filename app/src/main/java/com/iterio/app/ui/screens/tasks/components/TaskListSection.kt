package com.iterio.app.ui.screens.tasks.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iterio.app.R
import com.iterio.app.domain.model.ScheduleType
import com.iterio.app.domain.model.Task
import com.iterio.app.ui.theme.AccentTeal
import com.iterio.app.ui.theme.AccentWarning

@Composable
internal fun TaskList(
    tasks: List<Task>,
    onTaskClick: (Task) -> Unit,
    onStartTimer: (Task) -> Unit
) {
    if (tasks.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Assignment,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Text(
                    stringResource(R.string.tasks_no_tasks),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Text(
                    stringResource(R.string.tasks_add_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tasks, key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    onClick = { onTaskClick(task) },
                    onStartTimer = { onStartTimer(task) }
                )
            }
        }
    }
}

@Composable
internal fun TaskCard(
    task: Task,
    onClick: () -> Unit,
    onStartTimer: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = task.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    // Schedule badge
                    if (task.scheduleType != ScheduleType.NONE) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (task.isOverdue) AccentWarning.copy(alpha = 0.2f) else AccentTeal.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = when (task.scheduleType) {
                                        ScheduleType.REPEAT -> Icons.Default.Repeat
                                        ScheduleType.DEADLINE -> Icons.Default.Schedule
                                        ScheduleType.SPECIFIC -> Icons.Default.DateRange
                                        else -> Icons.Default.Schedule
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = if (task.isOverdue) AccentWarning else AccentTeal
                                )
                                Text(
                                    text = task.scheduleLabel ?: "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (task.isOverdue) AccentWarning else AccentTeal
                                )
                            }
                        }
                    }
                }
                if (task.progressNote != null || task.progressPercent != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        task.progressPercent?.let { percent ->
                            LinearProgressIndicator(
                                progress = { percent / 100f },
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(4.dp),
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = "$percent%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        task.progressNote?.let { note ->
                            Text(
                                text = note,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
                task.nextGoal?.let { goal ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.tasks_next_prefix, goal),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                // Last studied info
                task.lastStudiedLabel?.let { label ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onStartTimer) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = stringResource(R.string.timer_start),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
internal fun EmptyGroupsMessage(onAddGroup: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Folder,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                stringResource(R.string.tasks_no_groups),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Button(onClick = onAddGroup) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.tasks_add_group_button))
            }
        }
    }
}
