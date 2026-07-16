package com.blespam.app.ui.settings

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.blespam.app.data.preferences.AccentColor
import com.blespam.app.data.preferences.AnimationSpeed
import com.blespam.app.data.preferences.ThemeMode
import com.blespam.app.ui.components.GlassCard
import com.blespam.app.ui.components.SectionHeader
import com.blespam.app.ui.modes.SegmentedRow
import com.blespam.app.ui.theme.LocalAppTheme

/**
 * All app preferences: theme + AMOLED, accent color, dynamic (Material You) color, animation speed,
 * battery saver, notifications, developer mode, and an entry to the About screen. Every change is
 * persisted through [SettingsViewModel] and takes effect app-wide immediately.
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onOpenAbout: () -> Unit,
    contentPadding: PaddingValues,
) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { SectionHeader(title = "Appearance", modifier = Modifier.fillMaxWidth()) }
        item {
            GlassCard {
                Text("Theme", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                SegmentedRow(
                    options = ThemeMode.entries.map { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
                    selectedIndex = ThemeMode.entries.indexOf(prefs.themeMode),
                    onSelected = { viewModel.setTheme(ThemeMode.entries[it]) },
                )
                Spacer(Modifier.height(16.dp))
                Text("Accent color", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                AccentPicker(
                    selected = prefs.accentColor,
                    onSelected = viewModel::setAccent,
                    enabled = !prefs.useDynamicColor,
                )
                Spacer(Modifier.height(8.dp))
                SettingToggle(
                    title = "Dynamic color (Material You)",
                    subtitle = "Follow the system wallpaper palette on Android 12+",
                    checked = prefs.useDynamicColor,
                    onCheckedChange = viewModel::setDynamicColor,
                )
            }
        }

        item { SectionHeader(title = "Motion", modifier = Modifier.fillMaxWidth()) }
        item {
            GlassCard {
                Text("Animation speed", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                SegmentedRow(
                    options = AnimationSpeed.entries.map { it.displayName },
                    selectedIndex = AnimationSpeed.entries.indexOf(prefs.animationSpeed),
                    onSelected = { viewModel.setAnimationSpeed(AnimationSpeed.entries[it]) },
                )
            }
        }

        item { SectionHeader(title = "System", modifier = Modifier.fillMaxWidth()) }
        item {
            GlassCard {
                SettingToggle(
                    title = "Battery saver",
                    subtitle = "Reduce motion and default to low-power advertising",
                    checked = prefs.batterySaver,
                    onCheckedChange = viewModel::setBatterySaver,
                )
                SettingToggle(
                    title = "Notifications",
                    subtitle = "Show the ongoing advertising notification",
                    checked = prefs.notificationsEnabled,
                    onCheckedChange = viewModel::setNotifications,
                )
                SettingToggle(
                    title = "Keep screen on while broadcasting",
                    subtitle = "Prevent the display from sleeping during a session",
                    checked = prefs.keepScreenOn,
                    onCheckedChange = viewModel::setKeepScreenOn,
                )
                SettingToggle(
                    title = "Developer options",
                    subtitle = "Expose diagnostics and raw payload tools",
                    checked = prefs.developerMode,
                    onCheckedChange = viewModel::setDeveloperMode,
                )
            }
        }

        item { SectionHeader(title = "About", modifier = Modifier.fillMaxWidth()) }
        item {
            GlassCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenAbout)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.size(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("About BLE Spam", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Version, license, and open-source libraries",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AccentPicker(
    selected: AccentColor,
    onSelected: (AccentColor) -> Unit,
    enabled: Boolean,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AccentColor.entries.forEach { color ->
            val isSelected = color == selected && enabled
            val ringSize by animateDpAsState(if (isSelected) 3.dp else 0.dp, label = "ring")
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .border(ringSize, MaterialTheme.colorScheme.onBackground, CircleShape)
                    .padding(4.dp)
                    .background(Color(color.seedArgb).copy(alpha = if (enabled) 1f else 0.4f), CircleShape)
                    .clickable(enabled = enabled) { onSelected(color) },
            )
        }
    }
}

@Composable
private fun SettingToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.size(12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
