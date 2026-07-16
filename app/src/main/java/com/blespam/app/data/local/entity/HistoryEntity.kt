package com.blespam.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One record of a completed (or stopped) advertising session, powering the
 * Advanced → Advertising History and the Statistics/Performance views.
 */
@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mode: String,
    val txPower: String,
    val interval: String,
    val startedAt: Long,
    val durationMillis: Long,
    val packetsSent: Long,
    val estimatedBatteryPerHour: Float,
)
