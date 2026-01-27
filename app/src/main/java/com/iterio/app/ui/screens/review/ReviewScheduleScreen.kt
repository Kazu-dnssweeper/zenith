package com.iterio.app.ui.screens.review

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iterio.app.R
import com.iterio.app.domain.model.ReviewTask
import com.iterio.app.ui.components.EmptySectionMessage
import com.iterio.app.ui.components.IterioCard
import com.iterio.app.ui.components.LoadingIndicator
import com.iterio.app.ui.theme.AccentError
import com.iterio.app.ui.theme.AccentSuccess
import com.iterio.app.ui.theme.AccentTeal
import com.iterio.app.ui.theme.AccentWarning
import com.iterio.app.ui.theme.BackgroundDark
import com.iterio.app.ui.theme.TextPrimary
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScheduleScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReviewScheduleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.review_schedule_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.cancel)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingIndicator()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Summary Stats
                ReviewSummaryRow(uiState = uiState)

                // Filter Chips
                ReviewFilterRow(
                    selectedFilter = uiState.selectedFilter,
                    onFilterSelected = viewModel::updateFilter
                )

                // Task List
                if (uiState.filteredTasks.isEmpty()) {
                    EmptySectionMessage(
                        icon = Icons.Default.EventRepeat,
                        message = stringResource(R.string.review_schedule_empty),
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    ReviewTaskList(
                        tasksByDate = uiState.tasksByDate,
                        today = uiState.today,
                        onToggleCompletion = viewModel::toggleTaskCompletion,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewSummaryRow(uiState: ReviewScheduleUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryChip(
            label = stringResource(R.string.review_schedule_total, uiState.totalCount),
            color = AccentTeal,
            modifier = Modifier.weight(1f)
        )
        SummaryChip(
            label = stringResource(R.string.review_schedule_pending_count, uiState.pendingCount),
            color = AccentWarning,
            modifier = Modifier.weight(1f)
        )
        SummaryChip(
            label = stringResource(R.string.review_schedule_overdue_count, uiState.overdueCount),
            color = AccentError,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryChip(
    label: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    IterioCard(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewFilterRow(
    selectedFilter: ReviewFilter,
    onFilterSelected: (ReviewFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val filters = listOf(
            ReviewFilter.ALL to R.string.review_schedule_filter_all,
            ReviewFilter.PENDING to R.string.review_schedule_filter_pending,
            ReviewFilter.COMPLETED to R.string.review_schedule_filter_completed,
            ReviewFilter.OVERDUE to R.string.review_schedule_filter_overdue
        )

        filters.forEach { (filter, labelResId) ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        text = stringResource(labelResId),
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentTeal.copy(alpha = 0.2f),
                    selectedLabelColor = AccentTeal
                )
            )
        }
    }
}

@Composable
private fun ReviewTaskList(
    tasksByDate: Map<LocalDate, List<ReviewTask>>,
    today: LocalDate,
    onToggleCompletion: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("M/d (E)")

    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        tasksByDate.forEach { (date, tasks) ->
            item(key = "header_$date") {
                DateHeader(
                    date = date,
                    today = today,
                    dateFormatter = dateFormatter
                )
            }

            items(
                items = tasks,
                key = { it.id }
            ) { task ->
                ReviewScheduleTaskItem(
                    reviewTask = task,
                    onToggleCompletion = { onToggleCompletion(task.id) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun DateHeader(
    date: LocalDate,
    today: LocalDate,
    dateFormatter: DateTimeFormatter
) {
    val label = when {
        date == today -> stringResource(R.string.review_schedule_today)
        date.isBefore(today) -> stringResource(R.string.review_schedule_overdue)
        else -> stringResource(R.string.review_schedule_upcoming)
    }

    val labelColor = when {
        date == today -> AccentTeal
        date.isBefore(today) -> AccentError
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = date.format(dateFormatter),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = labelColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ReviewScheduleTaskItem(
    reviewTask: ReviewTask,
    onToggleCompletion: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (reviewTask.isCompleted) {
            AccentSuccess.copy(alpha = 0.1f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "backgroundColor"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleCompletion() },
        shape = RoundedCornerShape(12.dp),
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
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            if (reviewTask.isCompleted) AccentSuccess
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (reviewTask.isCompleted) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = reviewTask.taskName ?: "タスク",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (reviewTask.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (reviewTask.isCompleted) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Text(
                        text = reviewTask.reviewLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (reviewTask.isCompleted) {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
