package com.blespam.app.ble

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission
import com.blespam.app.data.model.BleConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin, testable wrapper over [BluetoothLeAdvertiser]. It builds [AdvertiseSettings] from a
 * [BleConfig], starts/stops a single advertisement, and reports success/failure through a
 * listener. It performs no privilege escalation and honours the platform's own limits.
 */
@Singleton
class BleAdvertiser @Inject constructor(
    @ApplicationContext private val context: Context,
    private val payloadBuilder: PayloadBuilder,
) {
    /** Callbacks the controller listens to. */
    interface Listener {
        fun onStarted(settings: AdvertiseSettings?)
        fun onFailed(reason: String)
    }

    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val adapter: BluetoothAdapter? get() = bluetoothManager?.adapter

    private var advertiser: BluetoothLeAdvertiser? = null
    private var activeCallback: AdvertiseCallback? = null

    val isAdvertising: Boolean get() = activeCallback != null

    /** Whether we currently hold the permission required to advertise. */
    fun hasAdvertisePermission(): Boolean =
        context.checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED

    /**
     * Starts advertising [config]. Returns the built payload (with any validation error) so callers
     * can log or display the exact bytes. Actual start is asynchronous — the [listener] fires later.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_ADVERTISE)
    fun start(config: BleConfig, listener: Listener): PayloadBuilder.Built {
        stop() // Only one advertisement at a time; supersede any previous one.

        val built = payloadBuilder.build(config)
        if (!built.isValid) {
            listener.onFailed(built.error ?: "Invalid payload")
            return built
        }

        val a = adapter
        if (a == null || !a.isEnabled) {
            listener.onFailed("Bluetooth is turned off")
            return built
        }
        if (!a.isMultipleAdvertisementSupported) {
            listener.onFailed("This device does not support BLE advertising")
            return built
        }

        // Apply the requested local name so name-based modes advertise correctly.
        runCatching { if (config.includeDeviceName) a.name = config.localName }

        val adv = a.bluetoothLeAdvertiser
        if (adv == null) {
            listener.onFailed("BLE advertiser unavailable")
            return built
        }
        advertiser = adv

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(config.speed.advertiseConstant)
            .setTxPowerLevel(config.txPower.advertiseConstant)
            .setConnectable(config.connectable)
            .setTimeout(config.durationMs.coerceIn(0, BleConfig.MAX_DURATION_MS))
            .build()

        val callback = object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                listener.onStarted(settingsInEffect)
            }

            override fun onStartFailure(errorCode: Int) {
                activeCallback = null
                listener.onFailed(describeError(errorCode))
            }
        }
        activeCallback = callback

        runCatching {
            adv.startAdvertising(settings, built.data, callback)
        }.onFailure {
            activeCallback = null
            listener.onFailed(it.message ?: "Failed to start advertising")
        }
        return built
    }

    /** Stops any active advertisement. Safe to call when nothing is running. */
    @RequiresPermission(Manifest.permission.BLUETOOTH_ADVERTISE)
    fun stop() {
        val cb = activeCallback ?: return
        runCatching { advertiser?.stopAdvertising(cb) }
        activeCallback = null
    }

    private fun describeError(code: Int): String = when (code) {
        AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE ->
            "Advertisement data too large for this device"
        AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS ->
            "Too many advertisers active; try again shortly"
        AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED ->
            "Advertising already started"
        AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR ->
            "Internal Bluetooth error"
        AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED ->
            "Advertising not supported on this device"
        else -> "Advertising failed (code $code)"
    }
}
