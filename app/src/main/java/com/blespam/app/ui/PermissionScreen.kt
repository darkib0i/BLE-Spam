package com.blespam.app.ui

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import com.blespam.app.permission.BlePermissions
import com.blespam.app.ui.components.GlassCard
import com.blespam.app.ui.components.GradientButton
import com.blespam.app.ui.theme.LocalAppTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState

/**
 * A dedicated, friendly permission-request screen. It lists each required permission with a live
 * granted/pending indicator and a single primary button to request them all. Denials are handled
 * gracefully: the screen simply stays until the user grants what's needed or backs out, and it
 * explains *why* each permission is used.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionScreen(
    onAllGranted: () -> Unit,
    onSkip: () -> Unit,
    contentPadding: PaddingValues,
) {
    val permissions = rememberMultiplePermissionsState(BlePermissions.required())

    // Advance automatically the moment the required set is satisfied.
    LaunchedEffect(permissions.allPermissionsGranted) {
        if (permissions.allPermissionsGranted) onAllGranted()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = 20.dp, end = 20.dp,
                top = contentPadding.calculateTopPadding() + 24.dp,
                bottom = contentPadding.calculateBottomPadding() + 24.dp,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        PermissionHero()

        Text(
            text = "Permissions needed",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "BLE Spam needs a few permissions to broadcast and inspect Bluetooth Low Energy " +
                "advertisements. It never uses them to determine your location.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            PermissionRows(permissions)
        }

        GradientButton(
            text = if (permissions.allPermissionsGranted) "Continue" else "Grant permissions",
            onClick = {
                if (permissions.allPermissionsGranted) onAllGranted()
                else permissions.launchMultiplePermissionRequest()
            },
            modifier = Modifier.fillMaxWidth(),
        )
        GradientButton(
            text = "Not now",
            onClick = onSkip,
            outlined = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun PermissionRows(permissions: MultiplePermissionsState) {
    val descriptions = permissionDescriptions()
    permissions.permissions.forEach { perm ->
        val granted = perm.status.isGrantedCompat()
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (granted) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                val info = descriptions[perm.permission]
                Text(info?.first ?: perm.permission, style = MaterialTheme.typography.titleMedium)
                Text(
                    info?.second ?: "Required by BLE Spam",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PermissionHero() {
    val theme = LocalAppTheme.current
    Box(
        modifier = Modifier
            .size(96.dp)
            .background(theme.accent.copy(alpha = 0.16f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Rounded.Security, contentDescription = null, tint = theme.accent, modifier = Modifier.size(48.dp))
    }
}

@OptIn(ExperimentalPermissionsApi::class)
private fun com.google.accompanist.permissions.PermissionStatus.isGrantedCompat(): Boolean =
    this == com.google.accompanist.permissions.PermissionStatus.Granted

/** Friendly labels and rationales keyed by manifest permission string. */
private fun permissionDescriptions(): Map<String, Pair<String, String>> = mapOf(
    android.Manifest.permission.BLUETOOTH_ADVERTISE to
        ("Advertise" to "Broadcast BLE advertisements"),
    android.Manifest.permission.BLUETOOTH_CONNECT to
        ("Nearby devices" to "Access the Bluetooth adapter name and state"),
    android.Manifest.permission.BLUETOOTH_SCAN to
        ("Scan" to "Discover nearby BLE devices (never for location)"),
    android.Manifest.permission.ACCESS_FINE_LOCATION to
        ("Location" to "Required for BLE on Android 10–11"),
    android.Manifest.permission.POST_NOTIFICATIONS to
        ("Notifications" to "Show the ongoing advertising notification"),
)
