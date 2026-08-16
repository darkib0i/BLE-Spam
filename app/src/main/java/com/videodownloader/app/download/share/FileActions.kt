package com.videodownloader.app.download.share

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

/** Opens a MediaStore (gallery) item — used for finished downloads. */
fun openUri(context: Context, uri: Uri, isAudio: Boolean) {
    runCatching {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, if (isAudio) "audio/*" else "video/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }.onFailure {
        Toast.makeText(context, "No app can open this file.", Toast.LENGTH_SHORT).show()
    }
}

/** Shares a MediaStore (gallery) item — used for finished downloads. */
fun shareUri(context: Context, uri: Uri, isAudio: Boolean) {
    runCatching {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = if (isAudio) "audio/*" else "video/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share via").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }.onFailure {
        Toast.makeText(context, "Couldn't share this file.", Toast.LENGTH_SHORT).show()
    }
}

private fun uriFor(context: Context, file: File) = FileProvider.getUriForFile(
    context,
    "${context.packageName}.fileprovider",
    file,
)

private fun mimeOf(file: File): String = when (file.extension.lowercase()) {
    "mp3", "m4a", "opus", "aac", "wav", "flac" -> "audio/*"
    else -> "video/*"
}

/** Opens a downloaded file in the user's preferred player. */
fun openFile(context: Context, file: File) {
    runCatching {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uriFor(context, file), mimeOf(file))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }.onFailure {
        Toast.makeText(context, "No app can open this file.", Toast.LENGTH_SHORT).show()
    }
}

/** Shares a downloaded file to another app. */
fun shareFile(context: Context, file: File) {
    runCatching {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeOf(file)
            putExtra(Intent.EXTRA_STREAM, uriFor(context, file))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share via").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }.onFailure {
        Toast.makeText(context, "Couldn't share this file.", Toast.LENGTH_SHORT).show()
    }
}
