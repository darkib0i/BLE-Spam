package com.blespam.app.permission

import android.Manifest
import android.os.Build

/**
 * Central definition of which runtime permissions the app needs on each OS version. Keeping this
 * in one place means the permission UI, the Home status cards, and the advertising gate all agree.
 */
object BlePermissions {

    /** Permissions required to advertise + the notification permission on Android 13+. */
    fun required(): List<String> = buildList {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_ADVERTISE)
            add(Manifest.permission.BLUETOOTH_CONNECT)
            add(Manifest.permission.BLUETOOTH_SCAN)
        } else {
            // Android 10–11: scanning/advertising is gated behind fine location.
            add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val advertise: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Manifest.permission.BLUETOOTH_ADVERTISE
        } else {
            Manifest.permission.ACCESS_FINE_LOCATION
        }

    val connect: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Manifest.permission.BLUETOOTH_CONNECT
        } else {
            Manifest.permission.ACCESS_FINE_LOCATION
        }

    val scan: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Manifest.permission.BLUETOOTH_SCAN
        } else {
            Manifest.permission.ACCESS_FINE_LOCATION
        }

    val location: String get() = Manifest.permission.ACCESS_FINE_LOCATION

    val notifications: String?
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.POST_NOTIFICATIONS
        } else {
            null
        }
}
