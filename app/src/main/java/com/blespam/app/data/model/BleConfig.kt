package com.blespam.app.data.model

/**
 * An immutable description of a single advertising configuration. This is the domain object the
 * UI edits, the repository persists, and the [com.blespam.app.ble.PayloadBuilder] turns into a
 * real [android.bluetooth.le.AdvertiseData].
 *
 * All fields map onto genuine advertising parameters; nothing here bypasses OS/BLE limits.
 */
data class BleConfig(
    val mode: AdvertisingMode = AdvertisingMode.GENERIC,
    val localName: String = "BLE-Spam",
    val includeDeviceName: Boolean = true,
    val serviceUuid: String = "0000FEAA-0000-1000-8000-00805F9B34FB",
    val includeServiceUuid: Boolean = true,
    val manufacturerId: Int = 0xFFFF, // 0xFFFF is the reserved "test" company identifier.
    val manufacturerDataHex: String = "",
    val serviceDataHex: String = "",
    val txPower: TxPowerLevel = TxPowerLevel.MEDIUM,
    val speed: AdvertisingSpeed = AdvertisingSpeed.BALANCED,
    val includeTxPower: Boolean = true,
    val connectable: Boolean = true,
    /** 0 = advertise until stopped; otherwise a bounded duration in milliseconds (max 180000). */
    val durationMs: Int = 0,
) {
    companion object {
        /** Android legacy advertising payload budget in bytes. */
        const val MAX_LEGACY_PAYLOAD_BYTES = 31

        /** Platform-imposed maximum advertise duration. */
        const val MAX_DURATION_MS = 180_000
    }
}
