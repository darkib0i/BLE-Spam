package com.videodownloader.app.download.vault

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** A single entry inside the vault: either a sub-folder or a stored file. */
data class VaultEntry(
    val file: File,
    val name: String,
    val isFolder: Boolean,
    val kind: Kind,
    val sizeBytes: Long,
    val lastModified: Long,
    val childCount: Int,
) {
    enum class Kind { IMAGE, VIDEO, OTHER, FOLDER }
}

/**
 * A private, in-app file stash that supports nested folders. Imported files are
 * *copied* into the app's internal storage, so they keep working here even
 * after the originals are deleted from the gallery, and they're invisible to
 * other apps and the system gallery. Deleting an entry removes only the app's
 * private copy (folders are removed with their contents).
 */
object VaultManager {

    fun rootDir(context: Context): File =
        File(context.filesDir, "vault").apply { mkdirs() }

    fun list(dir: File): List<VaultEntry> =
        dir.listFiles()
            ?.map { it.toEntry() }
            ?.sortedWith(
                compareByDescending<VaultEntry> { it.isFolder }
                    .thenByDescending { it.lastModified },
            )
            ?: emptyList()

    /** Creates a new (uniquely named) folder under [parent]. */
    fun createFolder(parent: File, rawName: String): File? {
        val safe = sanitize(rawName)
        val dir = uniqueFile(parent, safe)
        return if (dir.mkdirs() || dir.isDirectory) dir else null
    }

    /** Copies the content behind [uri] into [dir]. Returns the new entry. */
    suspend fun import(context: Context, uri: Uri, dir: File): VaultEntry? =
        withContext(Dispatchers.IO) {
            val resolver = context.contentResolver
            val rawName = queryDisplayName(context, uri)
            val mime = resolver.getType(uri)
            val safe = sanitize(rawName ?: "import_${System.currentTimeMillis()}")
            val named = ensureExtension(safe, mime)
            val target = uniqueFile(dir, named)

            val ok = runCatching {
                resolver.openInputStream(uri)?.use { input ->
                    target.outputStream().use { output -> input.copyTo(output) }
                } != null
            }.getOrDefault(false)

            if (!ok || target.length() == 0L) {
                target.delete()
                return@withContext null
            }
            target.toEntry()
        }

    fun delete(entry: VaultEntry): Boolean =
        if (entry.isFolder) entry.file.deleteRecursively() else entry.file.delete()

    /** Decodes a thumbnail for a file entry (down-sampled image or video frame). */
    suspend fun thumbnail(entry: VaultEntry, targetPx: Int = 300): Bitmap? =
        withContext(Dispatchers.IO) {
            runCatching {
                when (entry.kind) {
                    VaultEntry.Kind.IMAGE -> decodeSampledImage(entry.file, targetPx)
                    VaultEntry.Kind.VIDEO -> MediaMetadataRetriever().use { r ->
                        r.setDataSource(entry.file.absolutePath)
                        r.getFrameAtTime(0)
                    }
                    else -> null
                }
            }.getOrNull()
        }

    // --- helpers -----------------------------------------------------------

    private fun File.toEntry(): VaultEntry {
        val folder = isDirectory
        return VaultEntry(
            file = this,
            name = if (folder) name else nameWithoutExtension.replace('_', ' ').trim().ifBlank { name },
            isFolder = folder,
            kind = if (folder) VaultEntry.Kind.FOLDER else kindFor(extension),
            sizeBytes = if (folder) 0L else length(),
            lastModified = lastModified(),
            childCount = if (folder) (listFiles()?.size ?: 0) else 0,
        )
    }

    private fun kindFor(ext: String): VaultEntry.Kind = when (ext.lowercase()) {
        "jpg", "jpeg", "png", "gif", "webp", "bmp", "heic", "heif" -> VaultEntry.Kind.IMAGE
        "mp4", "mkv", "webm", "mov", "avi", "3gp", "m4v", "ts" -> VaultEntry.Kind.VIDEO
        else -> VaultEntry.Kind.OTHER
    }

    private fun queryDisplayName(context: Context, uri: Uri): String? =
        runCatching {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { c -> if (c.moveToFirst()) c.getString(0) else null }
        }.getOrNull() ?: uri.lastPathSegment?.substringAfterLast('/')

    private fun sanitize(name: String): String =
        name.replace(Regex("""[^A-Za-z0-9._ -]"""), "_").trim().ifBlank { "untitled" }

    private fun ensureExtension(name: String, mime: String?): String {
        if (name.contains('.')) return name
        val ext = when {
            mime == null -> null
            mime.startsWith("image/") -> mime.removePrefix("image/").let { if (it == "jpeg") "jpg" else it }
            mime.startsWith("video/") -> mime.removePrefix("video/")
            else -> null
        }
        return if (ext != null) "$name.$ext" else name
    }

    private fun uniqueFile(dir: File, name: String): File {
        var candidate = File(dir, name)
        if (!candidate.exists()) return candidate
        val base = name.substringBeforeLast('.', name)
        val ext = name.substringAfterLast('.', "")
        var i = 1
        while (candidate.exists()) {
            val suffix = if (ext.isEmpty()) "${base}_$i" else "${base}_$i.$ext"
            candidate = File(dir, suffix)
            i++
        }
        return candidate
    }

    private fun decodeSampledImage(file: File, targetPx: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        var sample = 1
        val longest = maxOf(bounds.outWidth, bounds.outHeight)
        while (longest / sample > targetPx * 2) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        return BitmapFactory.decodeFile(file.absolutePath, opts)
    }

    private inline fun <T> MediaMetadataRetriever.use(block: (MediaMetadataRetriever) -> T): T =
        try {
            block(this)
        } finally {
            runCatching { release() }
        }
}
