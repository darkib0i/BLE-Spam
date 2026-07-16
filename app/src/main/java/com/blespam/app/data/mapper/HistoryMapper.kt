package com.blespam.app.data.mapper

import com.blespam.app.data.local.entity.HistoryEntity
import com.blespam.app.domain.model.AdvInterval
import com.blespam.app.domain.model.AdvertisingMode
import com.blespam.app.domain.model.HistoryRecord
import com.blespam.app.domain.model.TxPower

fun HistoryEntity.toDomain(): HistoryRecord = HistoryRecord(
    id = id,
    mode = runCatching { enumValueOf<AdvertisingMode>(mode) }.getOrDefault(AdvertisingMode.GENERIC),
    txPower = runCatching { enumValueOf<TxPower>(txPower) }.getOrDefault(TxPower.MEDIUM),
    interval = runCatching { enumValueOf<AdvInterval>(interval) }.getOrDefault(AdvInterval.BALANCED),
    startedAt = startedAt,
    durationMillis = durationMillis,
    packetsSent = packetsSent,
    estimatedBatteryPerHour = estimatedBatteryPerHour,
)

fun HistoryRecord.toEntity(): HistoryEntity = HistoryEntity(
    id = id,
    mode = mode.name,
    txPower = txPower.name,
    interval = interval.name,
    startedAt = startedAt,
    durationMillis = durationMillis,
    packetsSent = packetsSent,
    estimatedBatteryPerHour = estimatedBatteryPerHour,
)
