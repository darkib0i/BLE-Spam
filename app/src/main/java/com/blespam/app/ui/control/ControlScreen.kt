package com.blespam.app.ui.control

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Tune
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
import com.blespam.app.ui.components.EnterAnimation
import com.blespam.app.ui.components.GlassCard
import com.blespam.app.ui.components.GlowingCounter
import com.blespam.app.ui.components.GradientActionButton
import com.blespam.app.ui.components.LiveAreaChart
import com.blespam.app.ui.components.PulsingDot
import com.blespam.app.ui.components.SectionHeader
import com.blespam.app.ui.theme.LocalGlass
import com.blespam.app.ui.theme.StatusOk

/**
 * The advertising control room: a big Start/Stop action, glowing live counters,
 * a live throughput graph, and a grid of session statistics. Everything updates
 * reactively from the shared [ControlViewModel] session state.
 */
@Composable
fun ControlScreen(
    onOpenModes: () -> Unit,
    viewModel: ControlViewModel = hiltViewModel(),
) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()
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
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(40.dp))

            EnterAnimation {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PulsingDot(if (stats.isAdvertising) StatusOk else glass.accent.copy(alpha = 0.4f), stats.isAdvertising)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = if (stats.isAdvertising) "Advertising" else "Idle",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // --- Big glowing primary counter ---
            EnterAnimation(delayMillis = 60) {
                GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = 28.dp) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        GlowingCounter(
                            value = stats.packetsSent,
                            label = "PACKETS BROADCAST",
                        )
                        Spacer(Modifier.height(20.dp))
                        LiveAreaChart(samples = stats.throughputSamples, height = 120.dp)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Throughput (packets/s)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // --- Start/Stop ---
            EnterAnimation(delayMillis = 120) {
                GradientActionButton(
                    text = if (stats.isAdvertising) "Stop" else "Start",
                    icon = if (stats.isAdvertising) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                    active = stats.isAdvertising,
                    onClick = viewModel::toggle,
                    modifier = Modifier.fillMaxWidth(0.85f),
                )
            }

            Spacer(Modifier.height(32.dp))

            EnterAnimation(delayMillis = 160) {
                SectionHeader(
                    "Live Statistics",
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    trailing = {
                        androidx.compose.material3.TextButton(onClick = onOpenModes) {
                            androidx.compose.material3.Icon(Icons.Rounded.Tune, null, tint = glass.accent, modifier = Modifier.height(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Configure", color = glass.accent)
                        }
                    },
                )
            }

            // --- Stat grid ---
            val elapsed = formatElapsed(stats.elapsedMillis)
            val statItems = listOf(
                StatItem("Sessions", stats.sessions.toString()),
                StatItem("Elapsed", elapsed),
                StatItem("Mode", stats.currentMode.displayName),
                StatItem("Power Level", config.txPower.label),
                StatItem("Interval", config.interval.label),
                StatItem("Battery Usage", "~${"%.1f".format(stats.estimatedBatteryPerHour * 100)}%/h"),
            )

            EnterAnimation(delayMillis = 200) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    statItems.chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            row.forEach { item ->
                                GlassCard(modifier = Modifier.weight(1f), contentPadding = 16.dp) {
                                    Column {
                                        Text(item.label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            item.value,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                    }
                                }
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(120.dp))
        }

        SnackbarHost(snackbar, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp))
    }
}

private data class StatItem(val label: String, val value: String)

/** hh:mm:ss / mm:ss elapsed formatting. */
fun formatElapsed(millis: Long): String {
    val totalSeconds = millis / 1000
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
