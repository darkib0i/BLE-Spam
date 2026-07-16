package com.blespam.app.domain.ble

import android.app.Notification
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
import androidx.core.app.ServiceCompat
import com.blespam.app.MainActivity
import com.blespam.app.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * A lightweight foreground service that keeps the app's process alive while a
 * long advertising session runs in the background, and shows the user a
 * persistent, dismissible-on-stop notification (a transparency requirement for
 * any tool that broadcasts).
 *
 * The service does not itself start the radio; [BleAdvertiser] (a singleton)
 * owns advertising. The service only anchors the process and reflects state in
 * a notification, which keeps the radio lifecycle in one place.
 */
@AndroidEntryPoint
class AdvertisingService : Service() {

    @Inject lateinit var advertiser: BleAdvertiser

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_STOP -> {
                advertiser.stop()
                stopForegroundCompat()
                stopSelf()
            }
            else -> startForegroundCompat()
        }
        // Do not auto-restart if the system kills us; the user re-arms explicitly.
        return START_NOT_STICKY
    }

    // This is a started (not bound) service.
    override fun onBind(intent: Intent): IBinder? = null

    private fun startForegroundCompat() {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
        } else 0
        ServiceCompat.startForeground(this, NOTIFICATION_ID, buildNotification(), type)
    }

    private fun stopForegroundCompat() {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
    }

    private fun buildNotification(): Notification {
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val stopIntent = PendingIntent.getService(
            this, 1, Intent(this, AdvertisingService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("BLE advertising session is active")
            .setSmallIcon(R.drawable.ic_ble_logo)
            .setOngoing(true)
            .setContentIntent(open)
            .addAction(0, "Stop", stopIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Advertising",
                NotificationManager.IMPORTANCE_LOW,
            ).apply { description = "Shown while a BLE advertising session is active" }
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "ble_advertising"
        private const val NOTIFICATION_ID = 1001
        const val ACTION_STOP = "com.blespam.app.action.STOP"

        fun start(context: Context) {
            val intent = Intent(context, AdvertisingService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.startService(
                Intent(context, AdvertisingService::class.java).setAction(ACTION_STOP),
            )
        }
    }
}
