package com.blespam.app.domain.model

/** TX power presets mapped to the platform advertising constants. */
enum class TxPower(val label: String) {
    ULTRA_LOW("Ultra Low"),
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
}

/** Advertising interval presets mapped to the platform advertising modes. */
enum class AdvInterval(val label: String, val approxMillis: Int) {
    LOW_LATENCY("Low Latency (~100ms)", 100),
    BALANCED("Balanced (~250ms)", 250),
    LOW_POWER("Low Power (~1000ms)", 1000),
}

/**
 * A complete, user-configurable advertising configuration.
 *
 * All fields are optional building blocks; the [AdvertisingMode] decides which
 * ones are used. Sizes are validated against the 31-byte legacy advertising
 * limit before broadcasting (see PayloadBuilder).
 */
data class AdvertisingConfig(
    val mode: AdvertisingMode = AdvertisingMode.GENERIC,
    val localName: String? = null,
    val includeDeviceName: Boolean = false,
    val serviceUuid: String? = null,
    val manufacturerId: Int? = null,
    val manufacturerData: ByteArray = ByteArray(0),
    val serviceData: ByteArray = ByteArray(0),
    val serviceDataUuid: String? = null,
    val txPower: TxPower = TxPower.MEDIUM,
    val interval: AdvInterval = AdvInterval.BALANCED,
    val connectable: Boolean = true,
    /** 0 = advertise until stopped; otherwise a bounded duration in ms. */
    val durationMillis: Int = 0,
    val payloadSize: Int = 16,
) {
    // ByteArray fields require hand-written equals/hashCode.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AdvertisingConfig) return false
        return mode == other.mode &&
            localName == other.localName &&
            includeDeviceName == other.includeDeviceName &&
            serviceUuid == other.serviceUuid &&
            manufacturerId == other.manufacturerId &&
            manufacturerData.contentEquals(other.manufacturerData) &&
            serviceData.contentEquals(other.serviceData) &&
            serviceDataUuid == other.serviceDataUuid &&
            txPower == other.txPower &&
            interval == other.interval &&
            connectable == other.connectable &&
            durationMillis == other.durationMillis &&
            payloadSize == other.payloadSize
    }

    override fun hashCode(): Int {
        var result = mode.hashCode()
        result = 31 * result + (localName?.hashCode() ?: 0)
        result = 31 * result + includeDeviceName.hashCode()
        result = 31 * result + (serviceUuid?.hashCode() ?: 0)
        result = 31 * result + (manufacturerId ?: 0)
        result = 31 * result + manufacturerData.contentHashCode()
        result = 31 * result + serviceData.contentHashCode()
        result = 31 * result + (serviceDataUuid?.hashCode() ?: 0)
        result = 31 * result + txPower.hashCode()
        result = 31 * result + interval.hashCode()
        result = 31 * result + connectable.hashCode()
        result = 31 * result + durationMillis
        result = 31 * result + payloadSize
        return result
    }
}
