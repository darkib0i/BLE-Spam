package com.blespam.app.domain.model

/** Domain representation of a saved, named advertising configuration. */
data class SavedConfig(
    val id: Long = 0,
    val name: String,
    val config: AdvertisingConfig,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

/** Domain representation of one history record. */
data class HistoryRecord(
    val id: Long = 0,
    val mode: AdvertisingMode,
    val txPower: TxPower,
    val interval: AdvInterval,
    val startedAt: Long,
    val durationMillis: Long,
    val packetsSent: Long,
    val estimatedBatteryPerHour: Float,
)
