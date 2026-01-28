package com.iterio.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.iterio.app.R
import com.iterio.app.service.TimerPhase
import com.iterio.app.ui.MainActivity
import com.iterio.app.widget.actions.StopTimerActionCallback
import com.iterio.app.widget.actions.ToggleTimerActionCallback

class IterioWidget : GlanceAppWidget() {

    companion object {
        private val COMPACT = DpSize(110.dp, 40.dp)
        private val MEDIUM = DpSize(110.dp, 110.dp)
        private val LARGE = DpSize(110.dp, 180.dp)
    }

    override val sizeMode = SizeMode.Responsive(
        setOf(COMPACT, MEDIUM, LARGE)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = IterioWidgetStateHelper.getWidgetState(context)

        provideContent {
            GlanceTheme {
                val size = LocalSize.current
                when {
                    size.height >= LARGE.height -> IterioWidgetLarge(context = context, state = state)
                    size.height >= MEDIUM.height -> IterioWidgetMedium(context = context, state = state)
                    else -> IterioWidgetCompact(context = context, state = state)
                }
            }
        }
    }
}

// --- Compact Layout (2x1): Study time + timer status only ---

@Composable
private fun IterioWidgetCompact(context: Context, state: WidgetState) {
    val backgroundColor = ColorProvider(R.color.widget_background)
    val primaryTextColor = ColorProvider(R.color.widget_text_primary)
    val accentColor = ColorProvider(R.color.teal_700)

    val launchIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(backgroundColor)
            .cornerRadius(16.dp)
            .clickable(actionStartActivity(launchIntent))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        if (state.isPremium) {
            Row(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Text(
                    text = "Iterio",
                    style = TextStyle(
                        color = accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                Text(
                    text = formatStudyTime(state.todayStudyMinutes),
                    style = TextStyle(
                        color = primaryTextColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                if (state.isTimerRunning || state.timerPhase != TimerPhase.IDLE) {
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    TimerStatusRow(state)
                }
            }
        } else {
            FreePremiumPrompt(accentColor, primaryTextColor)
        }
    }
}

// --- Medium Layout (2x2): Current display + review badge ---

@Composable
private fun IterioWidgetMedium(context: Context, state: WidgetState) {
    val backgroundColor = ColorProvider(R.color.widget_background)
    val primaryTextColor = ColorProvider(R.color.widget_text_primary)
    val secondaryTextColor = ColorProvider(R.color.widget_text_secondary)
    val accentColor = ColorProvider(R.color.teal_700)

    val launchIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(backgroundColor)
            .cornerRadius(16.dp)
            .clickable(actionStartActivity(launchIntent))
            .padding(16.dp)
    ) {
        if (state.isPremium) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                // App title
                Text(
                    text = "Iterio",
                    style = TextStyle(
                        color = accentColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = GlanceModifier.height(8.dp))

                // Study time
                Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
                    Text(
                        text = formatStudyTime(state.todayStudyMinutes),
                        style = TextStyle(
                            color = primaryTextColor,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Text(
                        text = "今日",
                        style = TextStyle(
                            color = secondaryTextColor,
                            fontSize = 12.sp
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(4.dp))

                // Streak
                Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
                    Text(
                        text = "${state.currentStreak}日連続",
                        style = TextStyle(
                            color = secondaryTextColor,
                            fontSize = 12.sp
                        )
                    )
                }

                // Review badge
                if (state.pendingReviewCount > 0) {
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
                        Text(
                            text = context.getString(R.string.widget_review_count, state.pendingReviewCount),
                            style = TextStyle(
                                color = accentColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                // Timer status (if running)
                if (state.isTimerRunning || state.timerPhase != TimerPhase.IDLE) {
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    TimerStatusRow(state)
                }
            }
        } else {
            FreePremiumPrompt(accentColor, primaryTextColor)
        }
    }
}

// --- Large Layout (2x3+): Medium + task list (max 3 items) ---

@Composable
private fun IterioWidgetLarge(context: Context, state: WidgetState) {
    val backgroundColor = ColorProvider(R.color.widget_background)
    val primaryTextColor = ColorProvider(R.color.widget_text_primary)
    val secondaryTextColor = ColorProvider(R.color.widget_text_secondary)
    val accentColor = ColorProvider(R.color.teal_700)

    val launchIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(backgroundColor)
            .cornerRadius(16.dp)
            .clickable(actionStartActivity(launchIntent))
            .padding(16.dp)
    ) {
        if (state.isPremium) {
            Column(
                modifier = GlanceModifier.fillMaxSize()
            ) {
                // App title
                Text(
                    text = "Iterio",
                    style = TextStyle(
                        color = accentColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = GlanceModifier.height(8.dp))

                // Study time
                Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
                    Text(
                        text = formatStudyTime(state.todayStudyMinutes),
                        style = TextStyle(
                            color = primaryTextColor,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Text(
                        text = "今日",
                        style = TextStyle(
                            color = secondaryTextColor,
                            fontSize = 12.sp
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(4.dp))

                // Streak + Review badge row
                Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
                    Text(
                        text = "${state.currentStreak}日連続",
                        style = TextStyle(
                            color = secondaryTextColor,
                            fontSize = 12.sp
                        )
                    )
                    if (state.pendingReviewCount > 0) {
                        Spacer(modifier = GlanceModifier.width(12.dp))
                        Text(
                            text = context.getString(R.string.widget_review_count, state.pendingReviewCount),
                            style = TextStyle(
                                color = accentColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                // Timer status (if running)
                if (state.isTimerRunning || state.timerPhase != TimerPhase.IDLE) {
                    Spacer(modifier = GlanceModifier.height(6.dp))
                    TimerStatusRow(state)
                }

                // Task list (max 3 items displayed)
                if (state.todayTasks.isNotEmpty()) {
                    Spacer(modifier = GlanceModifier.height(8.dp))

                    Text(
                        text = context.getString(R.string.widget_today_tasks),
                        style = TextStyle(
                            color = secondaryTextColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    Spacer(modifier = GlanceModifier.height(4.dp))

                    val displayTasks = state.todayTasks.take(MAX_DISPLAY_TASKS)
                    Column(modifier = GlanceModifier.fillMaxWidth()) {
                        displayTasks.forEach { task ->
                            TaskItemRow(task, primaryTextColor, secondaryTextColor)
                        }
                    }

                    // Show remaining count
                    val remaining = state.todayTasks.size - MAX_DISPLAY_TASKS
                    if (remaining > 0) {
                        Text(
                            text = context.getString(R.string.widget_more_tasks, remaining),
                            style = TextStyle(
                                color = secondaryTextColor,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        } else {
            FreePremiumPrompt(accentColor, primaryTextColor)
        }
    }
}

// --- Shared components ---

@Composable
private fun FreePremiumPrompt(
    accentColor: ColorProvider,
    primaryTextColor: ColorProvider
) {
    val secondaryTextColor = ColorProvider(R.color.widget_text_secondary)
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Text(
            text = "Iterio",
            style = TextStyle(
                color = accentColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = GlanceModifier.height(8.dp))

        Text(
            text = "Premium機能",
            style = TextStyle(
                color = primaryTextColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = GlanceModifier.height(4.dp))

        Text(
            text = "タップしてアップグレード",
            style = TextStyle(
                color = secondaryTextColor,
                fontSize = 12.sp
            )
        )
    }
}

@Composable
private fun TimerStatusRow(state: WidgetState) {
    val statusText = when (state.timerPhase) {
        TimerPhase.WORK -> "作業中"
        TimerPhase.SHORT_BREAK -> "休憩中"
        TimerPhase.LONG_BREAK -> "長休憩"
        TimerPhase.IDLE -> ""
    }

    val statusColor = when (state.timerPhase) {
        TimerPhase.WORK -> ColorProvider(R.color.timer_work)
        TimerPhase.SHORT_BREAK, TimerPhase.LONG_BREAK -> ColorProvider(R.color.timer_break)
        TimerPhase.IDLE -> ColorProvider(R.color.widget_text_secondary)
    }

    if (statusText.isNotEmpty()) {
        Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
            Box(
                modifier = GlanceModifier
                    .size(8.dp)
                    .cornerRadius(4.dp)
                    .background(statusColor)
            ) {}
            Spacer(modifier = GlanceModifier.width(6.dp))
            Text(
                text = "$statusText ${formatTime(state.timeRemainingSeconds)}",
                style = TextStyle(
                    color = statusColor,
                    fontSize = 12.sp
                )
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            // Pause/Resume button
            Box(
                modifier = GlanceModifier
                    .size(24.dp)
                    .cornerRadius(12.dp)
                    .background(statusColor)
                    .clickable(actionRunCallback<ToggleTimerActionCallback>()),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (state.isTimerRunning) "⏸" else "▶",
                    style = TextStyle(fontSize = 10.sp)
                )
            }
            Spacer(modifier = GlanceModifier.width(4.dp))
            // Stop button
            Box(
                modifier = GlanceModifier
                    .size(24.dp)
                    .cornerRadius(12.dp)
                    .background(ColorProvider(R.color.widget_text_secondary))
                    .clickable(actionRunCallback<StopTimerActionCallback>()),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⏹",
                    style = TextStyle(fontSize = 10.sp)
                )
            }
        }
    }
}

@Composable
private fun TaskItemRow(
    task: WidgetTaskItem,
    primaryTextColor: ColorProvider,
    secondaryTextColor: ColorProvider
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(vertical = 1.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        Text(
            text = "・${task.name}",
            style = TextStyle(
                color = primaryTextColor,
                fontSize = 11.sp
            )
        )
        if (task.groupName.isNotEmpty()) {
            Spacer(modifier = GlanceModifier.width(4.dp))
            Text(
                text = task.groupName,
                style = TextStyle(
                    color = secondaryTextColor,
                    fontSize = 10.sp
                )
            )
        }
    }
}

private const val MAX_DISPLAY_TASKS = 3

private fun formatStudyTime(minutes: Int): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return when {
        hours > 0 -> "${hours}h ${mins}m"
        else -> "${mins}m"
    }
}

private fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}
