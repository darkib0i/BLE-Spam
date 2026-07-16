package com.blespam.app.ui.modes

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.blespam.app.domain.model.AdvInterval
import com.blespam.app.domain.model.TxPower
import com.blespam.app.ui.components.ChipRow
import com.blespam.app.ui.components.EnterAnimation
import com.blespam.app.ui.components.GlassCard
import com.blespam.app.ui.components.SectionHeader
import com.blespam.app.ui.theme.LocalGlass
import com.blespam.app.ui.theme.StatusWarn

/**
 * Mode picker + quick configuration. Each mode is a selectable frosted card that
 * expands to show its description; below, chip rows configure TX power and
 * interval, and a button opens the full visual payload editor.
 */
@Composable
fun ModesScreen(
    onOpenEditor: () -> Unit,
    viewModel: ModesViewModel = hiltViewModel(),
) {
    val config by viewModel.config.collectAsStateWithLifecycle()
    val glass = LocalGlass.current
    val snackbar = remember { SnackbarHostState() }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.messages.collect { snackbar.showSnackbar(it) }
    }

    androidx.compose.foundation.layout.Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(40.dp))
            EnterAnimation {
                Text(
                    "Advertising Modes",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(Modifier.height(4.dp))
            EnterAnimation(delayMillis = 60) {
                Text(
                    "Pick a payload structure to test with.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(20.dp))

            viewModel.modes.forEachIndexed { index, mode ->
                val selected = config.mode == mode
                EnterAnimation(delayMillis = 80 + index * 25) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .animateContentSize(),
                        onClick = { viewModel.selectMode(mode) },
                        glowColor = if (selected) glass.accent else null,
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    mode.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (selected) glass.accent else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f),
                                )
                                if (selected) {
                                    Icon(Icons.Rounded.Edit, null, tint = glass.accent, modifier = Modifier.height(20.dp))
                                }
                            }
                            if (selected) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    mode.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                if (mode.requiresParserDisclaimer) {
                                    Spacer(Modifier.height(10.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.Info, null, tint = StatusWarn, modifier = Modifier.height(18.dp))
                                        Spacer(Modifier.height(0.dp))
                                        Text(
                                            "  Structure only, for parser interoperability testing.",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = StatusWarn,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            EnterAnimation {
                SectionHeader("Radio Settings", modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))
            }

            EnterAnimation(delayMillis = 60) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("TX Power", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        ChipRow(
                            options = TxPower.entries,
                            selected = config.txPower,
                            label = { it.label },
                            onSelect = viewModel::setTxPower,
                        )
                        Text("Advertising Interval", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        ChipRow(
                            options = AdvInterval.entries,
                            selected = config.interval,
                            label = { it.label },
                            onSelect = viewModel::setInterval,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            EnterAnimation(delayMillis = 80) {
                GlassCard(modifier = Modifier.fillMaxWidth(), onClick = onOpenEditor, glowColor = glass.accent) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Edit, null, tint = glass.accent)
                        Spacer(Modifier.height(0.dp))
                        Column(Modifier.padding(start = 14.dp).weight(1f)) {
                            Text("Visual Payload Editor", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Build a custom payload with live hex preview", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(Modifier.height(120.dp))
        }

        SnackbarHost(snackbar, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp))
    }
}
