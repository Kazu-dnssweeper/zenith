package com.iterio.app.ui.screens.timer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iterio.app.service.TimerPhase

@Composable
internal fun TimerControls(
    phase: TimerPhase,
    isRunning: Boolean,
    isPaused: Boolean,
    focusModeEnabled: Boolean,
    isLockModeActive: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onSkip: () -> Unit,
    onStop: () -> Unit
) {
    val canStop = !isLockModeActive
    val isBreakPhase = phase == TimerPhase.SHORT_BREAK || phase == TimerPhase.LONG_BREAK
    val canSkip = isBreakPhase || !focusModeEnabled

    Row(
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when {
            phase == TimerPhase.IDLE -> {
                // Start button
                FilledIconButton(
                    onClick = onStart,
                    modifier = Modifier.size(80.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "開始",
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            isRunning -> {
                // Stop button (disabled in lock mode)
                OutlinedIconButton(
                    onClick = onStop,
                    modifier = Modifier.size(56.dp),
                    enabled = canStop
                ) {
                    Icon(
                        if (canStop) Icons.Default.Stop else Icons.Default.Lock,
                        contentDescription = if (canStop) "停止" else "完全ロックモード中",
                        tint = if (canStop) LocalContentColor.current else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }

                // Pause button
                FilledIconButton(
                    onClick = onPause,
                    modifier = Modifier.size(80.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Default.Pause,
                        contentDescription = "一時停止",
                        modifier = Modifier.size(48.dp)
                    )
                }

                // Skip button
                if (canSkip) {
                    OutlinedIconButton(
                        onClick = onSkip,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Default.SkipNext, contentDescription = "スキップ")
                    }
                }
            }

            isPaused -> {
                // Stop button (disabled in lock mode)
                OutlinedIconButton(
                    onClick = onStop,
                    modifier = Modifier.size(56.dp),
                    enabled = canStop
                ) {
                    Icon(
                        if (canStop) Icons.Default.Stop else Icons.Default.Lock,
                        contentDescription = if (canStop) "停止" else "完全ロックモード中",
                        tint = if (canStop) LocalContentColor.current else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }

                // Resume button
                FilledIconButton(
                    onClick = onResume,
                    modifier = Modifier.size(80.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "再開",
                        modifier = Modifier.size(48.dp)
                    )
                }

                // Skip button
                if (canSkip) {
                    OutlinedIconButton(
                        onClick = onSkip,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Default.SkipNext, contentDescription = "スキップ")
                    }
                }
            }
        }
    }
}
