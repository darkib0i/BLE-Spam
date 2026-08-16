package com.videodownloader.app.download

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
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

    /** Sub-folder created inside the shared Movies/Music collections. */
    private const val ALBUM = "VideoDownloader"

    /** Private scratch dirs where yt-dlp writes before we publish to the gallery. */
    private fun tempVideoDir(context: Context): File =
        (context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
            ?: File(context.filesDir, "Movies")).apply { mkdirs() }

    private fun tempAudioDir(context: Context): File =
        (context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            ?: File(context.filesDir, "Music")).apply { mkdirs() }

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
        val targetDir = if (isAudio) tempAudioDir(context) else tempVideoDir(context)
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

        // Publish into the shared gallery so it appears in Photos/Gallery.
        publishToGallery(context, newFile, isAudio)
    }

    /** Cancels a running download started with the given [processId]. */
    fun cancel(processId: String) {
        runCatching { YoutubeDL.getInstance().destroyProcessById(processId) }
    }

    /**
     * Copies [src] into the shared MediaStore (Movies/Music → VideoDownloader
     * album) so the phone's Gallery scans and shows it. Returns the item.
     */
    private fun publishToGallery(context: Context, src: File, isAudio: Boolean): DownloadedFile {
        val resolver = context.contentResolver
        val collection =
            if (isAudio) MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            else MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val relativePath =
            if (isAudio) "${Environment.DIRECTORY_MUSIC}/$ALBUM"
            else "${Environment.DIRECTORY_MOVIES}/$ALBUM"

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, src.name)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeFor(src, isAudio))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val displayName = src.nameWithoutExtension.replace('_', ' ')
        val size = src.length()

        val mediaUri = runCatching {
            val uri = resolver.insert(collection, values) ?: error("insert returned null")
            resolver.openOutputStream(uri).use { out ->
                requireNotNull(out) { "no output stream" }
                src.inputStream().use { it.copyTo(out) }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                resolver.update(
                    uri,
                    ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) },
                    null, null,
                )
            }
            uri
        }.getOrNull()

        return if (mediaUri != null) {
            src.delete()
            DownloadedFile(mediaUri, displayName, size, System.currentTimeMillis(), isAudio)
        } else {
            // Fallback for old devices without shared-storage access: keep the
            // private copy and expose it via FileProvider so it still plays.
            val uri = FileProvider.getUriForFile(
                context, "${context.packageName}.fileprovider", src,
            )
            DownloadedFile(uri, displayName, size, System.currentTimeMillis(), isAudio)
        }
    }

    /** Lists what this app downloaded (from MediaStore), newest first. */
    suspend fun listDownloads(context: Context): List<DownloadedFile> =
        withContext(Dispatchers.IO) {
            queryAlbum(context, isAudio = false) + queryAlbum(context, isAudio = true)
        }.sortedByDescending { it.dateAddedMs }

    private fun queryAlbum(context: Context, isAudio: Boolean): List<DownloadedFile> {
        val collection =
            if (isAudio) MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            else MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DATE_ADDED,
        )
        // On Q+ filter by our album folder; pre-Q just show the collection.
        val selection: String?
        val args: Array<String>?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            selection = "${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?"
            args = arrayOf("%$ALBUM%")
        } else {
            selection = null
            args = null
        }

        val out = mutableListOf<DownloadedFile>()
        runCatching {
            resolver(context).query(collection, projection, selection, args, null)?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val nameCol = c.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                val sizeCol = c.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
                val dateCol = c.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)
                while (c.moveToNext()) {
                    val id = c.getLong(idCol)
                    out += DownloadedFile(
                        uri = ContentUris.withAppendedId(collection, id),
                        name = (c.getString(nameCol) ?: "download").substringBeforeLast('.').replace('_', ' '),
                        sizeBytes = c.getLong(sizeCol),
                        dateAddedMs = c.getLong(dateCol) * 1000L,
                        isAudio = isAudio,
                    )
                }
            }
        }
        return out
    }

    private fun resolver(context: Context) = context.contentResolver

    private fun mimeFor(file: File, isAudio: Boolean): String = when (file.extension.lowercase()) {
        "mp4", "m4v" -> "video/mp4"
        "webm" -> "video/webm"
        "mkv" -> "video/x-matroska"
        "mp3" -> "audio/mpeg"
        "m4a", "aac" -> "audio/mp4"
        "opus" -> "audio/opus"
        else -> if (isAudio) "audio/*" else "video/*"
    }
}
