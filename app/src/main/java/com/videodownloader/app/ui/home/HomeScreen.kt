package com.videodownloader.app.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.videodownloader.app.download.DownloadStatus
import com.videodownloader.app.download.share.openUri
import com.videodownloader.app.download.share.shareUri
import com.videodownloader.app.ui.components.AnimatedPercentage
import com.videodownloader.app.ui.components.AuroraText
import com.videodownloader.app.ui.components.GlassCard
import com.videodownloader.app.ui.components.GradientButton
import com.videodownloader.app.ui.components.ProgressRing
import com.videodownloader.app.ui.components.QualitySelector
import com.videodownloader.app.ui.components.SecretSearchBar
import com.videodownloader.app.ui.components.SuccessBurst
import com.videodownloader.app.ui.theme.Cyan
import com.videodownloader.app.ui.theme.Lime
import com.videodownloader.app.ui.theme.Magenta
import com.videodownloader.app.ui.theme.Pink
import com.videodownloader.app.ui.theme.TextMuted
import com.videodownloader.app.ui.theme.TextPrimary
import com.videodownloader.app.ui.theme.TextSecondary
import androidx.compose.material3.MaterialTheme

@Composable
fun HomeScreen(
    state: HomeUiState,
    onUrlChange: (String) -> Unit,
    onQualityChange: (com.videodownloader.app.download.Quality) -> Unit,
    onStart: () -> Unit,
    onCancel: () -> Unit,
    onReset: () -> Unit,
    onClearUrl: () -> Unit,
    onOpenVault: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(8.dp))

        // Looks like a plain search bar (top-right). It's the hidden entrance:
        // ten quick taps open the private vault, with no visible hint.
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            SecretSearchBar(
                onUnlock = onOpenVault,
                modifier = Modifier.fillMaxWidth(0.6f),
            )
        }

        Spacer(Modifier.height(8.dp))

        AnimatedVisibility(
            visible = entered,
            enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -it / 2 },
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AuroraText(
                    text = "Video Downloader",
                    style = MaterialTheme.typography.displaySmall,
                )
                Text(
                    text = "TikTok · YouTube · Instagram · X · and more",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        AnimatedVisibility(
            visible = entered,
            enter = fadeIn(tween(700, delayMillis = 120)) + slideInVertically(tween(700, delayMillis = 120)) { it / 3 },
        ) {
            GlassCard(Modifier.fillMaxWidth()) {
                Column {
                    OutlinedTextField(
                        value = state.url,
                        onValueChange = onUrlChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Paste a video link…", color = TextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        trailingIcon = {
                            if (state.url.isNotEmpty()) {
                                IconButton(onClick = onClearUrl) {
                                    Icon(Icons.Rounded.Close, "Clear", tint = TextMuted)
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Magenta,
                            unfocusedBorderColor = TextMuted.copy(alpha = 0.4f),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = Cyan,
                        ),
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PasteChip {
                            clipboard.getText()?.text?.let { onUrlChange(it.trim()) }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "QUALITY",
                        color = TextMuted,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
                    )
                    QualitySelector(selected = state.quality, onSelect = onQualityChange)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // The action zone morphs between idle/progress/success/error.
        AnimatedContent(
            targetState = state.status::class,
            transitionSpec = {
                (fadeIn(tween(400)) + slideInVertically { it / 4 })
                    .togetherWith(fadeOut(tween(200)))
            },
            label = "status",
        ) { _ ->
            when (val status = state.status) {
                is DownloadStatus.Idle -> IdleAction(onStart)
                is DownloadStatus.Error -> ErrorAction(status.message, onStart)
                is DownloadStatus.Preparing -> PreparingAction(onCancel)
                is DownloadStatus.Downloading -> DownloadingAction(status, onCancel)
                is DownloadStatus.Success -> SuccessAction(
                    fileName = status.file.name,
                    onOpen = { openUri(context, status.file.uri, status.file.isAudio) },
                    onShare = { shareUri(context, status.file.uri, status.file.isAudio) },
                    onAnother = onReset,
                )
            }
        }

        Spacer(Modifier.height(120.dp))
    }
}

@Composable
private fun PasteChip(onClick: () -> Unit) {
    androidx.compose.material3.TextButton(onClick = onClick) {
        Icon(Icons.Rounded.ContentPaste, null, tint = Cyan, modifier = Modifier.size(18.dp))
        Text("  Paste from clipboard", color = Cyan)
    }
}

@Composable
private fun IdleAction(onStart: () -> Unit) {
    GradientButton(
        text = "Download",
        icon = Icons.Rounded.Download,
        onClick = onStart,
    )
}

@Composable
private fun ErrorAction(message: String, onRetry: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        GlassCard(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Bolt, null, tint = Pink, modifier = Modifier.size(22.dp))
                Text(
                    message,
                    color = TextPrimary,
                    modifier = Modifier.padding(start = 10.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        GradientButton(text = "Try again", icon = Icons.Rounded.Download, onClick = onRetry)
    }
}

@Composable
private fun PreparingAction(onCancel: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ProgressRing(progress = null) {
            Icon(Icons.Rounded.Bolt, null, tint = Cyan, modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text("Preparing your download…", color = TextSecondary)
        Spacer(Modifier.height(16.dp))
        CancelButton(onCancel)
    }
}

@Composable
private fun DownloadingAction(status: DownloadStatus.Downloading, onCancel: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ProgressRing(progress = status.progress) {
            AnimatedPercentage(fraction = status.progress)
        }
        Spacer(Modifier.height(12.dp))
        AnimatedContent(targetState = status.line, label = "line") { line ->
            Text(
                text = line.ifBlank { "Downloading…" },
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            )
        }
        if (status.etaSeconds > 0) {
            Text("ETA ${status.etaSeconds}s", color = TextMuted, style = MaterialTheme.typography.labelSmall)
        }
        Spacer(Modifier.height(16.dp))
        CancelButton(onCancel)
    }
}

@Composable
private fun SuccessAction(
    fileName: String,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onAnother: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        SuccessBurst()
        Text("Saved to your gallery", color = Lime, style = MaterialTheme.typography.titleMedium)
        Text(
            fileName,
            color = TextSecondary,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp, start = 16.dp, end = 16.dp),
        )
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PillButton("Open", Icons.AutoMirrored.Rounded.OpenInNew, onOpen)
            PillButton("Share", Icons.Rounded.Share, onShare)
        }
        Spacer(Modifier.height(16.dp))
        GradientButton(text = "Download another", icon = Icons.Rounded.Download, onClick = onAnother)
    }
}

@Composable
private fun CancelButton(onCancel: () -> Unit) {
    androidx.compose.material3.OutlinedButton(onClick = onCancel) {
        Icon(Icons.Rounded.Close, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
        Text("  Cancel", color = TextSecondary)
    }
}

@Composable
private fun PillButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    GlassCard(cornerRadius = 18.dp, contentPadding = 0.dp) {
        androidx.compose.material3.TextButton(
            onClick = onClick,
            modifier = Modifier.padding(horizontal = 8.dp),
        ) {
            Icon(icon, null, tint = Cyan, modifier = Modifier.size(18.dp))
            Text("  $text", color = TextPrimary)
        }
    }
}
