package com.videodownloader.app.ui.vault

import android.graphics.Bitmap
import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.videodownloader.app.download.share.openFile
import com.videodownloader.app.download.vault.VaultItem
import com.videodownloader.app.download.vault.VaultManager
import com.videodownloader.app.ui.theme.Cyan
import com.videodownloader.app.ui.theme.NeonSweep
import com.videodownloader.app.ui.theme.Night
import com.videodownloader.app.ui.theme.Pink
import com.videodownloader.app.ui.theme.TextMuted
import com.videodownloader.app.ui.theme.TextPrimary
import com.videodownloader.app.ui.theme.TextSecondary

@Composable
fun HiddenVaultScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VaultViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var viewing by remember { mutableStateOf<VaultItem?>(null) }

    BackHandler(enabled = viewing != null) { viewing = null }
    BackHandler(enabled = viewing == null) { onClose() }

    val mediaPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(30),
    ) { uris -> viewModel.importAll(uris) }

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments(),
    ) { uris -> viewModel.importAll(uris) }

    Box(
        modifier
            .fillMaxSize()
            .background(Night)
            .statusBarsPadding(),
    ) {
        Column(Modifier.fillMaxSize()) {
            // Header.
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.Lock, null, tint = Cyan, modifier = Modifier.size(22.dp))
                Text(
                    "  Private Vault",
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Rounded.Close, "Close", tint = TextSecondary)
                }
            }

            Text(
                "Files here are stored privately inside the app. You can delete the originals from your gallery and still view them here.",
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            )

            // Import actions.
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ImportButton(
                    text = "Gallery",
                    icon = Icons.Rounded.PhotoLibrary,
                    modifier = Modifier.weight(1f),
                ) {
                    mediaPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo),
                    )
                }
                ImportButton(
                    text = "Files",
                    icon = Icons.Rounded.Add,
                    modifier = Modifier.weight(1f),
                ) {
                    filePicker.launch(arrayOf("*/*"))
                }
            }

            if (state.items.isEmpty()) {
                EmptyVault()
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(state.items, key = { it.file.absolutePath }) { item ->
                        VaultCell(
                            item = item,
                            onOpen = { viewing = item },
                            onDelete = { viewModel.delete(item) },
                        )
                    }
                }
            }
        }

        // Full-screen viewer.
        AnimatedVisibility(
            visible = viewing != null,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            viewing?.let { item ->
                VaultViewer(item = item, onClose = { viewing = null })
            }
        }
    }
}

@Composable
private fun ImportButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.horizontalGradient(NeonSweep))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
        Text("  $text", color = Color.White)
    }
}

@Composable
private fun VaultCell(item: VaultItem, onOpen: () -> Unit, onDelete: () -> Unit) {
    val thumb by produceState<Bitmap?>(initialValue = null, item) {
        value = VaultManager.thumbnail(item)
    }

    Box(
        Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .clickable(onClick = onOpen),
    ) {
        val bmp = thumb
        if (bmp != null) {
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = item.displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    when (item.kind) {
                        VaultItem.Kind.VIDEO -> Icons.Rounded.PlayCircle
                        VaultItem.Kind.IMAGE -> Icons.Rounded.PhotoLibrary
                        VaultItem.Kind.OTHER -> Icons.Rounded.InsertDriveFile
                    },
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(34.dp),
                )
            }
        }

        // Play badge for videos.
        if (item.kind == VaultItem.Kind.VIDEO && bmp != null) {
            Icon(
                Icons.Rounded.PlayCircle,
                null,
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(38.dp),
            )
        }

        // Delete affordance.
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable(onClick = onDelete)
                .padding(4.dp),
        ) {
            Icon(Icons.Rounded.Delete, "Delete", tint = Pink, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun VaultViewer(item: VaultItem, onClose: () -> Unit) {
    val context = LocalContext.current
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.96f))
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        when (item.kind) {
            VaultItem.Kind.IMAGE -> {
                val bmp by produceState<Bitmap?>(initialValue = null, item) {
                    value = VaultManager.thumbnail(item, targetPx = 1600)
                }
                bmp?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = item.displayName,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                    )
                }
            }
            VaultItem.Kind.VIDEO -> {
                AndroidView(
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            setVideoURI(Uri.fromFile(item.file))
                            val controller = MediaController(ctx)
                            controller.setAnchorView(this)
                            setMediaController(controller)
                            setOnPreparedListener { mp -> mp.isLooping = true; start() }
                        }
                    },
                )
            }
            VaultItem.Kind.OTHER -> {
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(Icons.Rounded.InsertDriveFile, null, tint = TextSecondary, modifier = Modifier.size(72.dp))
                    Spacer(Modifier.height(12.dp))
                    Text(item.displayName, color = TextPrimary, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(16.dp))
                    ImportButton(text = "Open externally", icon = Icons.Rounded.Add) {
                        openFile(context, item.file)
                    }
                }
            }
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
        ) {
            Icon(Icons.Rounded.Close, "Close", tint = Color.White, modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
private fun EmptyVault() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Rounded.Lock, null, tint = TextMuted, modifier = Modifier.size(56.dp))
            Spacer(Modifier.height(12.dp))
            Text("Vault is empty", color = TextSecondary, style = MaterialTheme.typography.titleMedium)
            Text(
                "Import photos, videos or files to keep them here privately.",
                color = TextMuted,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 40.dp),
            )
        }
    }
}
