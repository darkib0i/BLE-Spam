package com.blespam.app.ui.settings

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.BatterySaver
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.blespam.app.ui.components.EnterAnimation
import com.blespam.app.ui.components.GlassCard
import com.blespam.app.ui.components.SectionHeader
import com.blespam.app.ui.components.SelectableChip
import com.blespam.app.ui.theme.AccentColor
import com.blespam.app.ui.theme.LocalGlass
import com.blespam.app.ui.theme.ThemeMode

/**
 * Settings: theme, accent color, dynamic color, AMOLED, animation speed/toggle,
 * battery saver, notifications, language, developer options and diagnostics.
 * Every change is written to DataStore and reflected app-wide immediately.
 */
@Composable
fun SettingsScreen(
    onOpenAbout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val glass = LocalGlass.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(40.dp))
        EnterAnimation {
            Text("Settings", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }

        // --- Appearance ---
        Spacer(Modifier.height(20.dp))
        EnterAnimation { SectionHeader("Appearance", modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) }

        EnterAnimation(delayMillis = 40) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Contrast, null, tint = glass.accent)
                        Text("  Theme", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ThemeMode.entries.forEach { mode ->
                            SelectableChip(
                                label = mode.name.lowercase().replaceFirstChar { it.uppercase() },
                                selected = settings.themeMode == mode,
                                onClick = { viewModel.setThemeMode(mode) },
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        EnterAnimation(delayMillis = 70) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Palette, null, tint = glass.accent)
                        Text("  Accent Color", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        AccentColor.entries.forEach { accent ->
                            AccentSwatch(
                                color = accent.seed,
                                selected = settings.accentColor == accent && !settings.dynamicColor,
                                onClick = { viewModel.setAccent(accent) },
                            )
                        }
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        ToggleRow(
                            icon = Icons.Rounded.Palette,
                            title = "Dynamic color",
                            subtitle = "Derive colors from your wallpaper (Material You)",
                            checked = settings.dynamicColor,
                            onChange = viewModel::setDynamicColor,
                        )
                    }
                    ToggleRow(
                        icon = Icons.Rounded.Contrast,
                        title = "AMOLED black",
                        subtitle = "Pure-black background in dark mode",
                        checked = settings.amoled,
                        onChange = viewModel::setAmoled,
                    )
                }
            }
        }

        // --- Motion ---
        Spacer(Modifier.height(24.dp))
        EnterAnimation { SectionHeader("Motion", modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) }
        EnterAnimation(delayMillis = 40) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ToggleRow(
                        icon = Icons.Rounded.Animation,
                        title = "Animations",
                        subtitle = "Enable all motion and live effects",
                        checked = settings.animationsEnabled,
                        onChange = viewModel::setAnimationsEnabled,
                    )
                    Text(
                        "Animation speed: ${"%.1f".format(settings.animationSpeed)}×",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Slider(
                        value = settings.animationSpeed,
                        onValueChange = viewModel::setAnimationSpeed,
                        valueRange = 0.4f..2.0f,
                        enabled = settings.animationsEnabled,
                    )
                }
            }
        }

        // --- Power ---
        Spacer(Modifier.height(24.dp))
        EnterAnimation { SectionHeader("Power & Notifications", modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) }
        EnterAnimation(delayMillis = 40) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ToggleRow(
                        icon = Icons.Rounded.BatterySaver,
                        title = "Battery saver",
                        subtitle = "Calmer motion and lower-power defaults",
                        checked = settings.batterySaver,
                        onChange = viewModel::setBatterySaver,
                    )
                    ToggleRow(
                        icon = Icons.Rounded.Notifications,
                        title = "Notifications",
                        subtitle = "Show status while advertising",
                        checked = settings.notificationsEnabled,
                        onChange = viewModel::setNotifications,
                    )
                }
            }
        }

        // --- Developer ---
        Spacer(Modifier.height(24.dp))
        EnterAnimation { SectionHeader("Developer", modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) }
        EnterAnimation(delayMillis = 40) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ToggleRow(
                        icon = Icons.Rounded.Code,
                        title = "Developer options",
                        subtitle = "Extra diagnostics and raw payload details",
                        checked = settings.developerMode,
                        onChange = viewModel::setDeveloperMode,
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        EnterAnimation {
            GlassCard(modifier = Modifier.fillMaxWidth(), onClick = onOpenAbout) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Info, null, tint = glass.accent)
                    Text("  About BLE Spam", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        Spacer(Modifier.height(120.dp))
    }
}

@Composable
private fun ToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
        Column(Modifier.padding(start = 14.dp).weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun AccentSwatch(color: Color, selected: Boolean, onClick: () -> Unit) {
    val glass = LocalGlass.current
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(color)
            .border(2.dp, if (selected) MaterialTheme.colorScheme.onSurface else glass.glassBorder, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
    }
}
