package com.videodownloader.app.download

import android.content.Context
import android.os.Environment
import com.yausername.aria2c.Aria2c
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Thin wrapper around youtubedl-android (a packaged yt-dlp + ffmpeg + aria2c).
 *
 * yt-dlp is a general-purpose media extractor, so a single code path handles
 * YouTube, TikTok, Instagram, X, Facebook, Reddit and the ~1800 other sites
 * it supports — the caller only ever passes a URL and a [Quality].
 *
 * All native calls are blocking and are dispatched onto [Dispatchers.IO].
 */
object DownloadEngine {

    private val initMutex = Mutex()

    @Volatile
    private var initialized = false

    /** Where finished files land. Scoped storage, so no runtime permission. */
    fun videoDir(context: Context): File =
        context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
            ?: File(context.filesDir, "Movies").apply { mkdirs() }

    fun audioDir(context: Context): File =
        context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            ?: File(context.filesDir, "Music").apply { mkdirs() }

    /**
     * Initialises the native engines exactly once. Safe to call repeatedly;
     * concurrent callers wait on the same init. Throws on failure so the UI
     * can surface it.
     */
    suspend fun ensureInitialized(context: Context) {
        if (initialized) return
        initMutex.withLock {
            if (initialized) return
            withContext(Dispatchers.IO) {
                val app = context.applicationContext
                YoutubeDL.getInstance().init(app)
                FFmpeg.getInstance().init(app)
                Aria2c.getInstance().init(app)
            }
            initialized = true
        }
    }

    /**
     * Runs a download to completion. [onProgress] is invoked on a background
     * thread with progress in 0f..1f, an ETA in seconds and the raw yt-dlp
     * status line. Returns the freshly written file.
     */
    suspend fun download(
        context: Context,
        url: String,
        quality: Quality,
        processId: String,
        onProgress: (Float, Long, String) -> Unit,
    ): DownloadedFile = withContext(Dispatchers.IO) {
        ensureInitialized(context)

        val isAudio = quality == Quality.AUDIO
        val targetDir = if (isAudio) audioDir(context) else videoDir(context)
        val before = targetDir.listFiles()?.toSet() ?: emptySet()

        val request = YoutubeDLRequest(url).apply {
            // Never expand a shared link into a whole channel/playlist.
            addOption("--no-playlist")
            addOption("--no-mtime")
            // Keep filenames filesystem-safe across every source site.
            addOption("--restrict-filenames")
            addOption("-o", File(targetDir, "%(title).150B_%(id)s.%(ext)s").absolutePath)

            when (quality) {
                Quality.BEST -> {
                    addOption("-f", "bv*+ba/b")
                    addOption("--merge-output-format", "mp4")
                }
                Quality.P1080 -> {
                    addOption("-f", "bv*[height<=1080]+ba/b[height<=1080]")
                    addOption("--merge-output-format", "mp4")
                }
                Quality.P720 -> {
                    addOption("-f", "bv*[height<=720]+ba/b[height<=720]")
                    addOption("--merge-output-format", "mp4")
                }
                Quality.AUDIO -> {
                    addOption("-x")
                    addOption("--audio-format", "mp3")
                    addOption("--audio-quality", "0")
                }
            }
        }

        YoutubeDL.getInstance().execute(request, processId) { progress, etaSeconds, line ->
            val fraction = (progress / 100f).coerceIn(0f, 1f)
            onProgress(fraction, etaSeconds, line)
        }

        // yt-dlp doesn't hand back the final path directly, so diff the folder.
        val after = targetDir.listFiles()?.toList() ?: emptyList()
        val newFile = after.filterNot { it in before }
            .maxByOrNull { it.lastModified() }
            ?: after.maxByOrNull { it.lastModified() }
            ?: error("Download finished but no output file was found.")

        newFile.toDownloadedFile(isAudio = isAudio)
    }

    /** Cancels a running download started with the given [processId]. */
    fun cancel(processId: String) {
        runCatching { YoutubeDL.getInstance().destroyProcessById(processId) }
    }

    /** Lists everything downloaded so far, newest first. */
    suspend fun listDownloads(context: Context): List<DownloadedFile> =
        withContext(Dispatchers.IO) {
            val videos = videoDir(context).listFiles()?.map { it.toDownloadedFile(false) }.orEmpty()
            val audio = audioDir(context).listFiles()?.map { it.toDownloadedFile(true) }.orEmpty()
            (videos + audio)
                .filter { it.file.isFile && it.sizeBytes > 0 }
                .sortedByDescending { it.lastModified }
        }
}

private fun File.toDownloadedFile(isAudio: Boolean) = DownloadedFile(
    file = this,
    name = nameWithoutExtension.replace('_', ' '),
    sizeBytes = length(),
    lastModified = lastModified(),
    isAudio = isAudio || extension.lowercase() in setOf("mp3", "m4a", "opus", "aac", "wav", "flac"),
)
