package com.blespam.app.ble

import android.bluetooth.le.AdvertiseSettings
import com.blespam.app.data.local.HistoryEntity
import com.blespam.app.data.model.AdvertisingStats
import com.blespam.app.data.model.BleConfig
import com.blespam.app.data.repository.HistoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Application-scoped coordinator that owns the *logical* advertising session: it drives the
 * [BleAdvertiser], keeps a live [AdvertisingStats] flow updated on a lightweight ticker, and
 * records each finished session to history. ViewModels observe [stats] and call [start]/[stop];
 * they never touch the raw BLE API directly.
 */
@Singleton
class AdvertisingController @Inject constructor(
    private val advertiser: BleAdvertiser,
    private val historyRepository: HistoryRepository,
) {
    private val _stats = MutableStateFlow(AdvertisingStats())
    val stats: StateFlow<AdvertisingStats> = _stats.asStateFlow()

    private var sessionCount = 0
    private var tickerJob: Job? = null
    private var startElapsedRef = 0L
    private var currentConfig: BleConfig? = null
    private var lastPayloadHex: String = ""

    /** Called by the foreground service when the OS/user wants advertising to begin. */
    fun start(scope: CoroutineScope, config: BleConfig) {
        currentConfig = config
        val built = advertiser.start(config, object : BleAdvertiser.Listener {
            override fun onStarted(settings: AdvertiseSettings?) {
                sessionCount += 1
                startElapsedRef = System.currentTimeMillis()
                _stats.value = AdvertisingStats(
                    isAdvertising = true,
                    mode = config.mode,
                    sessionCount = sessionCount,
                    txPower = config.txPower,
                    speed = config.speed,
                    lastError = null,
                )
                startTicker(scope, config)
            }

            override fun onFailed(reason: String) {
                _stats.value = _stats.value.copy(isAdvertising = false, lastError = reason)
                recordHistory(scope, config, succeeded = false, error = reason)
            }
        })
        lastPayloadHex = built.previewHex
    }

    /** Stops advertising and records the completed session. */
    fun stop(scope: CoroutineScope) {
        if (!_stats.value.isAdvertising && tickerJob == null) return
        tickerJob?.cancel()
        tickerJob = null
        runCatching { advertiser.stop() }
        val config = currentConfig
        val elapsed = _stats.value.elapsedMs
        _stats.value = _stats.value.copy(isAdvertising = false, elapsedMs = elapsed)
        if (config != null) recordHistory(scope, config, succeeded = true, error = null)
    }

    /**
     * The stats ticker updates once per second. We intentionally do NOT count real radio packets
     * (the OS does not expose them); instead we present an honest *estimate* from elapsed time and
     * the nominal advertising interval, which is what a developer expects to see.
     */
    private fun startTicker(scope: CoroutineScope, config: BleConfig) {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            val intervalMs = config.speed.approxIntervalMs.coerceAtLeast(1)
            val samples = ArrayDeque<Float>(MAX_SAMPLES)
            while (true) {
                val elapsed = System.currentTimeMillis() - startElapsedRef
                val packets = elapsed / intervalMs
                val perSecond = 1000f / intervalMs
                if (samples.size >= MAX_SAMPLES) samples.removeFirst()
                // Small jitter keeps the live graph feeling alive without misrepresenting the rate.
                samples.addLast(perSecond * (0.9f + (Math.random().toFloat() * 0.2f)))
                _stats.value = _stats.value.copy(
                    isAdvertising = true,
                    estimatedPackets = packets,
                    elapsedMs = elapsed,
                    throughputSamples = samples.toList(),
                )
                delay(TICK_MS)
            }
        }
    }

    private fun recordHistory(scope: CoroutineScope, config: BleConfig, succeeded: Boolean, error: String?) {
        val elapsed = _stats.value.elapsedMs
        val packets = _stats.value.estimatedPackets
        scope.launch {
            runCatching {
                historyRepository.record(
                    HistoryEntity(
                        modeId = config.mode.id,
                        modeName = config.mode.displayName,
                        localName = config.localName,
                        txPower = config.txPower.name,
                        speed = config.speed.name,
                        estimatedPackets = packets,
                        durationMs = elapsed,
                        payloadHex = lastPayloadHex,
                        succeeded = succeeded,
                        errorMessage = error,
                        startedAt = startElapsedRef.takeIf { it > 0 } ?: System.currentTimeMillis(),
                    )
                )
            }
        }
    }

    private companion object {
        const val TICK_MS = 1000L
        const val MAX_SAMPLES = 40
    }
}
