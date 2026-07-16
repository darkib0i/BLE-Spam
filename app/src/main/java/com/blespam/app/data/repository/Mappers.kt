package com.blespam.app.data.repository

import com.blespam.app.data.local.SavedConfigEntity
import com.blespam.app.data.model.AdvertisingMode
import com.blespam.app.data.model.AdvertisingSpeed
import com.blespam.app.data.model.BleConfig
import com.blespam.app.data.model.TxPowerLevel

/** Converts a persisted row into the domain [BleConfig] used across the app. */
fun SavedConfigEntity.toConfig(): BleConfig = BleConfig(
    mode = AdvertisingMode.fromId(modeId),
    localName = localName,
    includeDeviceName = includeDeviceName,
    serviceUuid = serviceUuid,
    includeServiceUuid = includeServiceUuid,
    manufacturerId = manufacturerId,
    manufacturerDataHex = manufacturerDataHex,
    serviceDataHex = serviceDataHex,
    txPower = TxPowerLevel.fromName(txPower),
    speed = AdvertisingSpeed.fromName(speed),
    includeTxPower = includeTxPower,
    connectable = connectable,
    durationMs = durationMs,
)

/** Converts a domain [BleConfig] into a persistable row with the supplied name/id/favorite. */
fun BleConfig.toEntity(
    name: String,
    id: Long = 0,
    isFavorite: Boolean = false,
    createdAt: Long = System.currentTimeMillis(),
): SavedConfigEntity = SavedConfigEntity(
    id = id,
    name = name,
    modeId = mode.id,
    localName = localName,
    includeDeviceName = includeDeviceName,
    serviceUuid = serviceUuid,
    includeServiceUuid = includeServiceUuid,
    manufacturerId = manufacturerId,
    manufacturerDataHex = manufacturerDataHex,
    serviceDataHex = serviceDataHex,
    txPower = txPower.name,
    speed = speed.name,
    includeTxPower = includeTxPower,
    connectable = connectable,
    durationMs = durationMs,
    isFavorite = isFavorite,
    createdAt = createdAt,
)
