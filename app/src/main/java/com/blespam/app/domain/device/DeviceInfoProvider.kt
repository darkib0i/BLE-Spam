package com.blespam.app.domain.device

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Read-only device/battery information for the Home cards and Advanced report. */
@Singleton
class DeviceInfoProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val batteryManager =
        context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager

    private val powerManager =
        context.getSystemService(Context.POWER_SERVICE) as? PowerManager

    /** Battery level as 0..100, or -1 when unavailable. */
    fun batteryLevel(): Int =
        batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1

    fun isCharging(): Boolean =
        batteryManager?.isCharging ?: false

    /** True when the app is exempt from battery optimizations (dozing). */
    fun isIgnoringBatteryOptimizations(): Boolean =
        powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false

    fun deviceModel(): String = "${Build.MANUFACTURER} ${Build.MODEL}"
    fun androidVersion(): String = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"

    /** Emits the battery level whenever the system broadcasts a change. */
    fun batteryLevelFlow(): Flow<Int> = callbackFlow {
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                if (level >= 0 && scale > 0) {
                    trySend((level * 100) / scale)
                } else {
                    trySend(batteryLevel())
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(receiver, filter)
        trySend(batteryLevel())
        awaitClose { runCatching { context.unregisterReceiver(receiver) } }
    }
}
