package com.blespam.app.ui.permissions

import android.Manifest
import android.os.Build
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import androidx.compose.runtime.Composable

/**
 * Returns an Accompanist [MultiplePermissionsState] pre-populated with exactly
 * the runtime permissions this Android version needs for BLE advertising and
 * scanning. Centralised so the version branching isn't duplicated in the UI.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberBlePermissionsState(): MultiplePermissionsState {
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        buildList {
            add(Manifest.permission.BLUETOOTH_ADVERTISE)
            add(Manifest.permission.BLUETOOTH_SCAN)
            add(Manifest.permission.BLUETOOTH_CONNECT)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    } else {
        listOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    return rememberMultiplePermissionsState(permissions)
}
