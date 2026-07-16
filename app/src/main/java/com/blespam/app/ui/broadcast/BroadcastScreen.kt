package com.blespam.app.ui.broadcast

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.blespam.app.data.model.AdvertisingStats
import com.blespam.app.data.model.BleConfig
import com.blespam.app.ui.SessionViewModel
import com.blespam.app.ui.components.GlassCard
import com.blespam.app.ui.components.GlowingCounter
import com.blespam.app.ui.components.LiveGraph
import com.blespam.app.ui.components.Pill
import com.blespam.app.ui.components.PulsingStartButton
import com.blespam.app.ui.components.SectionHeader

/**
 * The control room: a large Start/Stop button, glowing live counters, and a live throughput graph.
 * Everything updates from the shared [SessionViewModel]'s [AdvertisingStats] flow.
 */
@Composable
fun BroadcastScreen(
    viewModel: SessionViewModel,
    onRequestPermissions: () -> Unit,
    contentPadding: PaddingValues,
) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val config by viewModel.config.collectAsStateWithLifecycle()
    val permissions by viewModel.permissions.collectAsStateWithLifecycle()
    val environment by viewModel.environment.collectAsStateWithLifecycle()

    val canAdvertise = permissions.canAdvertise && environment.bluetoothEnabled && environment.advertisingSupported

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = 16.dp, end = 16.dp,
                top = contentPadding.calculateTopPadding() + 8.dp,
                bottom = contentPadding.calculateBottomPadding() + 24.dp,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = if (stats.isAdvertising) "Broadcasting" else "Ready",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Pill(text = config.mode.displayName)

        PulsingStartButton(
            isActive = stats.isAdvertising,
            enabled = true,
            onClick = {
                if (stats.isAdvertising) {
                    viewModel.stop()
                } else if (canAdvertise) {
                    viewModel.start()
                } else {
                    onRequestPermissions()
                }
            },
        )

        stats.lastError?.let { error ->
            GlassCard(highlight = true, contentPadding = 14.dp) {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        LiveStatsGrid(stats, config)

        SectionHeader(
            title = "Live Throughput",
            subtitle = "Estimated packets per second",
            modifier = Modifier.fillMaxWidth(),
        )
        GlassCard(contentPadding = 18.dp) {
            if (stats.throughputSamples.size >= 2) {
                LiveGraph(samples = stats.throughputSamples)
            } else {
                Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "Start broadcasting to see live data",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveStatsGrid(stats: AdvertisingStats, config: BleConfig) {
    // Two hero glowing counters up top.
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        GlowingCounter(value = stats.estimatedPackets, label = "PACKETS SENT")
        GlowingCounter(value = stats.sessionCount.toLong(), label = "SESSIONS")
    }

    Spacer(Modifier.height(4.dp))

    val rows = listOf(
        "Elapsed Time" to formatElapsed(stats.elapsedMs),
        "Current Mode" to config.mode.displayName,
        "Power Level" to "${config.txPower.displayName} (${config.txPower.approxDbm} dBm)",
        "Advertising Interval" to "~${config.speed.approxIntervalMs} ms",
        "Battery Usage" to batteryUsageLabel(stats.batteryUsageFactor),
        "Duration" to if (config.durationMs == 0) "Until stopped" else "${config.durationMs / 1000}s",
    )
    GlassCard(contentPadding = 6.dp) {
        rows.forEachIndexed { index, (label, value) ->
            StatRow(label = label, value = value)
            if (index != rows.lastIndex) {
                androidx.compose.material3.HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

private fun formatElapsed(ms: Long): String {
    val totalSeconds = ms / 1000
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}

private fun batteryUsageLabel(factor: Float): String = when {
    factor < 0.35f -> "Low"
    factor < 0.65f -> "Moderate"
    else -> "High"
}
