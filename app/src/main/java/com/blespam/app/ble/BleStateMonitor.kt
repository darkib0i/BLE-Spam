package com.blespam.app.ble

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.BatteryManager
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import com.blespam.app.data.model.BleConfig
import com.blespam.app.data.model.BleEnvironment
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Observes the device's Bluetooth/BLE capabilities and battery, emitting a [BleEnvironment]
 * whenever the adapter state or battery changes. Read-only — this never modifies adapter state.
 */
@Singleton
class BleStateMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val adapter: BluetoothAdapter? get() = bluetoothManager?.adapter

    /** A cold flow that re-emits the full environment snapshot on adapter/battery broadcasts. */
    val environment: Flow<BleEnvironment> = callbackFlow {
        trySend(snapshot())
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                trySend(snapshot())
            }
        }
        val filter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED)
        }
        // System broadcasts, so export flag is irrelevant but required on API 33+.
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        awaitClose { runCatching { context.unregisterReceiver(receiver) } }
    }

    /** Builds a one-shot snapshot; annotated so lint knows CONNECT is only touched when granted. */
    fun snapshot(): BleEnvironment {
        val hasBle = context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
        val a = adapter
        val enabled = a?.isEnabled == true
        val advertisingSupported = enabled && a?.isMultipleAdvertisementSupported == true
        val extended = enabled && a?.isLeExtendedAdvertisingSupported == true
        val maxLen = if (enabled) {
            runCatching { a?.leMaximumAdvertisingDataLength ?: BleConfig.MAX_LEGACY_PAYLOAD_BYTES }
                .getOrDefault(BleConfig.MAX_LEGACY_PAYLOAD_BYTES)
        } else {
            BleConfig.MAX_LEGACY_PAYLOAD_BYTES
        }

        val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else -1
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL

        return BleEnvironment(
            bluetoothSupported = a != null,
            bleSupported = hasBle,
            advertisingSupported = advertisingSupported,
            bluetoothEnabled = enabled,
            adapterName = readAdapterName(),
            maxAdvertisingDataLength = maxLen,
            supportsExtendedAdvertising = extended,
            batteryLevelPercent = batteryPct,
            isCharging = charging,
        )
    }

    private fun readAdapterName(): String {
        // Reading the adapter name requires BLUETOOTH_CONNECT on Android 12+. Guard it so a missing
        // permission degrades gracefully to a placeholder instead of throwing.
        val granted = context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) ==
            PackageManager.PERMISSION_GRANTED
        return if (granted) safeAdapterName() else "Grant permission to view"
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun safeAdapterName(): String =
        runCatching { adapter?.name }.getOrNull() ?: "Bluetooth Adapter"
}
