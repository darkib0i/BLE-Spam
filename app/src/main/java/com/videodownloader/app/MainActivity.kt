package com.videodownloader.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.videodownloader.app.ui.AppRoot
import com.videodownloader.app.ui.theme.VideoDownloaderTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // A URL can arrive via "Share" from TikTok/YouTube/Instagram etc.
        val sharedUrl = intent
            ?.takeIf { it.action == android.content.Intent.ACTION_SEND }
            ?.getStringExtra(android.content.Intent.EXTRA_TEXT)
            ?.let { extractUrl(it) }

        setContent {
            VideoDownloaderTheme {
                AppRoot(initialUrl = sharedUrl)
            }
        }
    }
}

/** Pulls the first http(s) URL out of shared text (often "caption https://..."). */
private fun extractUrl(text: String): String? =
    Regex("""https?://\S+""").find(text)?.value
