package com.iterio.app.ui.screens.tasks.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iterio.app.R
import com.iterio.app.domain.model.ScheduleType
import com.iterio.app.domain.model.Task
import java.time.LocalDate

@Composable
internal fun AddTaskDialog(
    defaultWorkDurationMinutes: Int,
    scheduleType: ScheduleType,
    repeatDays: Set<Int>,
    deadlineDate: LocalDate?,
    specificDate: LocalDate?,
    isPremium: Boolean,
    reviewCountOptions: List<Int>,
    reviewCount: Int?,
    reviewEnabled: Boolean,
    onScheduleTypeChange: (ScheduleType) -> Unit,
    onRepeatDaysChange: (Set<Int>) -> Unit,
    onDeadlineDateChange: (LocalDate?) -> Unit,
    onSpecificDateChange: (LocalDate?) -> Unit,
    onReviewCountChange: (Int?) -> Unit,
    onReviewEnabledChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (String, Int?, ScheduleType, Set<Int>, LocalDate?, LocalDate?, Int?, Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var useCustomDuration by remember { mutableStateOf(false) }
    var workDuration by remember { mutableFloatStateOf(defaultWorkDurationMinutes.toFloat()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.tasks_add_task)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.tasks_task_name)) },
                    placeholder = { Text(stringResource(R.string.tasks_task_name_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider()

                // Schedule Section
                ScheduleSection(
                    scheduleType = scheduleType,
                    repeatDays = repeatDays,
                    deadlineDate = deadlineDate,
                    specificDate = specificDate,
                    onScheduleTypeChange = onScheduleTypeChange,
                    onRepeatDaysChange = onRepeatDaysChange,
                    onDeadlineDateChange = onDeadlineDateChange,
                    onSpecificDateChange = onSpecificDateChange
                )

                HorizontalDivider()

                DurationSection(
                    useCustomDuration = useCustomDuration,
                    workDuration = workDuration,
                    defaultWorkDurationMinutes = defaultWorkDurationMinutes,
                    onUseCustomDurationChange = { useCustomDuration = it },
                    onWorkDurationChange = { workDuration = it }
                )

                HorizontalDivider()

                ReviewSection(
                    reviewEnabled = reviewEnabled,
                    onReviewEnabledChange = onReviewEnabledChange,
                    isPremium = isPremium,
                    reviewCountOptions = reviewCountOptions,
                    reviewCount = reviewCount,
                    onReviewCountChange = onReviewCountChange
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val duration = if (useCustomDuration) workDuration.toInt() else null
                    onConfirm(name, duration, scheduleType, repeatDays, deadlineDate, specificDate, reviewCount, reviewEnabled)
                },
                enabled = name.isNotBlank()
            ) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
internal fun EditTaskDialog(
    task: Task,
    scheduleType: ScheduleType,
    repeatDays: Set<Int>,
    deadlineDate: LocalDate?,
    specificDate: LocalDate?,
    isPremium: Boolean,
    reviewCountOptions: List<Int>,
    reviewCount: Int?,
    reviewEnabled: Boolean,
    onScheduleTypeChange: (ScheduleType) -> Unit,
    onRepeatDaysChange: (Set<Int>) -> Unit,
    onDeadlineDateChange: (LocalDate?) -> Unit,
    onSpecificDateChange: (LocalDate?) -> Unit,
    onReviewCountChange: (Int?) -> Unit,
    onReviewEnabledChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (Task) -> Unit,
    onDelete: (Task) -> Unit,
    onStartTimer: (Task) -> Unit
) {
    var name by remember { mutableStateOf(task.name) }
    var progressNote by remember { mutableStateOf(task.progressNote ?: "") }
    var progressPercent by remember { mutableStateOf(task.progressPercent?.toString() ?: "") }
    var nextGoal by remember { mutableStateOf(task.nextGoal ?: "") }
    var useCustomDuration by remember { mutableStateOf(task.workDurationMinutes != null) }
    var workDuration by remember { mutableFloatStateOf((task.workDurationMinutes ?: 25).toFloat()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.tasks_delete_confirm)) },
            text = { Text(stringResource(R.string.tasks_delete_task_message, task.name)) },
            confirmButton = {
                TextButton(
                    onClick = { onDelete(task) },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.tasks_edit_task)) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(R.string.tasks_task_name)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = progressNote,
                        onValueChange = { progressNote = it },
                        label = { Text(stringResource(R.string.tasks_progress_note)) },
                        placeholder = { Text(stringResource(R.string.tasks_progress_note_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = progressPercent,
                        onValueChange = { progressPercent = it.filter { c -> c.isDigit() }.take(3) },
                        label = { Text(stringResource(R.string.tasks_progress_percent)) },
                        placeholder = { Text(stringResource(R.string.tasks_progress_percent_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = nextGoal,
                        onValueChange = { nextGoal = it },
                        label = { Text(stringResource(R.string.tasks_next_goal)) },
                        placeholder = { Text(stringResource(R.string.tasks_next_goal_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider()

                    // Schedule Section
                    ScheduleSection(
                        scheduleType = scheduleType,
                        repeatDays = repeatDays,
                        deadlineDate = deadlineDate,
                        specificDate = specificDate,
                        onScheduleTypeChange = onScheduleTypeChange,
                        onRepeatDaysChange = onRepeatDaysChange,
                        onDeadlineDateChange = onDeadlineDateChange,
                        onSpecificDateChange = onSpecificDateChange
                    )

                    HorizontalDivider()

                    DurationSection(
                        useCustomDuration = useCustomDuration,
                        workDuration = workDuration,
                        defaultWorkDurationMinutes = 25,
                        onUseCustomDurationChange = { useCustomDuration = it },
                        onWorkDurationChange = { workDuration = it },
                        showDefaultMessage = !useCustomDuration
                    )

                    HorizontalDivider()

                    ReviewSection(
                        reviewEnabled = reviewEnabled,
                        onReviewEnabledChange = onReviewEnabledChange,
                        isPremium = isPremium,
                        reviewCountOptions = reviewCountOptions,
                        reviewCount = reviewCount,
                        onReviewCountChange = onReviewCountChange
                    )

                    HorizontalDivider()

                    // Delete button
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                        )
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.tasks_delete_task))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { onStartTimer(task) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.tasks_start_timer_with_task))
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirm(
                            task.copy(
                                name = name,
                                progressNote = progressNote.takeIf { it.isNotBlank() },
                                progressPercent = progressPercent.toIntOrNull()?.coerceIn(0, 100),
                                nextGoal = nextGoal.takeIf { it.isNotBlank() },
                                workDurationMinutes = if (useCustomDuration) workDuration.toInt() else null,
                                scheduleType = scheduleType,
                                repeatDays = repeatDays,
                                deadlineDate = deadlineDate,
                                specificDate = specificDate,
                                reviewCount = reviewCount,
                                reviewEnabled = reviewEnabled
                            )
                        )
                    },
                    enabled = name.isNotBlank()
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun DurationSection(
    useCustomDuration: Boolean,
    workDuration: Float,
    defaultWorkDurationMinutes: Int,
    onUseCustomDurationChange: (Boolean) -> Unit,
    onWorkDurationChange: (Float) -> Unit,
    showDefaultMessage: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.tasks_specify_duration),
            style = MaterialTheme.typography.bodyMedium
        )
        Switch(
            checked = useCustomDuration,
            onCheckedChange = onUseCustomDurationChange
        )
    }

    if (useCustomDuration) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { if (workDuration > 1f) onWorkDurationChange(workDuration - 1f) }
                ) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = stringResource(R.string.decrease_one_minute),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = stringResource(R.string.minutes_unit, workDuration.toInt()),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                IconButton(
                    onClick = { if (workDuration < 180f) onWorkDurationChange(workDuration + 1f) }
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.increase_one_minute),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Slider(
                value = workDuration,
                onValueChange = onWorkDurationChange,
                valueRange = 1f..180f,
                steps = 178
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.minutes_unit, 1), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(R.string.minutes_unit, 180), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    } else if (showDefaultMessage) {
        Text(
            text = stringResource(R.string.tasks_use_default_duration, defaultWorkDurationMinutes),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        Text(
            text = stringResource(R.string.tasks_use_default),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ReviewSection(
    reviewEnabled: Boolean,
    onReviewEnabledChange: (Boolean) -> Unit,
    isPremium: Boolean,
    reviewCountOptions: List<Int>,
    reviewCount: Int?,
    onReviewCountChange: (Int?) -> Unit
) {
    // Review Enabled Toggle
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.tasks_review_enabled),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(R.string.tasks_review_enabled_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = reviewEnabled,
            onCheckedChange = onReviewEnabledChange
        )
    }

    // Review Count Section (only show if reviewEnabled)
    if (reviewEnabled) {
        ReviewCountSection(
            isPremium = isPremium,
            reviewCountOptions = reviewCountOptions,
            reviewCount = reviewCount,
            onReviewCountChange = onReviewCountChange
        )
    }
}

@Composable
internal fun ReviewCountSection(
    isPremium: Boolean,
    reviewCountOptions: List<Int>,
    reviewCount: Int?,
    onReviewCountChange: (Int?) -> Unit
) {
    val minCount = 1
    val maxCount = 6
    val freeMaxCount = 2
    val displayCount = reviewCount ?: 2

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.tasks_review_count),
                style = MaterialTheme.typography.bodyMedium
            )
            if (!isPremium) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = stringResource(R.string.tasks_review_count_premium_only),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Default or custom toggle
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = reviewCount == null,
                onClick = { onReviewCountChange(null) },
                label = {
                    Text(stringResource(R.string.tasks_use_default))
                }
            )
            FilterChip(
                selected = reviewCount != null,
                onClick = {
                    if (reviewCount == null) {
                        onReviewCountChange(2)
                    }
                },
                label = {
                    Text(
                        if (reviewCount != null)
                            stringResource(R.string.tasks_review_count_option, reviewCount)
                        else
                            stringResource(R.string.tasks_review_count_option, 2)
                    )
                }
            )
        }

        // Show slider only if custom value is selected
        if (reviewCount != null) {
            Slider(
                value = displayCount.toFloat(),
                onValueChange = { newValue ->
                    val count = newValue.toInt()
                    if (isPremium || count <= freeMaxCount) {
                        onReviewCountChange(count)
                    }
                },
                valueRange = minCount.toFloat()..maxCount.toFloat(),
                steps = maxCount - minCount - 1,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.settings_review_count_min),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.settings_review_count_max),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
