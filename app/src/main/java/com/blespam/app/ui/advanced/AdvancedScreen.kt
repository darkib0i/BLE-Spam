package com.blespam.app.ui.advanced

import android.content.Intent
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
import androidx.compose.material.icons.rounded.BatteryStd
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.blespam.app.domain.model.HistoryRecord
import com.blespam.app.domain.model.SavedConfig
import com.blespam.app.ui.components.EnterAnimation
import com.blespam.app.ui.components.GlassCard
import com.blespam.app.ui.components.GlowingCounter
import com.blespam.app.ui.components.SectionHeader
import com.blespam.app.ui.control.formatElapsed
import com.blespam.app.ui.theme.LocalGlass
import com.blespam.app.ui.theme.StatusOk
import com.blespam.app.ui.theme.StatusWarn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * The Advanced tab: aggregate statistics, saved configurations and favorites,
 * advertising history, JSON export/import (via the system share sheet), and a
 * full device / BLE capability report including battery-optimization status.
 */
@Composable
fun AdvancedScreen(viewModel: AdvancedViewModel = hiltViewModel()) {
    val history by viewModel.history.collectAsStateWithLifecycle()
    val saved by viewModel.savedConfigs.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val totalPackets by viewModel.totalPackets.collectAsStateWithLifecycle()
    val totalSessions by viewModel.totalSessions.collectAsStateWithLifecycle()
    val totalDuration by viewModel.totalDuration.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val report = remember { viewModel.deviceReport() }

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
                Text("Advanced", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }

            Spacer(Modifier.height(20.dp))

            // --- Aggregate statistics ---
            EnterAnimation(delayMillis = 60) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                        GlowingCounter(totalPackets, "TOTAL PACKETS", modifier = Modifier.weight(1f))
                        GlowingCounter(totalSessions.toLong(), "SESSIONS", modifier = Modifier.weight(1f))
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            EnterAnimation(delayMillis = 90) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Total advertising time", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                        Text(formatElapsed(totalDuration), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            // --- Export / Import ---
            Spacer(Modifier.height(24.dp))
            EnterAnimation { SectionHeader("Data", modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) }
            EnterAnimation(delayMillis = 60) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    ActionTile(
                        icon = Icons.Rounded.FileDownload,
                        label = "Export Logs",
                        modifier = Modifier.weight(1f),
                    ) {
                        scope.launch {
                            val json = viewModel.exportConfigsJson()
                            shareText(context, json, "BLE Spam Configurations")
                        }
                    }
                    ActionTile(
                        icon = Icons.Rounded.FileUpload,
                        label = "Import",
                        modifier = Modifier.weight(1f),
                    ) {
                        // Import from clipboard for a dependency-free flow.
                        val clip = androidx.core.content.ContextCompat.getSystemService(
                            context, android.content.ClipboardManager::class.java,
                        )
                        val text = clip?.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString()
                        if (text.isNullOrBlank()) {
                            scope.launch { snackbar.showSnackbar("Copy a configuration JSON to the clipboard first.") }
                        } else {
                            viewModel.importConfigsJson(text)
                        }
                    }
                }
            }

            // --- Favorites ---
            if (favorites.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                EnterAnimation { SectionHeader("Favorites", modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) }
                favorites.forEach { cfg ->
                    SavedConfigRow(cfg, viewModel, Modifier.padding(bottom = 10.dp))
                }
            }

            // --- Saved configurations ---
            Spacer(Modifier.height(24.dp))
            EnterAnimation { SectionHeader("Saved Configurations", modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) }
            if (saved.isEmpty()) {
                EmptyHint("No saved configurations yet. Build one in the Payload Editor.")
            } else {
                saved.forEach { cfg ->
                    SavedConfigRow(cfg, viewModel, Modifier.padding(bottom = 10.dp))
                }
            }

            // --- History ---
            Spacer(Modifier.height(24.dp))
            EnterAnimation {
                SectionHeader(
                    "Advertising History",
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    trailing = {
                        if (history.isNotEmpty()) {
                            IconButton(onClick = viewModel::clearHistory) {
                                Icon(Icons.Rounded.Delete, "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    },
                )
            }
            if (history.isEmpty()) {
                EmptyHint("No sessions recorded yet.")
            } else {
                history.take(20).forEach { record ->
                    HistoryRow(record, Modifier.padding(bottom = 10.dp))
                }
            }

            // --- Device / capability report ---
            Spacer(Modifier.height(24.dp))
            EnterAnimation { SectionHeader("Device Information", modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) }
            EnterAnimation(delayMillis = 60) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        InfoLine("Device", report.model)
                        InfoLine("System", report.androidVersion)
                        InfoLine("Adapter", report.capabilities.adapterName)
                        InfoLine("BLE Advertising", if (report.capabilities.advertisingSupported) "Supported" else "Unsupported")
                        InfoLine("Extended Advertising", yesNo(report.capabilities.extendedAdvertisingSupported))
                        InfoLine("Periodic Advertising", yesNo(report.capabilities.periodicAdvertisingSupported))
                        InfoLine("LE 2M PHY", yesNo(report.capabilities.le2MPhySupported))
                        InfoLine("LE Coded PHY", yesNo(report.capabilities.leCodedPhySupported))
                        InfoLine("Max Adv. Data", "${report.capabilities.maxAdvertisingDataLength} bytes")
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            EnterAnimation(delayMillis = 90) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.BatteryStd,
                            null,
                            tint = if (report.ignoringBatteryOptimizations) StatusWarn else StatusOk,
                        )
                        Column(Modifier.padding(start = 14.dp).weight(1f)) {
                            Text("Battery Optimization", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                if (report.ignoringBatteryOptimizations) {
                                    "Exempt — long sessions won't be dozed."
                                } else {
                                    "Optimized — the OS may pause long background sessions."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(120.dp))
        }

        SnackbarHost(snackbar, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp))
    }
}

@Composable
private fun ActionTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val glass = LocalGlass.current
    GlassCard(modifier = modifier, onClick = onClick) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Icon(icon, null, tint = glass.accent)
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun SavedConfigRow(cfg: SavedConfig, viewModel: AdvancedViewModel, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier.fillMaxWidth(), onClick = { viewModel.loadConfig(cfg) }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(cfg.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(cfg.config.mode.displayName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = { viewModel.toggleFavorite(cfg) }) {
                Icon(
                    if (cfg.isFavorite) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                    "Favorite",
                    tint = if (cfg.isFavorite) StatusWarn else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = { viewModel.delete(cfg) }) {
                Icon(Icons.Rounded.Delete, "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun HistoryRow(record: HistoryRecord, modifier: Modifier = Modifier) {
    val fmt = remember { SimpleDateFormat("MMM d · HH:mm", Locale.getDefault()) }
    GlassCard(modifier = modifier.fillMaxWidth(), contentPadding = 16.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Bolt, null, tint = LocalGlass.current.accent)
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text(record.mode.displayName, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "${fmt.format(Date(record.startedAt))} · ${formatElapsed(record.durationMillis)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text("${record.packetsSent} pkts", style = MaterialTheme.typography.labelLarge, color = LocalGlass.current.accent)
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun EmptyHint(text: String) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun yesNo(value: Boolean) = if (value) "Yes" else "No"

private fun shareText(context: android.content.Context, text: String, subject: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, subject))
}
