package com.blespam.app.ble

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import com.blespam.app.data.model.AdvertisingStats
import com.blespam.app.data.model.BleConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The single entry point the UI layer uses to toggle advertising. It hides the foreground-service
 * plumbing behind [start]/[stop] and re-exposes the live [stats] flow from the controller.
 */
@Singleton
class AdvertisingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    controller: AdvertisingController,
    private val advertiser: BleAdvertiser,
) {
    val stats: StateFlow<AdvertisingStats> = controller.stats

    fun hasAdvertisePermission(): Boolean = advertiser.hasAdvertisePermission()

    fun start(config: BleConfig) {
        val intent = AdvertisingService.startIntent(context, config)
        startServiceCompat(intent)
    }

    fun stop() {
        // Route stop through the service so its notification is torn down cleanly.
        context.startService(AdvertisingService.stopIntent(context))
    }

    private fun startServiceCompat(intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(context, intent)
        } else {
            context.startService(intent)
        }
    }
}
