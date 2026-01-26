package com.iterio.app.ui.screens.calendar

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iterio.app.R
import com.iterio.app.domain.model.DailyStats
import com.iterio.app.domain.model.PremiumFeature
import com.iterio.app.domain.model.ReviewTask
import com.iterio.app.domain.model.Task
import com.iterio.app.ui.components.LockedFeatureCard
import com.iterio.app.ui.components.IterioCard
import com.iterio.app.ui.components.IterioTopBar
import com.iterio.app.ui.premium.PremiumUpsellDialog
import com.iterio.app.ui.theme.AccentSuccess
import com.iterio.app.ui.theme.AccentTeal
import com.iterio.app.ui.theme.BackgroundDark
import com.iterio.app.ui.theme.HeatmapColors
import com.iterio.app.ui.theme.SurfaceVariantDark
import com.iterio.app.ui.theme.TextPrimary
import com.iterio.app.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarScreen(
    onStartTimer: (Long) -> Unit = {},
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val subscriptionStatus by viewModel.subscriptionStatus.collectAsStateWithLifecycle()
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()

    var showPremiumUpsellDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            IterioTopBar(title = stringResource(R.string.calendar_title))
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Month navigation
            MonthHeader(
                yearMonth = uiState.currentMonth,
                onPreviousMonth = viewModel::previousMonth,
                onNextMonth = viewModel::nextMonth
            )

            if (isPremium) {
                // Premium: ヒートマップ表示
                IterioCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        WeekdayHeader()
                        CalendarGrid(
                            yearMonth = uiState.currentMonth,
                            dailyStats = uiState.dailyStats,
                            taskCountByDate = uiState.taskCountByDate,
                            selectedDate = uiState.selectedDate,
                            onDateClick = viewModel::selectDate
                        )
                    }
                }

                HeatmapLegend()
            } else {
                // 無料版: リスト表示 + Premium誘導
                IterioCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        WeekdayHeader()
                        // グレースケールのカレンダー表示
                        CalendarGridSimple(
                            yearMonth = uiState.currentMonth,
                            dailyStats = uiState.dailyStats,
                            taskCountByDate = uiState.taskCountByDate,
                            selectedDate = uiState.selectedDate,
                            onDateClick = viewModel::selectDate
                        )
                    }
                }

                // Premium誘導カード
                LockedFeatureCard(
                    feature = PremiumFeature.CALENDAR_HEATMAP,
                    onClick = { showPremiumUpsellDialog = true },
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            // Selected date info
            uiState.selectedDate?.let { date ->
                val stats = uiState.dailyStats[date]
                SelectedDateInfo(
                    date = date,
                    stats = stats,
                    tasks = uiState.selectedDateTasks,
                    reviewTasks = uiState.selectedDateReviewTasks,
                    onStartTimer = onStartTimer,
                    onToggleReviewTaskComplete = viewModel::toggleReviewTaskComplete
                )
            }
        }
    }

    // Premium誘導ダイアログ
    if (showPremiumUpsellDialog) {
        PremiumUpsellDialog(
            feature = PremiumFeature.CALENDAR_HEATMAP,
            onDismiss = { showPremiumUpsellDialog = false },
            onStartTrial = {
                viewModel.startTrial()
                showPremiumUpsellDialog = false
            },
            onUpgrade = {
                showPremiumUpsellDialog = false
            },
            trialAvailable = subscriptionStatus.canStartTrial
        )
    }
}

@Composable
private fun MonthHeader(
    yearMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.calendar_prev_month),
                tint = TextPrimary
            )
        }

        Text(
            text = stringResource(R.string.calendar_year_month_format, yearMonth.year, yearMonth.monthValue),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.calendar_next_month),
                tint = TextPrimary
            )
        }
    }
}

