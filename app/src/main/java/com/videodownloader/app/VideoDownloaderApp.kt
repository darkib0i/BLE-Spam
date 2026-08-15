package com.videodownloader.app

import android.app.Application
import com.videodownloader.app.download.DownloadEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class VideoDownloaderApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        // Warm the yt-dlp/ffmpeg binaries up front so the very first download
        // isn't stalled behind a cold init. Failures are retried lazily on use.
        appScope.launch {
            runCatching { DownloadEngine.ensureInitialized(this@VideoDownloaderApp) }
        }
    }
}
