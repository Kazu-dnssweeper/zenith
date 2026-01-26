package com.iterio.app.ui.screens.tasks.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iterio.app.R
import com.iterio.app.domain.model.ScheduleType
import com.iterio.app.ui.theme.AccentTeal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * タスクのスケジュール設定セクション
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ScheduleSection(
    scheduleType: ScheduleType,
    repeatDays: Set<Int>,
    deadlineDate: LocalDate?,
    specificDate: LocalDate?,
    onScheduleTypeChange: (ScheduleType) -> Unit,
    onRepeatDaysChange: (Set<Int>) -> Unit,
    onDeadlineDateChange: (LocalDate?) -> Unit,
    onSpecificDateChange: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.schedule_title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        // スケジュールタイプ選択
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            ScheduleType.entries.forEachIndexed { index, type ->
                SegmentedButton(
                    selected = scheduleType == type,
                    onClick = { onScheduleTypeChange(type) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = ScheduleType.entries.size
                    )
                ) {
                    Text(
                        text = type.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // スケジュールタイプに応じた追加設定
        when (scheduleType) {
            ScheduleType.REPEAT -> {
                RepeatDaysSelector(
                    selectedDays = repeatDays,
                    onDaysChange = onRepeatDaysChange
                )
            }
            ScheduleType.DEADLINE -> {
                DateSelector(
                    label = stringResource(R.string.schedule_deadline_date),
                    selectedDate = deadlineDate,
                    onDateChange = onDeadlineDateChange
                )
            }
            ScheduleType.SPECIFIC -> {
                DateSelector(
                    label = stringResource(R.string.schedule_specific_date),
                    selectedDate = specificDate,
                    onDateChange = onSpecificDateChange
                )
            }
            ScheduleType.NONE -> {
                // 追加設定なし
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RepeatDaysSelector(
    selectedDays: Set<Int>,
    onDaysChange: (Set<Int>) -> Unit,
    modifier: Modifier = Modifier
) {
    val dayLabels = listOf(
        1 to stringResource(R.string.schedule_day_mon),
        2 to stringResource(R.string.schedule_day_tue),
        3 to stringResource(R.string.schedule_day_wed),
        4 to stringResource(R.string.schedule_day_thu),
        5 to stringResource(R.string.schedule_day_fri),
        6 to stringResource(R.string.schedule_day_sat),
        7 to stringResource(R.string.schedule_day_sun)
    )

    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.schedule_repeat_days),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            dayLabels.forEach { (dayNumber, dayLabel) ->
                FilterChip(
                    selected = selectedDays.contains(dayNumber),
                    onClick = {
                        val newDays = if (selectedDays.contains(dayNumber)) {
                            selectedDays - dayNumber
                        } else {
                            selectedDays + dayNumber
                        }
                        onDaysChange(newDays)
                    },
                    label = { Text(dayLabel) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentTeal.copy(alpha = 0.2f),
                        selectedLabelColor = AccentTeal
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateSelector(
    label: String,
    selectedDate: LocalDate?,
    onDateChange: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy/MM/dd") }

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Default.DateRange,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = selectedDate?.format(dateFormatter) ?: stringResource(R.string.schedule_select_date)
            )
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedDate?.let {
                    it.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                }
            )

            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val date = Instant.ofEpochMilli(millis)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                                onDateChange(date)
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text(stringResource(R.string.ok))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}
