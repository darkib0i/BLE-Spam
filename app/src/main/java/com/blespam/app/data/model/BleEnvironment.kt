package com.blespam.app.data.model

/**
 * Snapshot of everything the Home screen's status cards need to render: adapter state,
 * capabilities, and battery. Produced by [com.blespam.app.ble.BleStateMonitor].
 */
data class BleEnvironment(
    val bluetoothSupported: Boolean = false,
    val bleSupported: Boolean = false,
    val advertisingSupported: Boolean = false,
    val bluetoothEnabled: Boolean = false,
    val adapterName: String = "Unknown",
    val maxAdvertisingDataLength: Int = BleConfig.MAX_LEGACY_PAYLOAD_BYTES,
    val supportsExtendedAdvertising: Boolean = false,
    val batteryLevelPercent: Int = -1,
    val isCharging: Boolean = false,
)

/** Coarse permission summary used by the UI to gate the Start button and show status chips. */
data class PermissionState(
    val hasAdvertise: Boolean = false,
    val hasScan: Boolean = false,
    val hasConnect: Boolean = false,
    val hasLocation: Boolean = false,
    val hasNotifications: Boolean = false,
) {
    /** The permissions strictly required to begin advertising on this OS version. */
    val canAdvertise: Boolean get() = hasAdvertise && hasConnect
}
