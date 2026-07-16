package com.blespam.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.blespam.app.data.preferences.UserPreferences
import com.blespam.app.permission.readPermissionState
import com.blespam.app.ui.PermissionScreen
import com.blespam.app.ui.SessionViewModel
import com.blespam.app.ui.navigation.BleSpamNavHost
import com.blespam.app.ui.navigation.MainScaffold
import com.blespam.app.ui.settings.SettingsViewModel
import com.blespam.app.ui.theme.BleSpamTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * The single Activity host. Installs the splash screen, resolves the theme from persisted
 * preferences, keeps the shared [SessionViewModel]'s permission snapshot fresh across resumes, and
 * routes between the permission flow and the main navigation graph.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val prefs by settingsViewModel.preferences.collectAsStateWithLifecycle()
            AppRoot(prefs)
        }
    }
}

@Composable
private fun AppRoot(prefs: UserPreferences) {
    BleSpamTheme(
        themeMode = prefs.themeMode,
        accentColor = prefs.accentColor,
        useDynamicColor = prefs.useDynamicColor,
        animationSpeed = prefs.animationSpeed,
    ) {
        val context = LocalContext.current
        val sessionViewModel: SessionViewModel = hiltViewModel()
        val navController = rememberNavController()
        var showPermissions by remember { mutableStateOf(false) }

        // Refresh the permission snapshot every time the app resumes (e.g. returning from Settings).
        LifecycleResumeEffect(Unit) {
            sessionViewModel.updatePermissions(readPermissionState(context))
            onPauseOrDispose { }
        }

        MainScaffold(navController = navController) { padding ->
            BleSpamNavHost(
                navController = navController,
                sessionViewModel = sessionViewModel,
                onRequestPermissions = { showPermissions = true },
                contentPadding = padding,
            )
        }

        // Permission flow slides over everything when requested.
        AnimatedVisibility(
            visible = showPermissions,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize(),
        ) {
            androidx.compose.material3.Surface(
                modifier = Modifier.fillMaxSize(),
                color = androidx.compose.material3.MaterialTheme.colorScheme.background,
            ) {
                PermissionScreen(
                    onAllGranted = {
                        sessionViewModel.updatePermissions(readPermissionState(context))
                        showPermissions = false
                    },
                    onSkip = { showPermissions = false },
                    contentPadding = PaddingValues(),
                )
            }
        }
    }
}
