package com.blespam.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.DeveloperBoard
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Radar
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.blespam.app.R
import com.blespam.app.ui.components.EnterAnimation
import com.blespam.app.ui.components.GlassCard
import com.blespam.app.ui.components.GradientActionButton
import com.blespam.app.ui.components.SectionHeader
import com.blespam.app.ui.components.StatusLevel
import com.blespam.app.ui.components.StatusRow
import com.blespam.app.ui.components.floating
import com.blespam.app.ui.permissions.rememberBlePermissionsState
import com.blespam.app.ui.theme.LocalGlass
import com.google.accompanist.permissions.ExperimentalPermissionsApi

/**
 * The landing screen: hero logo + title, the primary "Start" call-to-action,
 * and a stack of live status cards for Bluetooth, permissions, BLE support,
 * adapter and battery. Requesting permissions is wired to the Start button and
 * the "grant" affordances so denial is always handled gracefully.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    onStart: () -> Unit,
    onOpenAbout: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val battery by viewModel.batteryLevel.collectAsStateWithLifecycle()
    val glass = LocalGlass.current
    val permissions = rememberBlePermissionsState()

    // Re-read capabilities/permissions each time we return to Home.
    androidx.compose.runtime.LaunchedEffect(permissions.allPermissionsGranted) {
        viewModel.refresh()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(48.dp))

        // --- Hero logo with a glowing halo, gently floating ---
        EnterAnimation {
            androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                androidx.compose.foundation.Image(
                    painter = painterResource(R.drawable.ic_ble_logo),
                    contentDescription = "BLE Spam logo",
                    modifier = Modifier
                        .size(140.dp)
                        .blur(36.dp),
                )
                androidx.compose.foundation.Image(
                    painter = painterResource(R.drawable.ic_ble_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(120.dp)
                        .floating(),
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        EnterAnimation(delayMillis = 80) {
            androidx.compose.material3.Text(
                text = stringResource(R.string.app_name),
                fontSize = 46.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialColorOnSurface(),
                textAlign = TextAlign.Center,
            )
        }
        EnterAnimation(delayMillis = 140) {
            androidx.compose.material3.Text(
                text = stringResource(R.string.app_subtitle),
                fontSize = 15.sp,
                color = glass.accent,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        Spacer(Modifier.height(32.dp))

        // --- Primary Start CTA ---
        EnterAnimation(delayMillis = 200) {
            GradientActionButton(
                text = stringResource(R.string.home_start),
                icon = Icons.Rounded.PlayArrow,
                active = state.isAdvertising,
                onClick = {
                    if (permissions.allPermissionsGranted) {
                        onStart()
                    } else {
                        permissions.launchMultiplePermissionRequest()
                    }
                },
                modifier = Modifier.fillMaxWidth(0.8f),
            )
        }

        Spacer(Modifier.height(36.dp))

        // --- Status cards ---
        EnterAnimation(delayMillis = 260) {
            SectionHeader("System Status", modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))
        }

        EnterAnimation(delayMillis = 300) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    StatusRow(
                        icon = Icons.Rounded.Bluetooth,
                        label = stringResource(R.string.status_bluetooth),
                        value = if (state.capabilities.bluetoothEnabled) "On" else "Off",
                        level = if (state.capabilities.bluetoothEnabled) StatusLevel.OK else StatusLevel.ERROR,
                    )
                    StatusRow(
                        icon = Icons.Rounded.LocationOn,
                        label = stringResource(R.string.status_location),
                        value = if (state.permissions.locationGranted) "Granted" else "Not required / Denied",
                        level = if (state.permissions.locationGranted) StatusLevel.OK else StatusLevel.WARN,
                    )
                    StatusRow(
                        icon = Icons.Rounded.Radar,
                        label = stringResource(R.string.status_scan),
                        value = if (state.permissions.scanGranted) "Granted" else "Denied",
                        level = if (state.permissions.scanGranted) StatusLevel.OK else StatusLevel.WARN,
                    )
                    StatusRow(
                        icon = Icons.Rounded.Devices,
                        label = stringResource(R.string.status_nearby),
                        value = if (state.permissions.nearbyDevicesGranted) "Granted" else "Denied",
                        level = if (state.permissions.nearbyDevicesGranted) StatusLevel.OK else StatusLevel.WARN,
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        EnterAnimation(delayMillis = 340) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    StatusRow(
                        icon = Icons.Rounded.Verified,
                        label = stringResource(R.string.status_ble_support),
                        value = when {
                            !state.capabilities.bleSupported -> "Unsupported"
                            state.capabilities.advertisingSupported -> "Advertise + Scan"
                            else -> "Scan only"
                        },
                        level = when {
                            !state.capabilities.bleSupported -> StatusLevel.ERROR
                            state.capabilities.advertisingSupported -> StatusLevel.OK
                            else -> StatusLevel.WARN
                        },
                    )
                    StatusRow(
                        icon = Icons.Rounded.DeveloperBoard,
                        label = stringResource(R.string.status_adapter),
                        value = state.capabilities.adapterName,
                        level = StatusLevel.NEUTRAL,
                    )
                    StatusRow(
                        icon = Icons.Rounded.BatteryFull,
                        label = stringResource(R.string.status_battery),
                        value = if (battery >= 0) "$battery%${if (state.isCharging) " · Charging" else ""}" else "Unknown",
                        level = when {
                            battery < 0 -> StatusLevel.NEUTRAL
                            battery < 15 -> StatusLevel.ERROR
                            battery < 30 -> StatusLevel.WARN
                            else -> StatusLevel.OK
                        },
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        EnterAnimation(delayMillis = 380) {
            GlassCard(modifier = Modifier.fillMaxWidth(), onClick = onOpenAbout) {
                Column {
                    androidx.compose.material3.Text(
                        text = stringResource(R.string.responsible_use_title),
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialColorOnSurface(),
                    )
                    Spacer(Modifier.height(6.dp))
                    androidx.compose.material3.Text(
                        text = stringResource(R.string.responsible_use_body),
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(Modifier.height(120.dp))
    }
}

@Composable
private fun MaterialColorOnSurface(): Color =
    androidx.compose.material3.MaterialTheme.colorScheme.onSurface
