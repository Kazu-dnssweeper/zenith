package com.iterio.app.ui.screens.settings.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iterio.app.R
import com.iterio.app.domain.model.ReviewTask
import com.iterio.app.ui.theme.SurfaceDark
import com.iterio.app.ui.theme.SurfaceVariantDark
import com.iterio.app.ui.theme.Teal700
import com.iterio.app.ui.theme.TextPrimary
import com.iterio.app.ui.theme.TextSecondary
import java.time.format.DateTimeFormatter

@Composable
internal fun ReviewTaskManagementSection(
    totalCount: Int,
    incompleteCount: Int,
    onShowList: () -> Unit,
    onDeleteAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Task count display
        Text(
            text = stringResource(
                R.string.settings_review_task_count,
                totalCount,
                incompleteCount
            ),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onShowList,
                modifier = Modifier.weight(1f),
                enabled = totalCount > 0
            ) {
                Icon(
                    Icons.Default.List,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.settings_review_task_show_list))
            }

            OutlinedButton(
                onClick = onDeleteAll,
                modifier = Modifier.weight(1f),
                enabled = totalCount > 0,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.settings_review_task_delete_all))
            }
        }
    }
}

@Composable
internal fun ReviewTasksListDialog(
    reviewTasks: List<ReviewTask>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.settings_review_task_list_title),
                color = TextPrimary
            )
        },
        text = {
            if (reviewTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.settings_review_task_empty),
                        color = TextSecondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.height(400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(reviewTasks) { task ->
                        ReviewTaskItem(task = task)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        },
        containerColor = SurfaceDark
    )
}

@Composable
internal fun ReviewTaskItem(
    task: ReviewTask,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("M/d")

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = SurfaceVariantDark
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (task.isCompleted) Teal700 else TextSecondary,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.taskName ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                if (task.groupName != null) {
                    Text(
                        text = task.groupName,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(
                            R.string.settings_review_task_scheduled,
                            task.scheduledDate.format(dateFormatter)
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = stringResource(
                            R.string.settings_review_task_review_number,
                            task.reviewNumber
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = Teal700
                    )
                }
            }

            Text(
                text = if (task.isCompleted) {
                    stringResource(R.string.settings_review_task_completed)
                } else {
                    stringResource(R.string.settings_review_task_pending)
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (task.isCompleted) Teal700 else TextSecondary
            )
        }
    }
}
