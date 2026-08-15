package com.videodownloader.app.ui.library

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MovieCreation
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.videodownloader.app.download.DownloadedFile
import com.videodownloader.app.download.share.openFile
import com.videodownloader.app.download.share.shareFile
import com.videodownloader.app.ui.components.AuroraText
import com.videodownloader.app.ui.components.GlassCard
import com.videodownloader.app.ui.theme.Cyan
import com.videodownloader.app.ui.theme.NeonSweep
import com.videodownloader.app.ui.theme.TextMuted
import com.videodownloader.app.ui.theme.TextPrimary
import com.videodownloader.app.ui.theme.TextSecondary
import java.text.DecimalFormat

@Composable
fun LibraryScreen(
    files: List<DownloadedFile>,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { onRefresh() }

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(12.dp))
        AuroraText("Library", style = MaterialTheme.typography.headlineMedium)
        Text(
            "${files.size} download${if (files.size == 1) "" else "s"}",
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp),
        )

        if (files.isEmpty()) {
            EmptyLibrary()
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 120.dp),
            ) {
                items(files, key = { it.file.absolutePath }) { file ->
                    LibraryRow(
                        file = file,
                        onOpen = { openFile(context, file.file) },
                        onShare = { shareFile(context, file.file) },
                    )
                }
            }
        }
    }
}

@Composable
private fun LibraryRow(file: DownloadedFile, onOpen: () -> Unit, onShare: () -> Unit) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }

    AnimatedVisibility(
        visible = shown,
        enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 4 },
    ) {
        GlassCard(Modifier.fillMaxWidth(), cornerRadius = 22.dp, contentPadding = 14.dp) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(NeonSweep)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        if (file.isAudio) Icons.Rounded.LibraryMusic else Icons.Rounded.MovieCreation,
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Column(
                    Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                        .clickable(onClick = onOpen),
                ) {
                    Text(
                        file.name,
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        formatSize(file.sizeBytes),
                        color = TextMuted,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                IconButton(onClick = onOpen) {
                    Icon(Icons.AutoMirrored.Rounded.OpenInNew, "Open", tint = Cyan)
                }
                IconButton(onClick = onShare) {
                    Icon(Icons.Rounded.Share, "Share", tint = Cyan)
                }
            }
        }
    }
}

@Composable
private fun EmptyLibrary() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Rounded.VideoLibrary,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(64.dp),
            )
            Spacer(Modifier.height(12.dp))
            Text("No downloads yet", color = TextSecondary, style = MaterialTheme.typography.titleMedium)
            Text(
                "Videos you grab will appear here.",
                color = TextMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

private fun formatSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    var value = bytes.toDouble()
    var unit = 0
    while (value >= 1024 && unit < units.lastIndex) {
        value /= 1024
        unit++
    }
    return "${DecimalFormat("#.#").format(value)} ${units[unit]}"
}
