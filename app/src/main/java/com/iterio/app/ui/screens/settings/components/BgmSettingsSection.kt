package com.iterio.app.ui.screens.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iterio.app.R
import com.iterio.app.domain.model.BgmTracks
import com.iterio.app.ui.components.PremiumBadge
import com.iterio.app.ui.theme.SurfaceVariantDark
import com.iterio.app.ui.theme.Teal700
import com.iterio.app.ui.theme.TextPrimary
import com.iterio.app.ui.theme.TextSecondary

@Composable
internal fun BgmSettingsContent(
    selectedTrackId: String?,
    volume: Float,
    autoPlayEnabled: Boolean,
    isPremium: Boolean,
    onTrackSelect: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onAutoPlayToggle: (Boolean) -> Unit,
    onPremiumClick: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        // Track selection row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (isPremium) {
                        onTrackSelect()
                    } else {
                        onPremiumClick()
                    }
                }
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = if (isPremium) Teal700 else TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
                Column(modifier = Modifier.alpha(if (isPremium) 1f else 0.6f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.settings_bgm_track),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        if (!isPremium) {
                            PremiumBadge()
                        }
                    }
                    Text(
                        text = selectedTrackId?.let { BgmTracks.getById(it)?.nameJa }
                            ?: stringResource(R.string.settings_bgm_track_none),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = stringResource(R.string.details),
                tint = TextSecondary
            )
        }

        // Volume slider
        if (isPremium && selectedTrackId != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.settings_bgm_volume),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = stringResource(R.string.settings_bgm_volume_value, (volume * 100).toInt()),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.VolumeDown,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = TextSecondary
                )
                Slider(
                    value = volume,
                    onValueChange = onVolumeChange,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = Teal700,
                        activeTrackColor = Teal700,
                        inactiveTrackColor = SurfaceVariantDark
                    )
                )
                Icon(
                    Icons.Default.VolumeUp,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = TextSecondary
                )
            }
        }

        // Auto-play toggle
        if (isPremium) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_bgm_auto_play),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = stringResource(R.string.settings_bgm_auto_play_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Switch(
                    checked = autoPlayEnabled,
                    onCheckedChange = onAutoPlayToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Teal700,
                        checkedTrackColor = Teal700.copy(alpha = 0.5f)
                    )
                )
            }
        }
    }
}
