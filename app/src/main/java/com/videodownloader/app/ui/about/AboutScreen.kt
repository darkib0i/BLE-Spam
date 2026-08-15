package com.videodownloader.app.ui.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.videodownloader.app.ui.components.AuroraText
import com.videodownloader.app.ui.components.GlassCard
import com.videodownloader.app.ui.theme.Cyan
import com.videodownloader.app.ui.theme.TextMuted
import com.videodownloader.app.ui.theme.TextPrimary
import com.videodownloader.app.ui.theme.TextSecondary

@Composable
fun AboutScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(12.dp))
        AuroraText("About", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        Feature(Icons.Rounded.Public, "Any platform", "Powered by yt-dlp — works across 1,800+ sites including TikTok, YouTube, Instagram, X, Facebook and Reddit.")
        Spacer(Modifier.height(12.dp))
        Feature(Icons.Rounded.HighQuality, "Top quality", "Grabs the highest available resolution and merges it with the best audio track.")
        Spacer(Modifier.height(12.dp))
        Feature(Icons.Rounded.Speed, "Fast", "Accelerated downloads with a bundled aria2c engine and on-device ffmpeg merging.")
        Spacer(Modifier.height(12.dp))
        Feature(Icons.Rounded.AutoAwesome, "Beautiful", "A fully animated, glassy neon interface built with Jetpack Compose.")

        Spacer(Modifier.height(20.dp))
        GlassCard(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Rounded.Info, null, tint = Cyan, modifier = Modifier.size(20.dp))
                Text(
                    "Only download content you own or have permission to use, and respect each platform's terms of service and applicable copyright law.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 10.dp),
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(
            "Video Downloader v1.0.0",
            color = TextMuted,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(120.dp))
    }
}

@Composable
private fun Feature(icon: ImageVector, title: String, body: String) {
    GlassCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Cyan, modifier = Modifier.size(28.dp))
            Column(Modifier.padding(start = 14.dp)) {
                Text(title, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                Text(body, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
