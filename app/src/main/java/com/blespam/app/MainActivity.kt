package com.blespam.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.blespam.app.ui.BleSpamApp
import com.blespam.app.ui.settings.SettingsViewModel
import com.blespam.app.ui.theme.BleSpamTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * The single Activity host. All UI is Compose; navigation is handled inside
 * [BleSpamApp]. The Activity draws edge-to-edge and hands the current theme
 * settings to [BleSpamTheme] so theme changes apply instantly and app-wide.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Branded splash while the first frame is prepared.
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
            BleSpamTheme(
                themeMode = settings.themeMode,
                accent = settings.accentColor,
                dynamicColor = settings.dynamicColor,
                amoled = settings.amoled,
            ) {
                BleSpamApp(
                    animationsEnabled = settings.animationsEnabled,
                    animationSpeed = settings.animationSpeed,
                )
            }
        }
    }
}
