package com.videodownloader.app.ui.vault

import android.graphics.Bitmap
import android.net.Uri
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
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DriveFileRenameOutline
import androidx.compose.material.icons.rounded.DriveFolderUpload
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Slideshow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.videodownloader.app.download.share.openFile
import com.videodownloader.app.download.vault.LikeStore
import com.videodownloader.app.download.vault.VaultEntry
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
    // PIN gate: the vault contents stay hidden until the code is entered.
    // Using remember (not saveable) so it re-locks every time it's reopened.
    var authed by remember { mutableStateOf(false) }
    if (!authed) {
        PinGate(
            onSuccess = { decoy ->
                viewModel.openVault(decoy)
                authed = true
            },
            onClose = onClose,
            modifier = modifier,
        )
        return
    }

    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    // The swipeable viewer pages through images + videos of the current folder.
    val mediaEntries = remember(state.entries) {
        state.entries.filter { it.kind == VaultEntry.Kind.IMAGE || it.kind == VaultEntry.Kind.VIDEO }
    }
    var pagerIndex by remember { mutableStateOf<Int?>(null) }
    var pendingDelete by remember { mutableStateOf<VaultEntry?>(null) }
    var renaming by remember { mutableStateOf<VaultEntry?>(null) }
    var showNewFolder by remember { mutableStateOf(false) }
    var showTimeline by remember { mutableStateOf(false) }
    var timelineLikedOnly by remember { mutableStateOf(false) }

    BackHandler {
        when {
            showTimeline -> showTimeline = false
            pagerIndex != null -> pagerIndex = null
            !state.atRoot -> viewModel.goUp()
            else -> onClose()
        }
    }

    val mediaPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(30),
    ) { uris -> viewModel.importAll(uris) }

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments(),
    ) { uris -> viewModel.importAll(uris) }

    val folderPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree(),
    ) { uri -> uri?.let { viewModel.importTree(it) } }

    Box(
        modifier
            .fillMaxSize()
            .background(Night)
            .statusBarsPadding(),
    ) {
        Column(Modifier.fillMaxSize()) {
            // Header with breadcrumb + up/close.
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (state.atRoot) {
                    Icon(Icons.Rounded.Lock, null, tint = Cyan, modifier = Modifier.size(22.dp).padding(start = 4.dp))
                } else {
                    IconButton(onClick = { viewModel.goUp() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Up", tint = TextPrimary)
                    }
                }
                Text(
                    "  ${state.breadcrumb}",
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(start = 4.dp),
                )
                IconButton(onClick = {
                    timelineLikedOnly = false
                    showTimeline = true
                }) {
                    Icon(Icons.Rounded.Slideshow, "Timeline", tint = Cyan)
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Rounded.Close, "Close", tint = TextSecondary)
                }
            }

            Text(
                "Private, in-app storage. Make folders, import from your gallery or files, and it all stays here even after you delete the originals.",
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 2.dp),
            )

            // Import + folder actions.
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ActionButton("Gallery", Icons.Rounded.PhotoLibrary, Modifier.weight(1f)) {
                    mediaPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo),
                    )
                }
                ActionButton("Files", Icons.Rounded.Add, Modifier.weight(1f)) {
                    filePicker.launch(arrayOf("*/*"))
                }
                ActionButton("Folder", Icons.Rounded.DriveFolderUpload, Modifier.weight(1f)) {
                    folderPicker.launch(null)
                }
                ActionButton("New", Icons.Rounded.CreateNewFolder, Modifier.weight(1f)) {
                    showNewFolder = true
                }
            }

            if (state.entries.isEmpty()) {
                EmptyState()
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(state.entries, key = { it.file.absolutePath }) { entry ->
                        VaultCell(
                            entry = entry,
                            onOpen = {
                                when {
                                    entry.isFolder -> viewModel.openFolder(entry.file)
                                    entry.kind == VaultEntry.Kind.IMAGE || entry.kind == VaultEntry.Kind.VIDEO -> {
                                        val idx = mediaEntries.indexOfFirst { it.file == entry.file }
                                        if (idx >= 0) pagerIndex = idx
                                    }
                                    else -> openFile(context, entry.file)
                                }
                            },
                            onRename = { renaming = entry },
                            onDelete = { pendingDelete = entry },
                        )
                    }
                }
            }
        }

        // Full-screen, swipeable (TikTok-style) media viewer for the folder.
        AnimatedVisibility(visible = pagerIndex != null, enter = fadeIn(), exit = fadeOut()) {
            pagerIndex?.let { start ->
                VaultPager(
                    entries = mediaEntries,
                    startIndex = start,
                    onClose = { pagerIndex = null },
                    onDelete = { entry ->
                        pagerIndex = null
                        pendingDelete = entry
                    },
                )
            }
        }

        // Timeline: a feed of ALL media in the vault (recursively), TikTok-style.
        AnimatedVisibility(visible = showTimeline, enter = fadeIn(), exit = fadeOut()) {
            val timelineMedia by produceState(
                initialValue = emptyList<VaultEntry>(),
                showTimeline, timelineLikedOnly, state.entries,
            ) {
                value = withContext(Dispatchers.IO) {
                    val all = VaultManager.listAllMedia(viewModel.currentRootDir())
                    if (timelineLikedOnly) {
                        all.filter { LikeStore.isLiked(context, it.file.absolutePath) }
                    } else {
                        all
                    }
                }
            }

            if (timelineMedia.isEmpty()) {
                Box(
                    Modifier.fillMaxSize().background(Color.Black).statusBarsPadding(),
                    contentAlignment = Alignment.Center,
                ) {
                    IconButton(
                        onClick = { showTimeline = false },
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                    ) { Icon(Icons.Rounded.Close, "Close", tint = Color.White) }
                    Text(
                        if (timelineLikedOnly) "No liked items yet" else "No photos or videos yet",
                        color = TextSecondary,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            } else {
                key(timelineLikedOnly) {
                    VaultPager(
                        entries = timelineMedia,
                        startIndex = 0,
                        onClose = { showTimeline = false },
                        onDelete = { entry -> pendingDelete = entry },
                        likedFilterActive = timelineLikedOnly,
                        onToggleLikedFilter = { timelineLikedOnly = !timelineLikedOnly },
                    )
                }
            }
        }
    }

    // New-folder dialog.
    if (showNewFolder) {
        NewFolderDialog(
            onConfirm = { name ->
                viewModel.createFolder(name)
                showNewFolder = false
            },
            onDismiss = { showNewFolder = false },
        )
    }

    // Rename dialog.
    renaming?.let { entry ->
        RenameDialog(
            entry = entry,
            onConfirm = { name ->
                viewModel.rename(entry, name)
                renaming = null
            },
            onDismiss = { renaming = null },
        )
    }

    // Delete confirmation.
    pendingDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(if (entry.isFolder) "Delete folder?" else "Delete file?") },
            text = {
                Text(
                    if (entry.isFolder)
                        "\"${entry.name}\" and everything inside it will be permanently removed."
                    else
                        "\"${entry.name}\" will be permanently removed from the vault.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(entry)
                    pendingDelete = null
                }) { Text("Delete", color = Pink) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel", color = TextSecondary) }
            },
        )
    }
}

