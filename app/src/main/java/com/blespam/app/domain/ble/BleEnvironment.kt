package com.blespam.app.domain.ble

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.blespam.app.domain.model.BleCapabilities
import com.blespam.app.domain.model.PermissionState
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central, version-aware accessor for Bluetooth state, capabilities and the
 * app's permission status. Everything Bluetooth-related that needs to reason
 * about the platform funnels through here, so the version branching lives in
 * exactly one place.
 */
@Singleton
class BleEnvironment @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager

    val adapter: BluetoothAdapter? get() = bluetoothManager?.adapter

    fun hasBleFeature(): Boolean =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)

    /** Reads the current capability snapshot. Safe to call frequently. */
    fun capabilities(): BleCapabilities {
        val a = adapter
        val hasBle = hasBleFeature()
        if (a == null) {
            return BleCapabilities(bluetoothSupported = false, bleSupported = hasBle)
        }

        val advertiser = runCatching { a.bluetoothLeAdvertiser }.getOrNull()
        val multiAdv = runCatching { a.isMultipleAdvertisementSupported }.getOrDefault(false)

        val extended = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            runCatching { a.isLeExtendedAdvertisingSupported }.getOrDefault(false)
        } else false
        val periodic = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            runCatching { a.isLePeriodicAdvertisingSupported }.getOrDefault(false)
        } else false
        val le2m = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            runCatching { a.isLe2MPhySupported }.getOrDefault(false)
        } else false
        val coded = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            runCatching { a.isLeCodedPhySupported }.getOrDefault(false)
        } else false
        val maxData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            runCatching { a.leMaximumAdvertisingDataLength }.getOrDefault(31)
        } else 31

        // Reading the adapter name/address needs BLUETOOTH_CONNECT on 12+.
        val name = if (hasConnectPermission()) {
            runCatching { a.name }.getOrNull() ?: "Bluetooth Adapter"
        } else "Permission required"

        return BleCapabilities(
            bluetoothSupported = true,
            bleSupported = hasBle,
            bluetoothEnabled = a.isEnabled,
            advertisingSupported = advertiser != null && multiAdv,
            extendedAdvertisingSupported = extended,
            periodicAdvertisingSupported = periodic,
            le2MPhySupported = le2m,
            leCodedPhySupported = coded,
            maxAdvertisingDataLength = maxData,
            adapterName = name,
            address = "Not exposed to apps",
        )
    }

    /** Reads the current runtime permission state, branching on API level. */
    fun permissionState(): PermissionState {
        val s31 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        return if (s31) {
            PermissionState(
                bluetoothGranted = isGranted(Manifest.permission.BLUETOOTH_CONNECT),
                scanGranted = isGranted(Manifest.permission.BLUETOOTH_SCAN),
                advertiseGranted = isGranted(Manifest.permission.BLUETOOTH_ADVERTISE),
                // Location is not required for BLE on 12+.
                locationGranted = true,
                nearbyDevicesGranted = isGranted(Manifest.permission.BLUETOOTH_SCAN) &&
                    isGranted(Manifest.permission.BLUETOOTH_ADVERTISE),
                notificationsGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    isGranted(Manifest.permission.POST_NOTIFICATIONS)
                } else true,
            )
        } else {
            val location = isGranted(Manifest.permission.ACCESS_FINE_LOCATION)
            PermissionState(
                bluetoothGranted = true, // legacy BLUETOOTH is normal, granted at install
                scanGranted = location,
                advertiseGranted = true,
                locationGranted = location,
                nearbyDevicesGranted = location,
                notificationsGranted = true,
            )
        }
    }

    /** The runtime permissions that must be requested for this API level. */
    fun requiredRuntimePermissions(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            buildList {
                add(Manifest.permission.BLUETOOTH_ADVERTISE)
                add(Manifest.permission.BLUETOOTH_SCAN)
                add(Manifest.permission.BLUETOOTH_CONNECT)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    add(Manifest.permission.POST_NOTIFICATIONS)
                }
            }.toTypedArray()
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }

    fun hasAdvertisePermission(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            isGranted(Manifest.permission.BLUETOOTH_ADVERTISE)
        } else true

    fun hasConnectPermission(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            isGranted(Manifest.permission.BLUETOOTH_CONNECT)
        } else true

    private fun isGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}
