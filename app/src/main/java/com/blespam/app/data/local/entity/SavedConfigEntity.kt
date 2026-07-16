package com.blespam.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A user-saved advertising configuration. The [AdvertisingConfig] is flattened
 * into primitive columns so it stays queryable and diff-friendly; byte arrays
 * are stored as uppercase hex strings for portability and log/export export.
 */
@Entity(tableName = "saved_configs")
data class SavedConfigEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val mode: String,
    val localName: String?,
    val includeDeviceName: Boolean,
    val serviceUuid: String?,
    val manufacturerId: Int?,
    val manufacturerDataHex: String,
    val serviceDataHex: String,
    val serviceDataUuid: String?,
    val txPower: String,
    val interval: String,
    val connectable: Boolean,
    val durationMillis: Int,
    val payloadSize: Int,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
