package com.iterio.app.ui.screens.stats

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iterio.app.R
import com.iterio.app.domain.model.PremiumFeature
import com.iterio.app.ui.components.BlurredPremiumContent
import com.iterio.app.ui.components.LoadingIndicator
import com.iterio.app.ui.components.IterioCard
import com.iterio.app.ui.components.IterioTopBar
import com.iterio.app.ui.premium.PremiumUpsellDialog
import com.iterio.app.ui.theme.AccentTeal
import com.iterio.app.ui.theme.AccentWarning
import com.iterio.app.ui.theme.BackgroundDark
import com.iterio.app.ui.theme.Teal700
import com.iterio.app.ui.theme.TextPrimary
import com.iterio.app.ui.theme.TextSecondary

@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val subscriptionStatus by viewModel.subscriptionStatus.collectAsStateWithLifecycle()
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()

    var showPremiumUpsellDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            IterioTopBar(title = stringResource(R.string.stats_title))
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingIndicator()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Today's Study Time Card (無料版でも表示・大きめ)
                TodayStudyCard(
                    minutes = uiState.todayMinutes,
                    sessions = uiState.todaySessions
                )

                // Streak Card (無料版でも表示)
                StatCard(
                    title = stringResource(R.string.stats_streak),
                    icon = Icons.Default.LocalFireDepartment,
                    iconTint = AccentWarning
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatValue(
                            value = stringResource(R.string.stats_streak_days, uiState.currentStreak),
                            label = stringResource(R.string.stats_current_streak)
                        )
                        StatValue(
                            value = stringResource(R.string.stats_streak_days, uiState.maxStreak),
                            label = stringResource(R.string.stats_max_streak)
                        )
                    }
                }

                // Premium Stats with Blur Effect
                BlurredPremiumContent(
                    isPremium = isPremium,
                    onPremiumClick = { showPremiumUpsellDialog = true }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // 学習時間統計
                        StatCard(
                            title = stringResource(R.string.stats_study_time),
                            icon = Icons.Default.Schedule,
                            iconTint = AccentTeal
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatValue(
                                    value = formatMinutes(uiState.thisWeekMinutes),
                                    label = stringResource(R.string.stats_this_week)
                                )
                                StatValue(
                                    value = formatMinutes(uiState.thisMonthMinutes),
                                    label = stringResource(R.string.stats_this_month)
                                )
                                StatValue(
                                    value = formatMinutes(uiState.averageDailyMinutes),
                                    label = stringResource(R.string.stats_daily_average)
                                )
                            }
                        }

                        // Sessions Card
                        StatCard(
                            title = stringResource(R.string.stats_sessions),
                            icon = Icons.Default.TrendingUp,
                            iconTint = Teal700
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                StatValue(
                                    value = "${uiState.totalSessions}",
                                    label = stringResource(R.string.stats_total_sessions)
                                )
                            }
                        }

                        // Weekly Chart
                        WeeklyChart(weeklyData = uiState.weeklyData)
                    }
                }
            }
        }
    }

    // Premium誘導ダイアログ
    if (showPremiumUpsellDialog) {
        PremiumUpsellDialog(
            feature = PremiumFeature.DETAILED_STATS,
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
private fun TodayStudyCard(
    minutes: Int,
    sessions: Int,
    modifier: Modifier = Modifier
) {
    IterioCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                tint = AccentTeal,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.stats_today_study_time),
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatMinutes(minutes),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = AccentTeal
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.stats_sessions_completed, sessions),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    IterioCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            content()
        }
    }
}

@Composable
private fun StatValue(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = AccentTeal
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun WeeklyChart(
    weeklyData: List<DayStats>,
    modifier: Modifier = Modifier
) {
    IterioCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.stats_weekly_chart),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val maxMinutes = weeklyData.maxOfOrNull { it.minutes }?.coerceAtLeast(60) ?: 60

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyData.forEach { dayData ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Minutes label
                        if (dayData.minutes > 0) {
                            Text(
                                text = "${dayData.minutes}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        // Bar
                        val barHeight = if (maxMinutes > 0) {
                            (dayData.minutes.toFloat() / maxMinutes * 100).coerceIn(4f, 100f)
                        } else {
                            4f
                        }

                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(if (dayData.minutes > 0) barHeight.dp else 4.dp)
                                .background(
                                    color = if (dayData.minutes > 0) AccentTeal else TextSecondary.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Day label
                        Text(
                            text = dayData.dayOfWeek,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

private fun formatMinutes(minutes: Int): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return when {
        hours > 0 && mins > 0 -> "${hours}h ${mins}m"
        hours > 0 -> "${hours}h"
        else -> "${mins}m"
    }
}
