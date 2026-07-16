package com.blespam.app.data.mapper

import com.blespam.app.data.local.entity.SavedConfigEntity
import com.blespam.app.domain.model.AdvInterval
import com.blespam.app.domain.model.AdvertisingConfig
import com.blespam.app.domain.model.AdvertisingMode
import com.blespam.app.domain.model.SavedConfig
import com.blespam.app.domain.model.TxPower
import com.blespam.app.domain.util.HexUtils

/**
 * Maps between the persisted [SavedConfigEntity] and the domain [SavedConfig] /
 * [AdvertisingConfig]. Enum names are stored as strings and parsed defensively
 * so a future rename can't crash existing rows.
 */
fun SavedConfigEntity.toDomain(): SavedConfig = SavedConfig(
    id = id,
    name = name,
    isFavorite = isFavorite,
    createdAt = createdAt,
    updatedAt = updatedAt,
    config = AdvertisingConfig(
        mode = enumOrDefault(mode, AdvertisingMode.GENERIC),
        localName = localName,
        includeDeviceName = includeDeviceName,
        serviceUuid = serviceUuid,
        manufacturerId = manufacturerId,
        manufacturerData = HexUtils.fromHex(manufacturerDataHex),
        serviceData = HexUtils.fromHex(serviceDataHex),
        serviceDataUuid = serviceDataUuid,
        txPower = enumOrDefault(txPower, TxPower.MEDIUM),
        interval = enumOrDefault(interval, AdvInterval.BALANCED),
        connectable = connectable,
        durationMillis = durationMillis,
        payloadSize = payloadSize,
    ),
)

fun SavedConfig.toEntity(): SavedConfigEntity = SavedConfigEntity(
    id = id,
    name = name,
    mode = config.mode.name,
    localName = config.localName,
    includeDeviceName = config.includeDeviceName,
    serviceUuid = config.serviceUuid,
    manufacturerId = config.manufacturerId,
    manufacturerDataHex = HexUtils.toHex(config.manufacturerData),
    serviceDataHex = HexUtils.toHex(config.serviceData),
    serviceDataUuid = config.serviceDataUuid,
    txPower = config.txPower.name,
    interval = config.interval.name,
    connectable = config.connectable,
    durationMillis = config.durationMillis,
    payloadSize = config.payloadSize,
    isFavorite = isFavorite,
    createdAt = createdAt,
    updatedAt = System.currentTimeMillis(),
)

private inline fun <reified T : Enum<T>> enumOrDefault(name: String, default: T): T =
    runCatching { enumValueOf<T>(name) }.getOrDefault(default)
