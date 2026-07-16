package com.blespam.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Battery5Bar
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Radar
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.blespam.app.data.model.BleEnvironment
import com.blespam.app.data.model.PermissionState
import com.blespam.app.ui.SessionViewModel
import com.blespam.app.ui.components.PulsingStartButton
import com.blespam.app.ui.components.StatusCard
import com.blespam.app.ui.components.StatusLevel
import com.blespam.app.ui.theme.LocalAppTheme

/**
 * The landing screen: hero logo + title, the big animated Start button, and a grid of live status
 * cards (Bluetooth, permissions, BLE support, adapter, battery). Tapping Start navigates the user
 * to the Broadcast tab once permissions allow advertising.
 */
@Composable
fun HomeScreen(
    viewModel: SessionViewModel,
    onStart: () -> Unit,
    onRequestPermissions: () -> Unit,
    contentPadding: PaddingValues,
) {
    val environment by viewModel.environment.collectAsStateWithLifecycle()
    val permissions by viewModel.permissions.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()

    val canAdvertise = permissions.canAdvertise && environment.bluetoothEnabled && environment.advertisingSupported

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
            HomeHeader(
                isActive = stats.isAdvertising,
                canAdvertise = canAdvertise,
                onStart = {
                    if (canAdvertise) onStart() else onRequestPermissions()
                },
            )
        }

        items(buildStatusItems(environment, permissions)) { statusItem ->
            StatusCard(
                icon = statusItem.icon,
                label = statusItem.label,
                value = statusItem.value,
                level = statusItem.level,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun HomeHeader(
    isActive: Boolean,
    canAdvertise: Boolean,
    onStart: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BleLogo()
        Spacer(Modifier.height(20.dp))
        Text(
            text = "BLE Spam",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Bluetooth Low Energy Testing Suite",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(28.dp))
        PulsingStartButton(
            isActive = isActive,
            enabled = true,
            onClick = onStart,
        )
        Spacer(Modifier.height(14.dp))
        AnimatedVisibility(
            visible = !canAdvertise,
            enter = fadeIn() + slideInVertically(),
        ) {
            Text(
                text = "Grant permissions & enable Bluetooth to begin",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Device Status",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp),
            textAlign = TextAlign.Start,
        )
        Spacer(Modifier.height(4.dp))
    }
}

/** An animated concentric-ring "radar" logo drawn on a Canvas. */
@Composable
private fun BleLogo() {
    val theme = LocalAppTheme.current
    val transition = rememberInfiniteTransition(label = "logo")
    val ring by transition.animateFloat(
        initialValue = 0f,
        targetValue = if (theme.animationScale <= 0f) 0f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween((2600 / theme.animationScale.coerceAtLeast(0.4f)).toInt()),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ring",
    )
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(132.dp)) {
        Canvas(Modifier.size(132.dp)) {
            val maxR = size.minDimension / 2f
            for (i in 0..2) {
                val progress = (ring + i / 3f) % 1f
                drawCircle(
                    color = theme.accent.copy(alpha = (1f - progress) * 0.4f),
                    radius = maxR * progress,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f),
                )
            }
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(theme.accent, theme.accent.copy(alpha = 0.7f)),
                ),
                radius = maxR * 0.34f,
            )
        }
        androidx.compose.material3.Icon(
            imageVector = Icons.Rounded.Bluetooth,
            contentDescription = "BLE",
            tint = Color.White,
            modifier = Modifier.size(40.dp),
        )
    }
}

private data class StatusItem(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String,
    val value: String,
    val level: StatusLevel,
)

private fun buildStatusItems(env: BleEnvironment, perms: PermissionState): List<StatusItem> = listOf(
    StatusItem(
        icon = Icons.Rounded.Bluetooth,
        label = "Bluetooth",
        value = if (env.bluetoothEnabled) "On" else "Off",
        level = if (env.bluetoothEnabled) StatusLevel.OK else StatusLevel.ERROR,
    ),
    StatusItem(
        icon = Icons.Rounded.Radar,
        label = "Advertise",
        value = if (perms.hasAdvertise) "Granted" else "Needed",
        level = if (perms.hasAdvertise) StatusLevel.OK else StatusLevel.WARN,
    ),
    StatusItem(
        icon = Icons.Rounded.Wifi,
        label = "Scan",
        value = if (perms.hasScan) "Granted" else "Needed",
        level = if (perms.hasScan) StatusLevel.OK else StatusLevel.WARN,
    ),
    StatusItem(
        icon = Icons.Rounded.Devices,
        label = "Nearby Devices",
        value = if (perms.hasConnect) "Granted" else "Needed",
        level = if (perms.hasConnect) StatusLevel.OK else StatusLevel.WARN,
    ),
    StatusItem(
        icon = Icons.Rounded.LocationOn,
        label = "Location",
        value = if (perms.hasLocation) "Granted" else "Optional",
        level = if (perms.hasLocation) StatusLevel.OK else StatusLevel.NEUTRAL,
    ),
    StatusItem(
        icon = Icons.Rounded.Notifications,
        label = "Notifications",
        value = if (perms.hasNotifications) "Granted" else "Off",
        level = if (perms.hasNotifications) StatusLevel.OK else StatusLevel.NEUTRAL,
    ),
    StatusItem(
        icon = Icons.Rounded.Memory,
        label = "BLE Support",
        value = if (env.advertisingSupported) "Ready" else if (env.bleSupported) "Limited" else "No",
        level = if (env.advertisingSupported) StatusLevel.OK else StatusLevel.ERROR,
    ),
    StatusItem(
        icon = Icons.Rounded.Devices,
        label = "Adapter",
        value = env.adapterName,
        level = StatusLevel.NEUTRAL,
    ),
    StatusItem(
        icon = Icons.Rounded.Battery5Bar,
        label = "Battery",
        value = if (env.batteryLevelPercent >= 0) "${env.batteryLevelPercent}%${if (env.isCharging) " ⚡" else ""}" else "—",
        level = when {
            env.batteryLevelPercent < 0 -> StatusLevel.NEUTRAL
            env.batteryLevelPercent < 15 -> StatusLevel.ERROR
            env.batteryLevelPercent < 30 -> StatusLevel.WARN
            else -> StatusLevel.OK
        },
    ),
)
