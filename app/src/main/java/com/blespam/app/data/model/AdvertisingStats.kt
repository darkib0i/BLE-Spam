package com.blespam.app.data.model

/**
 * Live, in-memory snapshot of the current advertising session. Emitted as a Flow so the UI can
 * render glowing counters and graphs without polling.
 */
data class AdvertisingStats(
    val isAdvertising: Boolean = false,
    val mode: AdvertisingMode = AdvertisingMode.GENERIC,
    /** Estimated packets emitted, derived from elapsed time and the advertising interval. */
    val estimatedPackets: Long = 0,
    val sessionCount: Int = 0,
    val elapsedMs: Long = 0,
    val txPower: TxPowerLevel = TxPowerLevel.MEDIUM,
    val speed: AdvertisingSpeed = AdvertisingSpeed.BALANCED,
    /** Rolling window of recent packets-per-second samples used to draw the live graph. */
    val throughputSamples: List<Float> = emptyList(),
    val lastError: String? = null,
) {
    /** Very rough relative battery-usage indicator (0f..1f) based on duty cycle and TX power. */
    val batteryUsageFactor: Float
        get() {
            val speedWeight = when (speed) {
                AdvertisingSpeed.LOW_POWER -> 0.25f
                AdvertisingSpeed.BALANCED -> 0.55f
                AdvertisingSpeed.LOW_LATENCY -> 0.9f
            }
            val powerWeight = when (txPower) {
                TxPowerLevel.ULTRA_LOW -> 0.2f
                TxPowerLevel.LOW -> 0.4f
                TxPowerLevel.MEDIUM -> 0.7f
                TxPowerLevel.HIGH -> 1.0f
            }
            return ((speedWeight * 0.6f) + (powerWeight * 0.4f)).coerceIn(0f, 1f)
        }
}
