package com.blespam.app.permission

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.blespam.app.data.model.PermissionState

/** Reads the current grant state of every permission the app cares about into a [PermissionState]. */
fun readPermissionState(context: Context): PermissionState {
    fun granted(permission: String?): Boolean =
        permission == null ||
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    return PermissionState(
        hasAdvertise = granted(BlePermissions.advertise),
        hasScan = granted(BlePermissions.scan),
        hasConnect = granted(BlePermissions.connect),
        hasLocation = granted(BlePermissions.location),
        hasNotifications = granted(BlePermissions.notifications),
    )
}
