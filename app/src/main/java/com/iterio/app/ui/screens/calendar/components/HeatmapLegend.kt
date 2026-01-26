package com.iterio.app.ui.screens.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iterio.app.R
import com.iterio.app.ui.theme.HeatmapColors
import com.iterio.app.ui.theme.TextSecondary

@Composable
fun HeatmapLegend(modifier: Modifier = Modifier) {
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
