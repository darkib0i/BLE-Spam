package com.blespam.app.ui.modes

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.blespam.app.data.model.AdvertisingMode
import com.blespam.app.ui.SessionViewModel
import com.blespam.app.ui.components.GlassCard
import com.blespam.app.ui.components.Pill
import com.blespam.app.ui.components.SectionHeader
import com.blespam.app.ui.theme.LocalAppTheme

/**
 * Lists every advertising mode as a selectable frosted card. Selecting a mode updates the shared
 * config; tapping "Configure" opens the payload editor. The "*-style" modes carry a TEST badge to
 * make their interoperability-testing purpose explicit.
 */
@Composable
fun ModesScreen(
    viewModel: SessionViewModel,
    onConfigure: () -> Unit,
    contentPadding: PaddingValues,
) {
    val config by viewModel.config.collectAsStateWithLifecycle()

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SectionHeader(
                title = "Advertising Modes",
                subtitle = "Pick a payload shape to broadcast",
                modifier = Modifier.fillMaxWidth(),
            )
        }
        items(AdvertisingMode.entries) { mode ->
            ModeCard(
                mode = mode,
                selected = config.mode == mode,
                onSelect = { viewModel.selectMode(mode) },
                onConfigure = {
                    viewModel.selectMode(mode)
                    onConfigure()
                },
            )
        }
    }
}

@Composable
private fun ModeCard(
    mode: AdvertisingMode,
    selected: Boolean,
    onSelect: () -> Unit,
    onConfigure: () -> Unit,
) {
    val theme = LocalAppTheme.current
    val accent by animateColorAsState(
        targetValue = if (selected) theme.accent else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "modeAccent",
    )
    val isTestFormat = mode in setOf(
        AdvertisingMode.APPLE_NEARBY,
        AdvertisingMode.SAMSUNG_NEARBY,
        AdvertisingMode.GOOGLE_FAST_PAIR,
        AdvertisingMode.MICROSOFT_SWIFT_PAIR,
    )

    GlassCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onSelect),
        highlight = selected,
        contentPadding = 16.dp,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).background(accent.copy(alpha = 0.16f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (selected) Icons.Rounded.CheckCircle else Icons.Rounded.Tune,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = mode.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (isTestFormat) Pill(text = "TEST", color = Color(0xFFFFB23E))
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = mode.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = "Configure",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable(onClick = onConfigure),
            )
        }
    }
}
