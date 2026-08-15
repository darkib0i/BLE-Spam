package com.videodownloader.app.download

import java.io.File

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

/** A file that finished downloading, shown in the Library tab. */
data class DownloadedFile(
    val file: File,
    val name: String,
    val sizeBytes: Long,
    val lastModified: Long,
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
