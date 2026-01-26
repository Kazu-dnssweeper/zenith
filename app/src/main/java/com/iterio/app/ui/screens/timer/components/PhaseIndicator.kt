package com.iterio.app.ui.screens.timer.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iterio.app.service.TimerPhase

@Composable
internal fun PhaseIndicator(
    phase: TimerPhase,
    currentCycle: Int,
    totalCycles: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = when (phase) {
                TimerPhase.WORK -> "作業中"
                TimerPhase.SHORT_BREAK -> "休憩中"
                TimerPhase.LONG_BREAK -> "長休憩中"
                TimerPhase.IDLE -> "準備完了"
            },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = when (phase) {
                TimerPhase.WORK -> MaterialTheme.colorScheme.primary
                TimerPhase.SHORT_BREAK, TimerPhase.LONG_BREAK -> MaterialTheme.colorScheme.tertiary
                TimerPhase.IDLE -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )

        if (phase != TimerPhase.IDLE) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(totalCycles) { index ->
                    val isCompleted = index < currentCycle - 1
                    val isCurrent = index == currentCycle - 1
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .padding(2.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = when {
                                    isCompleted -> Color(0xFF00838F)
                                    isCurrent -> Color(0xFF4DD0E1)
                                    else -> Color.Gray.copy(alpha = 0.3f)
                                }
                            )
                        }
                    }
                }
            }
            Text(
                text = "サイクル $currentCycle / $totalCycles",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
