package com.iterio.app.ui.screens.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iterio.app.R
import com.iterio.app.ui.theme.SurfaceVariantDark
import com.iterio.app.ui.theme.Teal700
import com.iterio.app.ui.theme.TextPrimary
import com.iterio.app.ui.theme.TextSecondary

@Composable
internal fun PomodoroSettingsContent(
    workDurationMinutes: Int,
    shortBreakMinutes: Int,
    longBreakMinutes: Int,
    cyclesBeforeLongBreak: Int,
    onWorkDurationChange: (Int) -> Unit,
    onShortBreakChange: (Int) -> Unit,
    onLongBreakChange: (Int) -> Unit,
    onCyclesChange: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        // Work duration
        Text(
            text = stringResource(R.string.settings_work_duration),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { if (workDurationMinutes > 1) onWorkDurationChange(workDurationMinutes - 1) }
            ) {
                Icon(
                    Icons.Default.Remove,
                    contentDescription = stringResource(R.string.decrease_one_minute),
                    tint = Teal700
                )
            }
            Text(
                text = stringResource(R.string.minutes_unit, workDurationMinutes),
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            IconButton(
                onClick = { if (workDurationMinutes < 180) onWorkDurationChange(workDurationMinutes + 1) }
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.increase_one_minute),
                    tint = Teal700
                )
            }
        }
        Slider(
            value = workDurationMinutes.toFloat(),
            onValueChange = { onWorkDurationChange(it.toInt()) },
            valueRange = 1f..180f,
            steps = 178,
            colors = SliderDefaults.colors(
                thumbColor = Teal700,
                activeTrackColor = Teal700,
                inactiveTrackColor = SurfaceVariantDark
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(R.string.minutes_unit, 1), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(stringResource(R.string.minutes_unit, 180), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }

        // Short break
        Text(
            text = stringResource(R.string.settings_short_break),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = stringResource(R.string.minutes_unit, shortBreakMinutes),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Slider(
            value = shortBreakMinutes.toFloat(),
            onValueChange = { onShortBreakChange(it.toInt()) },
            valueRange = 3f..15f,
            steps = 11,
            colors = SliderDefaults.colors(
                thumbColor = Teal700,
                activeTrackColor = Teal700,
                inactiveTrackColor = SurfaceVariantDark
            )
        )

        // Long break
        Text(
            text = stringResource(R.string.settings_long_break),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = stringResource(R.string.minutes_unit, longBreakMinutes),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Slider(
            value = longBreakMinutes.toFloat(),
            onValueChange = { onLongBreakChange(it.toInt()) },
            valueRange = 10f..30f,
            steps = 3,
            colors = SliderDefaults.colors(
                thumbColor = Teal700,
                activeTrackColor = Teal700,
                inactiveTrackColor = SurfaceVariantDark
            )
        )

        // Cycles before long break
        Text(
            text = stringResource(R.string.settings_cycles_before_long_break),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = stringResource(R.string.cycles_unit, cyclesBeforeLongBreak),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Slider(
            value = cyclesBeforeLongBreak.toFloat(),
            onValueChange = { onCyclesChange(it.toInt()) },
            valueRange = 2f..6f,
            steps = 3,
            colors = SliderDefaults.colors(
                thumbColor = Teal700,
                activeTrackColor = Teal700,
                inactiveTrackColor = SurfaceVariantDark
            )
        )
    }
}