@Composable
private fun PinGate(
    onSuccess: (decoy: Boolean) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Real PIN opens the real vault; the decoy PIN opens a separate empty one.
    val realPin = "4855"
    val decoyPin = "4845"
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Box(
        modifier
            .fillMaxSize()
            .background(Night)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        IconButton(onClick = onClose, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
            Icon(Icons.Rounded.Close, "Close", tint = TextSecondary)
        }
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(Icons.Rounded.Lock, null, tint = Cyan, modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(12.dp))
            Text("Enter PIN", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(4) { i ->
                    val filled = i < pin.length
                    Box(
                        Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    error -> Pink
                                    filled -> Color.White
                                    else -> Color.White.copy(alpha = 0.2f)
                                },
                            ),
                    )
                }
            }
            if (error) {
                Spacer(Modifier.height(8.dp))
                Text("Wrong PIN", color = Pink, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(28.dp))

            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "⌫"),
            )
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                rows.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        row.forEach { key ->
                            PinKey(key) {
                                error = false
                                when (key) {
                                    "" -> Unit
                                    "⌫" -> if (pin.isNotEmpty()) pin = pin.dropLast(1)
                                    else -> if (pin.length < 4) {
                                        pin += key
                                        if (pin.length == 4) {
                                            when (pin) {
                                                realPin -> onSuccess(false)
                                                decoyPin -> onSuccess(true)
                                                else -> {
                                                    error = true
                                                    pin = ""
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PinKey(label: String, onClick: () -> Unit) {
    if (label.isEmpty()) {
        Box(Modifier.size(68.dp))
        return
    }
    Box(
        Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.06f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (label == "⌫") {
            Icon(Icons.AutoMirrored.Rounded.Backspace, "delete", tint = TextPrimary, modifier = Modifier.size(24.dp))
        } else {
            Text(label, color = TextPrimary, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.horizontalGradient(NeonSweep))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
        Text(text, color = Color.White, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun VaultCell(
    entry: VaultEntry,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    Box(
        Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .clickable(onClick = onOpen),
    ) {
        if (entry.isFolder) {
            Column(
                Modifier.fillMaxSize().padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(Icons.Rounded.Folder, null, tint = Cyan, modifier = Modifier.size(46.dp))
                Text(
                    entry.name,
                    color = TextPrimary,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
                Text(
                    "${entry.childCount} item${if (entry.childCount == 1) "" else "s"}",
                    color = TextMuted,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        } else {
            val thumb by produceState<Bitmap?>(initialValue = null, entry) {
                value = VaultManager.thumbnail(entry)
            }
            val bmp = thumb
            if (bmp != null) {
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = entry.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                if (entry.kind == VaultEntry.Kind.VIDEO) {
                    Icon(
                        Icons.Rounded.PlayCircle, null, tint = Color.White,
                        modifier = Modifier.align(Alignment.Center).size(38.dp),
                    )
                }
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        when (entry.kind) {
                            VaultEntry.Kind.VIDEO -> Icons.Rounded.PlayCircle
                            VaultEntry.Kind.IMAGE -> Icons.Rounded.PhotoLibrary
                            else -> Icons.Rounded.InsertDriveFile
                        },
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(34.dp),
                    )
                }
            }
        }

        // Rename affordance.
        Box(
            Modifier
                .align(Alignment.TopStart)
                .padding(4.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable(onClick = onRename)
                .padding(4.dp),
        ) {
            Icon(Icons.Rounded.Edit, "Rename", tint = Color.White, modifier = Modifier.size(18.dp))
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
private fun VaultPager(
    entries: List<VaultEntry>,
    startIndex: Int,
    onClose: () -> Unit,
    onDelete: (VaultEntry) -> Unit,
    likedFilterActive: Boolean = false,
    onToggleLikedFilter: (() -> Unit)? = null,
) {
    if (entries.isEmpty()) return
    val context = LocalContext.current
    val pagerState = rememberPagerState(
        initialPage = startIndex.coerceIn(0, entries.lastIndex),
    ) { entries.size }

    // Local like cache (falls back to persisted store), plus a double-tap burst.
    val likeState = remember { mutableStateMapOf<String, Boolean>() }
    fun liked(e: VaultEntry) = likeState[e.file.absolutePath] ?: LikeStore.isLiked(context, e.file.absolutePath)
    fun toggleLike(e: VaultEntry) {
        val next = !liked(e)
        likeState[e.file.absolutePath] = next
        LikeStore.setLiked(context, e.file.absolutePath, next)
    }

    var burstAt by remember { mutableStateOf(0L) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        VerticalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            val entry = entries[page]
            val active = pagerState.currentPage == page
            Box(
                Modifier
                    .fillMaxSize()
                    .pointerInput(entry) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (!liked(entry)) toggleLike(entry)
                                burstAt = System.currentTimeMillis()
                            },
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (entry.kind == VaultEntry.Kind.VIDEO) {
                    PagerVideo(entry = entry, active = active)
                } else {
                    val bmp by produceState<Bitmap?>(initialValue = null, entry) {
                        value = VaultManager.thumbnail(entry, targetPx = 1600)
                    }
                    bmp?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = entry.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }

        val current = entries.getOrNull(pagerState.currentPage)

        // Double-tap heart burst.
        HeartBurst(trigger = burstAt)

        // Top overlay: close, name, optional liked-filter, delete.
        Row(
            Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClose) { Icon(Icons.Rounded.Close, "Close", tint = Color.White) }
            Text(
                current?.name.orEmpty(),
                color = Color.White,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(horizontal = 6.dp),
            )
            if (onToggleLikedFilter != null) {
                IconButton(onClick = onToggleLikedFilter) {
                    Icon(
                        if (likedFilterActive) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        "Liked only",
                        tint = Color.White,
                    )
                }
            }
            IconButton(onClick = { current?.let(onDelete) }) {
                Icon(Icons.Rounded.Delete, "Delete", tint = Color.White)
            }
        }

        // Right-side like button (TikTok-style).
        current?.let { entry ->
            val isLiked = liked(entry)
            Column(
                Modifier.align(Alignment.CenterEnd).padding(end = 10.dp, bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                IconButton(onClick = { toggleLike(entry) }) {
                    Icon(
                        if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Like",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp),
                    )
                }
                Text(
                    if (isLiked) "Liked" else "Like",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }

        Text(
            "${pagerState.currentPage + 1} / ${entries.size}   ·   swipe",
            color = Color.White.copy(alpha = 0.65f),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
        )
    }
}

/** A big heart that pops and fades on a double-tap like. */
@Composable
private fun HeartBurst(trigger: Long) {
    if (trigger == 0L) return
    val scale = remember(trigger) { Animatable(0.5f) }
    val alpha = remember(trigger) { Animatable(0.95f) }
    LaunchedEffect(trigger) {
        launch { scale.animateTo(1.35f, tween(450)) }
        alpha.animateTo(0f, tween(750))
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Icon(
            Icons.Rounded.Favorite,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .size(130.dp)
                .scale(scale.value)
                .graphicsLayer { this.alpha = alpha.value },
        )
    }
}

@Composable
private fun PagerVideo(entry: VaultEntry, active: Boolean) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            VideoView(ctx).apply {
                setVideoURI(Uri.fromFile(entry.file))
                setOnPreparedListener { mp ->
                    mp.isLooping = true
                    if (active) start()
                }
            }
        },
        update = { view ->
            if (active) {
                if (!view.isPlaying) view.start()
            } else if (view.isPlaying) {
                view.pause()
                view.seekTo(0)
            }
        },
    )
}

@Composable
private fun NewFolderDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New folder") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                placeholder = { Text("Folder name", color = TextMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Cyan,
                    unfocusedBorderColor = TextMuted,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = Cyan,
                ),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim()) },
                enabled = name.isNotBlank(),
            ) { Text("Create", color = Cyan) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        },
    )
}

@Composable
private fun RenameDialog(
    entry: VaultEntry,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(entry.name) }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.DriveFileRenameOutline, null, tint = Cyan) },
        title = { Text(if (entry.isFolder) "Rename folder" else "Rename file") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                placeholder = { Text("Name", color = TextMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Cyan,
                    unfocusedBorderColor = TextMuted,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = Cyan,
                ),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name.trim()) }, enabled = name.isNotBlank()) {
                Text("Rename", color = Cyan)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        },
    )
}

@Composable
private fun EmptyState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Rounded.Folder, null, tint = TextMuted, modifier = Modifier.size(56.dp))
            Spacer(Modifier.height(12.dp))
            Text("Nothing here yet", color = TextSecondary, style = MaterialTheme.typography.titleMedium)
            Text(
                "Make a folder or import photos, videos and files.",
                color = TextMuted,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 40.dp),
                textAlign = TextAlign.Center,
            )
        }
    }
}
