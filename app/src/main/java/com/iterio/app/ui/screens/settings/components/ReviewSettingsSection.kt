package com.iterio.app.ui.screens.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.iterio.app.ui.components.PremiumBadge
import com.iterio.app.ui.theme.SurfaceVariantDark
import com.iterio.app.ui.theme.Teal700
import com.iterio.app.ui.theme.TextPrimary
import com.iterio.app.ui.theme.TextSecondary

@Composable
internal fun ReviewSettingsContent(
    reviewIntervalsEnabled: Boolean,
    defaultReviewCount: Int,
    isPremium: Boolean,
    onReviewIntervalsToggle: (Boolean) -> Unit,
    onReviewCountChange: (Int) -> Unit,
    onPremiumClick: () -> Unit
) {
    SettingsSwitchItem(
        title = stringResource(R.string.settings_review_auto),
        description = stringResource(R.string.settings_review_auto_desc),
        checked = reviewIntervalsEnabled,
        onCheckedChange = onReviewIntervalsToggle
    )

    ReviewCountSlider(
        title = stringResource(R.string.settings_default_review_count),
        description = stringResource(R.string.settings_default_review_count_desc),
        selectedCount = defaultReviewCount,
        isPremium = isPremium,
        onCountChange = onReviewCountChange,
        onPremiumClick = onPremiumClick
    )
}

@Composable
internal fun ReviewCountSlider(
    title: String,
    description: String,
    selectedCount: Int,
    isPremium: Boolean,
    onCountChange: (Int) -> Unit,
    onPremiumClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val minCount = 1
    val maxCount = 6
    val freeMaxCount = 2

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp)
        )

        // Current selection display
        Text(
            text = stringResource(R.string.settings_review_count_option, selectedCount),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Slider
        Slider(
            value = selectedCount.toFloat(),
            onValueChange = { newValue ->
                val count = newValue.toInt()
                if (isPremium || count <= freeMaxCount) {
                    onCountChange(count)
                } else {
                    onPremiumClick()
                }
            },
            valueRange = minCount.toFloat()..maxCount.toFloat(),
            steps = maxCount - minCount - 1,
            colors = SliderDefaults.colors(
                thumbColor = Teal700,
                activeTrackColor = Teal700,
                inactiveTrackColor = SurfaceVariantDark
            )
        )

        // Range labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.settings_review_count_min),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Text(
                text = stringResource(R.string.settings_review_count_max),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }

        // Premium hint for non-premium users
        if (!isPremium) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_review_count_premium_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                PremiumBadge()
            }
        }
    }
}
