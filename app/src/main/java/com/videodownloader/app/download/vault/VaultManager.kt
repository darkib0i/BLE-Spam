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

/** One item stored privately inside the app's hidden vault. */
data class VaultItem(
    val file: File,
    val displayName: String,
    val kind: Kind,
    val sizeBytes: Long,
    val lastModified: Long,
) {
    enum class Kind { IMAGE, VIDEO, OTHER }
}

/**
 * A private, in-app file stash. Imported files are *copied* into the app's
 * internal storage, so they keep working here even after the originals are
 * deleted from the gallery, and they're invisible to other apps and the system
 * gallery. Deleting an item only removes the app's private copy.
 */
object VaultManager {

    fun vaultDir(context: Context): File =
        File(context.filesDir, "vault").apply { mkdirs() }

    /** Copies the content behind [uri] into the vault. Returns the new item. */
    suspend fun import(context: Context, uri: Uri): VaultItem? = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val rawName = queryDisplayName(context, uri)
        val mime = resolver.getType(uri)
        val safe = sanitize(rawName ?: "import_${System.currentTimeMillis()}")
        val named = ensureExtension(safe, mime)
        val target = uniqueFile(vaultDir(context), named)

        val ok = runCatching {
            resolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            } != null
        }.getOrDefault(false)

        if (!ok || target.length() == 0L) {
            target.delete()
            return@withContext null
        }
        target.toVaultItem()
    }

    suspend fun list(context: Context): List<VaultItem> = withContext(Dispatchers.IO) {
        vaultDir(context).listFiles()
            ?.filter { it.isFile }
            ?.map { it.toVaultItem() }
            ?.sortedByDescending { it.lastModified }
            ?: emptyList()
    }

    fun delete(item: VaultItem): Boolean = item.file.delete()

    /** Decodes a thumbnail for the grid (down-sampled image, or a video frame). */
    suspend fun thumbnail(item: VaultItem, targetPx: Int = 300): Bitmap? =
        withContext(Dispatchers.IO) {
            runCatching {
                when (item.kind) {
                    VaultItem.Kind.IMAGE -> decodeSampledImage(item.file, targetPx)
                    VaultItem.Kind.VIDEO -> MediaMetadataRetriever().use { r ->
                        r.setDataSource(item.file.absolutePath)
                        r.getFrameAtTime(0)
                    }
                    VaultItem.Kind.OTHER -> null
                }
            }.getOrNull()
        }

    // --- helpers -----------------------------------------------------------

    private fun File.toVaultItem(): VaultItem = VaultItem(
        file = this,
        displayName = nameWithoutExtension.replace('_', ' ').trim().ifBlank { name },
        kind = kindFor(extension),
        sizeBytes = length(),
        lastModified = lastModified(),
    )

    private fun kindFor(ext: String): VaultItem.Kind = when (ext.lowercase()) {
        "jpg", "jpeg", "png", "gif", "webp", "bmp", "heic", "heif" -> VaultItem.Kind.IMAGE
        "mp4", "mkv", "webm", "mov", "avi", "3gp", "m4v", "ts" -> VaultItem.Kind.VIDEO
        else -> VaultItem.Kind.OTHER
    }

    private fun queryDisplayName(context: Context, uri: Uri): String? =
        runCatching {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { c -> if (c.moveToFirst()) c.getString(0) else null }
        }.getOrNull() ?: uri.lastPathSegment?.substringAfterLast('/')

    private fun sanitize(name: String): String =
        name.replace(Regex("""[^A-Za-z0-9._ -]"""), "_").trim().ifBlank { "file" }

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