@Composable
private fun WeekdayHeader(modifier: Modifier = Modifier) {
    val weekdays = listOf(
        stringResource(R.string.calendar_day_sun),
        stringResource(R.string.calendar_day_mon),
        stringResource(R.string.calendar_day_tue),
        stringResource(R.string.calendar_day_wed),
        stringResource(R.string.calendar_day_thu),
        stringResource(R.string.calendar_day_fri),
        stringResource(R.string.calendar_day_sat)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        weekdays.forEach { day ->
            Text(
                text = day,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    dailyStats: Map<LocalDate, DailyStats>,
    taskCountByDate: Map<LocalDate, Int>,
    selectedDate: LocalDate?,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    val lastDayOfMonth = yearMonth.atEndOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // Sunday = 0

    val days = mutableListOf<LocalDate?>()

    // Add empty slots for days before the first day of month
    repeat(firstDayOfWeek) { days.add(null) }

    // Add all days of the month
    var currentDay = firstDayOfMonth
    while (!currentDay.isAfter(lastDayOfMonth)) {
        days.add(currentDay)
        currentDay = currentDay.plusDays(1)
    }

    // Create rows of 7 days
    val weeks = days.chunked(7)

    Column(modifier = modifier) {
        weeks.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                week.forEach { date ->
                    if (date != null) {
                        val taskCount = taskCountByDate[date] ?: 0
                        val taskHeatmapLevel = calculateTaskHeatmapLevel(taskCount)

                        DayCell(
                            date = date,
                            heatmapColor = HeatmapColors[taskHeatmapLevel],
                            taskCount = taskCount,
                            isSelected = date == selectedDate,
                            isToday = date == LocalDate.now(),
                            onClick = { onDateClick(date) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
                // Fill remaining cells if week is incomplete
                repeat(7 - week.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    heatmapColor: Color,
    taskCount: Int,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(heatmapColor)
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, AccentTeal, RoundedCornerShape(4.dp))
                } else {
                    Modifier
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = if (heatmapColor == HeatmapColors[0]) TextSecondary else TextPrimary,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
            )
            // Task dots (max 3)
            if (taskCount > 0) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(top = 1.dp)
                ) {
                    repeat(minOf(taskCount, 3)) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(AccentTeal)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeatmapLegend(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.calendar_legend_less),
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )

        HeatmapColors.forEach { color ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .size(16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }

        Text(
            text = stringResource(R.string.calendar_legend_more),
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun SelectedDateInfo(
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
                        text = "復習タスク",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val completedCount = reviewTasks.count { it.isCompleted }
                    Text(
                        text = "$completedCount / ${reviewTasks.size} 完了",
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
                            contentDescription = "完了",
                            tint = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reviewTask.taskName ?: "タスク",
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

private fun calculateHeatmapLevel(minutes: Int): Int {
    return when {
        minutes == 0 -> 0
        minutes < 30 -> 1
        minutes < 60 -> 2
        minutes < 120 -> 3
        else -> 4
    }
}

private fun calculateTaskHeatmapLevel(taskCount: Int): Int {
    return when {
        taskCount == 0 -> 0
        taskCount == 1 -> 1
        taskCount == 2 -> 2
        taskCount <= 4 -> 3
        else -> 4
    }
}

// 無料版用：グレースケールのシンプルなカレンダーグリッド
@Composable
private fun CalendarGridSimple(
    yearMonth: YearMonth,
    dailyStats: Map<LocalDate, DailyStats>,
    taskCountByDate: Map<LocalDate, Int>,
    selectedDate: LocalDate?,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    val lastDayOfMonth = yearMonth.atEndOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7

    val days = mutableListOf<LocalDate?>()
    repeat(firstDayOfWeek) { days.add(null) }

    var currentDay = firstDayOfMonth
    while (!currentDay.isAfter(lastDayOfMonth)) {
        days.add(currentDay)
        currentDay = currentDay.plusDays(1)
    }

    val weeks = days.chunked(7)

    Column(modifier = modifier) {
        weeks.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                week.forEach { date ->
                    if (date != null) {
                        val stats = dailyStats[date]
                        val hasStudied = stats?.hasStudied == true
                        val taskCount = taskCountByDate[date] ?: 0

                        DayCellSimple(
                            date = date,
                            hasStudied = hasStudied,
                            taskCount = taskCount,
                            isSelected = date == selectedDate,
                            isToday = date == LocalDate.now(),
                            onClick = { onDateClick(date) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
                repeat(7 - week.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DayCellSimple(
    date: LocalDate,
    hasStudied: Boolean,
    taskCount: Int,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (hasStudied) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        Color.Transparent
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, AccentTeal, RoundedCornerShape(4.dp))
                } else {
                    Modifier
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = if (hasStudied) TextPrimary else TextSecondary,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
            )
            // Task dots (max 3)
            if (taskCount > 0) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(top = 1.dp)
                ) {
                    repeat(minOf(taskCount, 3)) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(AccentTeal)
                        )
                    }
                }
            }
        }
    }
}
