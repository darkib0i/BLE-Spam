package com.blespam.app.domain.model

/** Live statistics surfaced on the Control screen. */
data class AdvertisingStats(
    val isAdvertising: Boolean = false,
    val packetsSent: Long = 0,
    val sessions: Int = 0,
    val elapsedMillis: Long = 0,
    val currentMode: AdvertisingMode = AdvertisingMode.GENERIC,
    val txPower: TxPower = TxPower.MEDIUM,
    val interval: AdvInterval = AdvInterval.BALANCED,
    /** Rough, model-based battery cost estimate as a 0..1 fraction per hour. */
    val estimatedBatteryPerHour: Float = 0f,
    /** Rolling packets-per-second samples for the live graph. */
    val throughputSamples: List<Float> = emptyList(),
)

/** Result of attempting to (re)start advertising. */
sealed interface AdvertiseResult {
    data object Success : AdvertiseResult
    data class Failure(val reason: String) : AdvertiseResult
}
