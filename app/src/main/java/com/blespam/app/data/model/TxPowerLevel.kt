package com.blespam.app.data.model

import android.bluetooth.le.AdvertiseSettings

/**
 * Maps a friendly power tier onto the fixed set of TX-power constants the Android advertiser
 * exposes. There is no way (and no attempt) to exceed the hardware's advertised power.
 */
enum class TxPowerLevel(
    val displayName: String,
    val advertiseConstant: Int,
    /** Approximate dBm used only for UI display; real value is hardware-dependent. */
    val approxDbm: Int,
) {
    ULTRA_LOW("Ultra Low", AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW, -21),
    LOW("Low", AdvertiseSettings.ADVERTISE_TX_POWER_LOW, -15),
    MEDIUM("Medium", AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM, -7),
    HIGH("High", AdvertiseSettings.ADVERTISE_TX_POWER_HIGH, 1);

    companion object {
        fun fromName(name: String): TxPowerLevel = entries.firstOrNull { it.name == name } ?: MEDIUM
    }
}

/**
 * Advertising duty-cycle tiers, mapped to the platform advertise-mode constants. Lower frequency
 * means longer intervals and lower battery usage.
 */
enum class AdvertisingSpeed(
    val displayName: String,
    val advertiseConstant: Int,
    /** Nominal interval in milliseconds, for display and battery estimates. */
    val approxIntervalMs: Int,
) {
    LOW_POWER("Low Power", AdvertiseSettings.ADVERTISE_MODE_LOW_POWER, 1000),
    BALANCED("Balanced", AdvertiseSettings.ADVERTISE_MODE_BALANCED, 250),
    LOW_LATENCY("Low Latency", AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY, 100);

    companion object {
        fun fromName(name: String): AdvertisingSpeed = entries.firstOrNull { it.name == name } ?: BALANCED
    }
}
