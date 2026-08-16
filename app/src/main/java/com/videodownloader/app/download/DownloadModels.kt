package com.videodownloader.app.download

import android.net.Uri

/**
 * Quality presets exposed in the UI. Each maps to a yt-dlp format selector.
 * "Best" asks for the highest resolution video stream merged with the best
 * audio, which is what pulls 4K/1080p originals when the platform offers them.
 */
enum class Quality(val label: String, val tagline: String) {
    BEST("Best", "Max resolution available"),
    P1080("1080p", "Full HD"),
    P720("720p", "Lighter file"),
    AUDIO("Audio", "MP3 only");
}

/**
 * A finished download. It lives in the shared media store (so it shows up in
 * the phone's Gallery), addressed by a content [uri].
 */
data class DownloadedFile(
    val uri: Uri,
    val name: String,
    val sizeBytes: Long,
    val dateAddedMs: Long,
    val isAudio: Boolean,
)

/** Progress/emission model for the running download. */
sealed interface DownloadStatus {
    data object Idle : DownloadStatus
    data object Preparing : DownloadStatus
    data class Downloading(
        val progress: Float, // 0f..1f
        val etaSeconds: Long,
        val line: String,
    ) : DownloadStatus

    data class Success(val file: DownloadedFile) : DownloadStatus
    data class Error(val message: String) : DownloadStatus
}
