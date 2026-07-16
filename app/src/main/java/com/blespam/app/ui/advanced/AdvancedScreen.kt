package com.blespam.app.ui.advanced

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DeveloperMode
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QueryStats
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.blespam.app.data.local.SavedConfigEntity
import com.blespam.app.data.repository.toConfig
import com.blespam.app.ui.SessionViewModel
import com.blespam.app.ui.components.GlassCard
import com.blespam.app.ui.components.GradientButton
import com.blespam.app.ui.components.Pill
import com.blespam.app.ui.components.SectionHeader
import com.blespam.app.ui.theme.LocalAppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * The Advanced tab aggregates everything a developer needs beyond the main flow: aggregate stats,
 * saved configs, favorites, the full advertising history, export/import, and device/BLE capability
 * reporting. Applying a saved config loads it into the shared session for immediate broadcast.
 */
@Composable
fun AdvancedScreen(
    viewModel: AdvancedViewModel,
    sessionViewModel: SessionViewModel,
    onOpenDeveloperSettings: () -> Unit,
    contentPadding: PaddingValues,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val exportLogsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri -> uri?.let { viewModel.exportLogs(it) {} } }

    val exportConfigsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri -> uri?.let { viewModel.exportConfigs(it) {} } }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> uri?.let { viewModel.importConfigs(it) {} } }

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            SectionHeader(title = "Statistics", subtitle = "All-time totals", modifier = Modifier.fillMaxWidth())
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(Modifier.weight(1f), Icons.Rounded.QueryStats, "Sessions", state.totalSessions.toString())
                StatTile(Modifier.weight(1f), Icons.Rounded.History, "Est. Packets", state.totalPackets.toString())
            }
        }

        item {
            SectionHeader(title = "Import / Export", modifier = Modifier.fillMaxWidth())
        }
        item {
            GlassCard {
                ActionRow(Icons.Rounded.Download, "Export logs", "Save advertising history as JSON") {
                    exportLogsLauncher.launch("ble_spam_logs.json")
                }
                ActionRow(Icons.Rounded.Upload, "Export configurations", "Back up your saved configs") {
                    exportConfigsLauncher.launch("ble_spam_configs.json")
                }
                ActionRow(Icons.Rounded.Bookmark, "Import configurations", "Merge configs from a JSON file") {
                    importLauncher.launch(arrayOf("application/json"))
                }
            }
        }

        if (state.favorites.isNotEmpty()) {
            item { SectionHeader(title = "Favorites", modifier = Modifier.fillMaxWidth()) }
            items(state.favorites, key = { "fav-${it.id}" }) { config ->
                SavedConfigRow(
                    config = config,
                    onApply = { sessionViewModel.applyConfig(config.toConfig()) },
                    onToggleFavorite = { viewModel.toggleFavorite(config) },
                    onDelete = { viewModel.deleteConfig(config) },
                )
            }
        }

        item {
            SectionHeader(
                title = "Saved Configurations",
                subtitle = "${state.savedConfigs.size} saved",
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (state.savedConfigs.isEmpty()) {
            item { EmptyCard("No saved configurations yet. Build one in the Modes tab.") }
        } else {
            items(state.savedConfigs, key = { "cfg-${it.id}" }) { config ->
                SavedConfigRow(
                    config = config,
                    onApply = { sessionViewModel.applyConfig(config.toConfig()) },
                    onToggleFavorite = { viewModel.toggleFavorite(config) },
                    onDelete = { viewModel.deleteConfig(config) },
                )
            }
        }

        item {
            SectionHeader(
                title = "Advertising History",
                subtitle = "Recent sessions",
                modifier = Modifier.fillMaxWidth(),
                trailing = {
                    if (state.history.isNotEmpty()) {
                        Pill(text = "Clear", color = MaterialTheme.colorScheme.error)
                    }
                },
            )
        }
        if (state.history.isEmpty()) {
            item { EmptyCard("No sessions recorded yet.") }
        } else {
            items(state.history, key = { "hist-${it.id}" }) { entry ->
                HistoryRow(
                    modeName = entry.modeName,
                    subtitle = "${formatTimestamp(entry.startedAt)} · ${entry.estimatedPackets} pkts · ${entry.durationMs / 1000}s",
                    succeeded = entry.succeeded,
                )
            }
            item {
                GradientButton(
                    text = "Clear history",
                    onClick = { viewModel.clearHistory() },
                    outlined = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        item {
            SectionHeader(title = "BLE Capability Report", modifier = Modifier.fillMaxWidth())
        }
        item {
            GlassCard {
                CapabilityRow("BLE supported", yesNo(state.environment.bleSupported))
                CapabilityRow("Advertising supported", yesNo(state.environment.advertisingSupported))
                CapabilityRow("Extended advertising", yesNo(state.environment.supportsExtendedAdvertising))
                CapabilityRow("Max advertising data", "${state.environment.maxAdvertisingDataLength} bytes")
                CapabilityRow("Adapter name", state.environment.adapterName)
                CapabilityRow("Bluetooth enabled", yesNo(state.environment.bluetoothEnabled))
            }
        }

        item {
            SectionHeader(title = "Device & Battery", modifier = Modifier.fillMaxWidth())
        }
        item {
            GlassCard {
                CapabilityRow("Manufacturer", android.os.Build.MANUFACTURER)
                CapabilityRow("Model", android.os.Build.MODEL)
                CapabilityRow("Android", "API ${android.os.Build.VERSION.SDK_INT} (${android.os.Build.VERSION.RELEASE})")
                CapabilityRow(
                    "Battery",
                    if (state.environment.batteryLevelPercent >= 0) "${state.environment.batteryLevelPercent}%" else "Unknown",
                )
                CapabilityRow("Charging", yesNo(state.environment.isCharging))
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenDeveloperSettings)) {
                ActionRow(Icons.Rounded.DeveloperMode, "Developer Settings", "Diagnostics and advanced toggles") {
                    onOpenDeveloperSettings()
                }
            }
        }
    }
}

@Composable
private fun StatTile(modifier: Modifier, icon: ImageVector, label: String, value: String) {
    val theme = LocalAppTheme.current
    GlassCard(modifier = modifier) {
        Icon(icon, contentDescription = null, tint = theme.accent, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(10.dp))
        Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ActionRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.size(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SavedConfigRow(
    config: SavedConfigEntity,
    onApply: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(config.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "${config.localName} · ${config.txPower} · ${config.speed}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = if (config.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (config.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable(onClick = onToggleFavorite).padding(6.dp),
            )
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = "Apply",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onApply).padding(6.dp),
            )
            Icon(
                imageVector = Icons.Rounded.Delete,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.clickable(onClick = onDelete).padding(6.dp),
            )
        }
    }
}

@Composable
private fun HistoryRow(modeName: String, subtitle: String, succeeded: Boolean) {
    val theme = LocalAppTheme.current
    GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(10.dp).background(
                    if (succeeded) theme.accent else MaterialTheme.colorScheme.error, CircleShape,
                ),
            )
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(modeName, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun CapabilityRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun EmptyCard(message: String) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun yesNo(value: Boolean): String = if (value) "Yes" else "No"

private fun formatTimestamp(millis: Long): String =
    SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(millis))
