package com.blespam.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A user-saved advertising configuration. Fields mirror [com.blespam.app.data.model.BleConfig]
 * but store enums as their stable string names for forward compatibility.
 */
@Entity(tableName = "saved_configs")
data class SavedConfigEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val modeId: String,
    val localName: String,
    val includeDeviceName: Boolean,
    val serviceUuid: String,
    val includeServiceUuid: Boolean,
    val manufacturerId: Int,
    val manufacturerDataHex: String,
    val serviceDataHex: String,
    val txPower: String,
    val speed: String,
    val includeTxPower: Boolean,
    val connectable: Boolean,
    val durationMs: Int,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)

/** An entry in the advertising history log — one row per completed advertising session. */
@Entity(tableName = "advertising_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val modeId: String,
    val modeName: String,
    val localName: String,
    val txPower: String,
    val speed: String,
    val estimatedPackets: Long,
    val durationMs: Long,
    val payloadHex: String,
    val succeeded: Boolean,
    val errorMessage: String? = null,
    val startedAt: Long,
)
