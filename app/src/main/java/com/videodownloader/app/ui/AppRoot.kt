package com.videodownloader.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.videodownloader.app.ui.about.AboutScreen
import com.videodownloader.app.ui.components.AnimatedBottomBar
import com.videodownloader.app.ui.components.AnimatedGradientBackground
import com.videodownloader.app.ui.components.BottomTab
import com.videodownloader.app.ui.home.DownloadViewModel
import com.videodownloader.app.ui.home.HomeScreen
import com.videodownloader.app.ui.library.LibraryScreen
import com.videodownloader.app.ui.vault.HiddenVaultScreen

@Composable
fun AppRoot(initialUrl: String?) {
    val viewModel: DownloadViewModel = viewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(initialUrl) {
        if (!initialUrl.isNullOrBlank()) viewModel.onUrlChange(initialUrl)
    }

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showVault by rememberSaveable { mutableStateOf(false) }
    val tabs = remember {
        listOf(
            BottomTab("Download", Icons.Rounded.Download),
            BottomTab("Library", Icons.Rounded.VideoLibrary),
            BottomTab("About", Icons.Rounded.Info),
        )
    }

    Box(Modifier.fillMaxSize()) {
        AnimatedGradientBackground()

        Box(
            Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    val forward = targetState > initialState
                    val dir = if (forward) 1 else -1
                    (slideInHorizontally(tween(400)) { dir * it / 2 } + fadeIn(tween(400)))
                        .togetherWith(slideOutHorizontally(tween(400)) { -dir * it / 2 } + fadeOut(tween(250)))
                        .using(SizeTransform(clip = false))
                },
                label = "tabs",
                modifier = Modifier.fillMaxSize(),
            ) { tab ->
                when (tab) {
                    0 -> HomeScreen(
                        state = state,
                        onUrlChange = viewModel::onUrlChange,
                        onQualityChange = viewModel::onQualityChange,
                        onStart = viewModel::start,
                        onCancel = viewModel::cancel,
                        onReset = viewModel::reset,
                        onClearUrl = viewModel::clearUrl,
                        onOpenVault = { showVault = true },
                    )
                    1 -> LibraryScreen(files = state.library, onRefresh = viewModel::refreshLibrary)
                    else -> AboutScreen()
                }
            }
        }

        AnimatedBottomBar(
            tabs = tabs,
            selectedIndex = selectedTab,
            onSelect = { selectedTab = it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
        )

        // Hidden vault sits above the whole app when unlocked.
        AnimatedVisibility(
            visible = showVault,
            enter = fadeIn(tween(250)),
            exit = fadeOut(tween(200)),
        ) {
            HiddenVaultScreen(onClose = { showVault = false })
        }
    }
}
