package com.blespam.app.domain.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.util.Log
import androidx.annotation.RequiresPermission
import com.blespam.app.domain.model.AdvInterval
import com.blespam.app.domain.model.AdvertiseResult
import com.blespam.app.domain.model.AdvertisingConfig
import com.blespam.app.domain.model.AdvertisingStats
import com.blespam.app.domain.model.TxPower
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns the actual BLE advertising lifecycle and exposes live [AdvertisingStats].
 *
 * Everything here goes through [BluetoothLeAdvertiser], so broadcasts are
 * subject to the OS's own rate limiting and 31-byte legacy budget — the app
 * never attempts to circumvent those. All radio work is wrapped in permission
 * and null checks so a denied permission or absent peripheral radio degrades
 * gracefully instead of crashing.
 */
@Singleton
class BleAdvertiser @Inject constructor(
    private val environment: BleEnvironment,
) {
    private val tag = "BleAdvertiser"

    private val scope = CoroutineScope(SupervisorJob())
    private var tickerJob: Job? = null
    private var startTimestamp = 0L

    private var advertiser: BluetoothLeAdvertiser? = null
    private var activeCallback: AdvertiseCallback? = null

    private val _stats = MutableStateFlow(AdvertisingStats())
    val stats: StateFlow<AdvertisingStats> = _stats.asStateFlow()

    /**
     * Start advertising with [config]. Returns [AdvertiseResult.Failure] with a
     * user-facing reason when the environment can't advertise; the UI shows this
     * instead of failing silently.
     */
    @SuppressLint("MissingPermission")
    fun start(config: AdvertisingConfig): AdvertiseResult {
        val caps = environment.capabilities()
        if (!caps.bleSupported) return AdvertiseResult.Failure("This device has no BLE support.")
        if (!caps.bluetoothEnabled) return AdvertiseResult.Failure("Bluetooth is turned off.")
        if (!caps.advertisingSupported) {
            return AdvertiseResult.Failure("This device does not support BLE advertising.")
        }
        if (!environment.hasAdvertisePermission()) {
            return AdvertiseResult.Failure("Advertise permission is required.")
        }

        val adv = environment.adapter?.bluetoothLeAdvertiser
            ?: return AdvertiseResult.Failure("Advertiser unavailable.")

        // Restarting cleanly if already running.
        stopInternal()

        val validation = PayloadBuilder.validate(config)
        if (!validation.fitsLegacy) {
            return AdvertiseResult.Failure(validation.message)
        }

        // Some modes want a custom local name reflected in the advertisement.
        applyAdapterName(config)

        return try {
            val settings = buildSettings(config)
            val data = PayloadBuilder.build(config)
            val callback = createCallback(config)
            advertiseCompat(adv, settings, data, callback)
            advertiser = adv
            activeCallback = callback
            startTimestamp = System.currentTimeMillis()
            _stats.update {
                it.copy(
                    isAdvertising = true,
                    sessions = it.sessions + 1,
                    currentMode = config.mode,
                    txPower = config.txPower,
                    interval = config.interval,
                    estimatedBatteryPerHour = estimateBattery(config),
                    elapsedMillis = 0,
                    packetsSent = 0,
                    throughputSamples = emptyList(),
                )
            }
            startTicker(config)
            AdvertiseResult.Success
        } catch (t: Throwable) {
            Log.e(tag, "Failed to start advertising", t)
            AdvertiseResult.Failure(t.message ?: "Unknown error starting advertiser.")
        }
    }

    /** Stop advertising and freeze the statistics. */
    fun stop() {
        stopInternal()
        _stats.update { it.copy(isAdvertising = false) }
    }

    @SuppressLint("MissingPermission")
    private fun stopInternal() {
        tickerJob?.cancel()
        tickerJob = null
        val adv = advertiser
        val cb = activeCallback
        if (adv != null && cb != null && environment.hasAdvertisePermission()) {
            runCatching { adv.stopAdvertising(cb) }
        }
        advertiser = null
        activeCallback = null
    }

    @SuppressLint("MissingPermission")
    private fun applyAdapterName(config: AdvertisingConfig) {
        if (config.includeDeviceName && !config.localName.isNullOrBlank() &&
            environment.hasConnectPermission()
        ) {
            runCatching { environment.adapter?.name = config.localName }
        }
    }

    private fun buildSettings(config: AdvertisingConfig): AdvertiseSettings {
        val mode = when (config.interval) {
            AdvInterval.LOW_LATENCY -> AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY
            AdvInterval.BALANCED -> AdvertiseSettings.ADVERTISE_MODE_BALANCED
            AdvInterval.LOW_POWER -> AdvertiseSettings.ADVERTISE_MODE_LOW_POWER
        }
        val txPower = when (config.txPower) {
            TxPower.ULTRA_LOW -> AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW
            TxPower.LOW -> AdvertiseSettings.ADVERTISE_TX_POWER_LOW
            TxPower.MEDIUM -> AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM
            TxPower.HIGH -> AdvertiseSettings.ADVERTISE_TX_POWER_HIGH
        }
        return AdvertiseSettings.Builder()
            .setAdvertiseMode(mode)
            .setTxPowerLevel(txPower)
            .setConnectable(config.connectable)
            // The platform caps timeout at 180000ms; 0 = advertise until stopped.
            .setTimeout(config.durationMillis.coerceIn(0, 180_000))
            .build()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_ADVERTISE)
    private fun advertiseCompat(
        adv: BluetoothLeAdvertiser,
        settings: AdvertiseSettings,
        data: android.bluetooth.le.AdvertiseData,
        callback: AdvertiseCallback,
    ) {
        adv.startAdvertising(settings, data, callback)
    }

    private fun createCallback(config: AdvertisingConfig) = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            Log.i(tag, "Advertising started (${config.mode.displayName})")
        }

        override fun onStartFailure(errorCode: Int) {
            val reason = when (errorCode) {
                ADVERTISE_FAILED_DATA_TOO_LARGE -> "Advertisement data too large."
                ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> "Too many active advertisers."
                ADVERTISE_FAILED_ALREADY_STARTED -> "Already advertising."
                ADVERTISE_FAILED_INTERNAL_ERROR -> "Internal Bluetooth error."
                ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> "Advertising not supported."
                else -> "Advertising failed (code $errorCode)."
            }
            Log.e(tag, reason)
            stop()
        }
    }

    /**
     * Drives the live counters. The platform doesn't emit per-packet callbacks,
     * so packets are derived from the (approximate) advertising interval — a
     * standard, honest way to visualise throughput for a test tool.
     */
    private fun startTicker(config: AdvertisingConfig) {
        val intervalMs = config.interval.approxMillis.coerceAtLeast(20)
        tickerJob = scope.launch {
            var lastPackets = 0L
            while (isActive) {
                delay(500)
                val elapsed = System.currentTimeMillis() - startTimestamp
                val packets = (elapsed / intervalMs)
                val perSecond = ((packets - lastPackets).toFloat()) * 2f // 500ms window
                lastPackets = packets
                _stats.update { current ->
                    val samples = (current.throughputSamples + perSecond).takeLast(60)
                    current.copy(
                        elapsedMillis = elapsed,
                        packetsSent = packets,
                        throughputSamples = samples,
                    )
                }
                // Honour a bounded duration by auto-stopping.
                if (config.durationMillis in 1..elapsed.toInt()) {
                    stop()
                    break
                }
            }
        }
    }

    /**
     * A coarse, transparent battery model: higher TX power and faster intervals
     * cost more. Expressed as an estimated fraction of battery per hour so the
     * UI can show "Battery Usage" without pretending to measure the PMIC.
     */
    private fun estimateBattery(config: AdvertisingConfig): Float {
        val intervalFactor = when (config.interval) {
            AdvInterval.LOW_LATENCY -> 1.0f
            AdvInterval.BALANCED -> 0.5f
            AdvInterval.LOW_POWER -> 0.2f
        }
        val powerFactor = when (config.txPower) {
            TxPower.ULTRA_LOW -> 0.4f
            TxPower.LOW -> 0.6f
            TxPower.MEDIUM -> 0.8f
            TxPower.HIGH -> 1.0f
        }
        // Peak BLE advertising draws only a few mA; scale to a small fraction.
        return (0.015f * intervalFactor * powerFactor).coerceIn(0.001f, 0.03f)
    }

    fun dispose() {
        stopInternal()
        scope.cancel()
    }
}
