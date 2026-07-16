package com.blespam.app.ble

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.blespam.app.MainActivity
import com.blespam.app.R
import com.blespam.app.data.model.AdvertisingMode
import com.blespam.app.data.model.AdvertisingSpeed
import com.blespam.app.data.model.BleConfig
import com.blespam.app.data.model.TxPowerLevel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * Foreground service that keeps a BLE advertisement alive while the app is backgrounded and shows
 * the required ongoing notification. Business logic lives in [AdvertisingController]; this class
 * only owns the Android service lifecycle and notification.
 */
@AndroidEntryPoint
class AdvertisingService : Service() {

    @Inject lateinit var controller: AdvertisingController

    private val serviceScope = CoroutineScope(SupervisorJob())
    private var observeJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> handleStart(intent)
            ACTION_STOP -> handleStop()
            else -> handleStop()
        }
        return START_NOT_STICKY
    }

    private fun handleStart(intent: Intent) {
        val config = intent.toConfig()
        ensureChannel()
        startForegroundCompat(config.mode)
        controller.start(serviceScope, config)

        // Keep the notification's mode label in sync and stop the service if advertising ends.
        observeJob?.cancel()
        observeJob = controller.stats
            .onEach { stats ->
                if (!stats.isAdvertising && stats.lastError != null) {
                    stopSelf()
                }
            }
            .launchIn(serviceScope)
    }

    private fun handleStop() {
        controller.stop(serviceScope)
        stopForegroundCompat()
        stopSelf()
    }

    override fun onDestroy() {
        controller.stop(serviceScope)
        observeJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    // --- Notification --------------------------------------------------------------------------

    private fun startForegroundCompat(mode: AdvertisingMode) {
        val notification = buildNotification(mode)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun stopForegroundCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }

    private fun buildNotification(mode: AdvertisingMode) = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle(getString(R.string.notification_advertising_title))
        .setContentText(getString(R.string.notification_advertising_text, mode.displayName))
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setOngoing(true)
        .setContentIntent(contentIntent())
        .addAction(0, "Stop", stopIntent())
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()

    private fun contentIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        return PendingIntent.getActivity(this, 0, intent, pendingFlags())
    }

    private fun stopIntent(): PendingIntent {
        val intent = Intent(this, AdvertisingService::class.java).setAction(ACTION_STOP)
        return PendingIntent.getService(this, 1, intent, pendingFlags())
    }

    private fun pendingFlags(): Int =
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

    private fun ensureChannel() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW,
            ).apply { description = getString(R.string.notification_channel_desc) }
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val ACTION_START = "com.blespam.app.action.START"
        const val ACTION_STOP = "com.blespam.app.action.STOP"
        private const val CHANNEL_ID = "ble_advertising"
        private const val NOTIFICATION_ID = 42

        // Intent extras used to pass the config across the process boundary.
        private const val EX_MODE = "mode"
        private const val EX_NAME = "name"
        private const val EX_INCLUDE_NAME = "include_name"
        private const val EX_UUID = "uuid"
        private const val EX_INCLUDE_UUID = "include_uuid"
        private const val EX_MFR_ID = "mfr_id"
        private const val EX_MFR_DATA = "mfr_data"
        private const val EX_SVC_DATA = "svc_data"
        private const val EX_TX = "tx"
        private const val EX_SPEED = "speed"
        private const val EX_INCLUDE_TX = "include_tx"
        private const val EX_CONNECTABLE = "connectable"
        private const val EX_DURATION = "duration"

        /** Builds the start intent, serializing the config into extras. */
        fun startIntent(context: Context, config: BleConfig): Intent =
            Intent(context, AdvertisingService::class.java).apply {
                action = ACTION_START
                putExtra(EX_MODE, config.mode.id)
                putExtra(EX_NAME, config.localName)
                putExtra(EX_INCLUDE_NAME, config.includeDeviceName)
                putExtra(EX_UUID, config.serviceUuid)
                putExtra(EX_INCLUDE_UUID, config.includeServiceUuid)
                putExtra(EX_MFR_ID, config.manufacturerId)
                putExtra(EX_MFR_DATA, config.manufacturerDataHex)
                putExtra(EX_SVC_DATA, config.serviceDataHex)
                putExtra(EX_TX, config.txPower.name)
                putExtra(EX_SPEED, config.speed.name)
                putExtra(EX_INCLUDE_TX, config.includeTxPower)
                putExtra(EX_CONNECTABLE, config.connectable)
                putExtra(EX_DURATION, config.durationMs)
            }

        fun stopIntent(context: Context): Intent =
            Intent(context, AdvertisingService::class.java).setAction(ACTION_STOP)

        private fun Intent.toConfig(): BleConfig = BleConfig(
            mode = AdvertisingMode.fromId(getStringExtra(EX_MODE) ?: AdvertisingMode.GENERIC.id),
            localName = getStringExtra(EX_NAME) ?: "BLE-Spam",
            includeDeviceName = getBooleanExtra(EX_INCLUDE_NAME, true),
            serviceUuid = getStringExtra(EX_UUID) ?: "",
            includeServiceUuid = getBooleanExtra(EX_INCLUDE_UUID, true),
            manufacturerId = getIntExtra(EX_MFR_ID, 0xFFFF),
            manufacturerDataHex = getStringExtra(EX_MFR_DATA) ?: "",
            serviceDataHex = getStringExtra(EX_SVC_DATA) ?: "",
            txPower = TxPowerLevel.fromName(getStringExtra(EX_TX) ?: TxPowerLevel.MEDIUM.name),
            speed = AdvertisingSpeed.fromName(getStringExtra(EX_SPEED) ?: AdvertisingSpeed.BALANCED.name),
            includeTxPower = getBooleanExtra(EX_INCLUDE_TX, true),
            connectable = getBooleanExtra(EX_CONNECTABLE, true),
            durationMs = getIntExtra(EX_DURATION, 0),
        )
    }
}
