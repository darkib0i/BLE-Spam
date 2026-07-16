package com.blespam.app.domain.model

/**
 * A snapshot of the device's Bluetooth/BLE state and capabilities, surfaced on
 * the Home screen status cards and the Advanced → BLE Capability Report.
 */
data class BleCapabilities(
    val bluetoothSupported: Boolean = false,
    val bleSupported: Boolean = false,
    val bluetoothEnabled: Boolean = false,
    val advertisingSupported: Boolean = false,
    val extendedAdvertisingSupported: Boolean = false,
    val periodicAdvertisingSupported: Boolean = false,
    val le2MPhySupported: Boolean = false,
    val leCodedPhySupported: Boolean = false,
    val maxAdvertisingDataLength: Int = 31,
    val adapterName: String = "Unknown",
    val address: String = "Unavailable",
)

/** Coarse permission state used to drive the status cards and gating logic. */
data class PermissionState(
    val bluetoothGranted: Boolean = false,
    val scanGranted: Boolean = false,
    val advertiseGranted: Boolean = false,
    val locationGranted: Boolean = false,
    val nearbyDevicesGranted: Boolean = false,
    val notificationsGranted: Boolean = false,
) {
    /** True when everything required to advertise has been granted. */
    val readyToAdvertise: Boolean
        get() = advertiseGranted
}
