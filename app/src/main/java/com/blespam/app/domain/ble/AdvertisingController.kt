package com.blespam.app.domain.ble

import android.content.Context
import com.blespam.app.data.repository.ConfigRepository
import com.blespam.app.domain.model.AdvertiseResult
import com.blespam.app.domain.model.AdvertisingConfig
import com.blespam.app.domain.model.AdvertisingStats
import com.blespam.app.domain.model.HistoryRecord
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * App-wide advertising session coordinator. Screens observe [config] and
 * [stats] from here so the Modes editor and the Control screen always agree.
 * It owns the foreground-service lifecycle and writes a [HistoryRecord] whenever
 * a session ends, feeding the Advanced statistics.
 */
@Singleton
class AdvertisingController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val advertiser: BleAdvertiser,
    private val repository: ConfigRepository,
) {
    private val scope = CoroutineScope(SupervisorJob())

    private val _config = MutableStateFlow(AdvertisingConfig())
    val config: StateFlow<AdvertisingConfig> = _config.asStateFlow()

    val stats: StateFlow<AdvertisingStats> = advertiser.stats

    private var sessionStartedAt = 0L

    fun updateConfig(transform: (AdvertisingConfig) -> AdvertisingConfig) {
        _config.update(transform)
    }

    fun setConfig(config: AdvertisingConfig) {
        _config.value = config
    }

    /** Start advertising with the current config, launching the foreground service. */
    fun start(): AdvertiseResult {
        val result = advertiser.start(_config.value)
        if (result is AdvertiseResult.Success) {
            sessionStartedAt = System.currentTimeMillis()
            AdvertisingService.start(context)
        }
        return result
    }

    /** Stop advertising, tear down the service, and persist the session record. */
    fun stop() {
        val stats = advertiser.stats.value
        advertiser.stop()
        AdvertisingService.stop(context)
        if (sessionStartedAt > 0L && stats.packetsSent > 0) {
            val record = HistoryRecord(
                mode = stats.currentMode,
                txPower = stats.txPower,
                interval = stats.interval,
                startedAt = sessionStartedAt,
                durationMillis = stats.elapsedMillis,
                packetsSent = stats.packetsSent,
                estimatedBatteryPerHour = stats.estimatedBatteryPerHour,
            )
            scope.launch { repository.recordHistory(record) }
        }
        sessionStartedAt = 0L
    }

    fun toggle(): AdvertiseResult {
        return if (stats.value.isAdvertising) {
            stop()
            AdvertiseResult.Success
        } else {
            start()
        }
    }
}
