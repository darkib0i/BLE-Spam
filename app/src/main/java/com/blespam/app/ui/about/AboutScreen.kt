package com.blespam.app.ui.about

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.blespam.app.BuildConfig
import com.blespam.app.ui.components.GlassCard
import com.blespam.app.ui.components.SectionHeader
import com.blespam.app.ui.theme.LocalAppTheme

/**
 * The About screen: an animated hero logo, version/developer/license metadata, a clear statement of
 * the app's legitimate testing purpose, and a list of the open-source libraries it builds on.
 */
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    contentPadding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(
                start = 16.dp, end = 16.dp,
                top = contentPadding.calculateTopPadding() + 8.dp,
                bottom = contentPadding.calculateBottomPadding() + 24.dp,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) {
            Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
        }

        AnimatedAboutLogo()

        Text(
            text = "BLE Spam",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Bluetooth Low Energy Testing Suite",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            InfoRow("Version", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            InfoRow("Developer", "BLE Spam Project")
            InfoRow("License", "Apache License 2.0")
        }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("Purpose", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(
                "BLE Spam is a developer tool for building, broadcasting, and inspecting Bluetooth Low " +
                    "Energy advertisements. It is intended for legitimate BLE development, debugging, " +
                    "interoperability testing, and education. All advertising stays within Android's " +
                    "public BLE APIs and OS restrictions — the app does not attempt to bypass hardware, " +
                    "operating system, or Bluetooth specification limits.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text("Privacy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(
                "All configurations and logs stay on your device. BLE Spam collects no analytics and " +
                    "requires no network access.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SectionHeader(title = "Open Source Libraries", modifier = Modifier.fillMaxWidth())
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            LIBRARIES.forEach { (name, license) ->
                InfoRow(name, license)
            }
        }
    }
}

@Composable
private fun AnimatedAboutLogo() {
    val theme = LocalAppTheme.current
    val transition = rememberInfiniteTransition(label = "aboutLogo")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = if (theme.animationScale <= 0f) 0f else 360f,
        animationSpec = infiniteRepeatable(
            animation = tween((9000 / theme.animationScale.coerceAtLeast(0.4f)).toInt()),
            repeatMode = RepeatMode.Restart,
        ),
        label = "angle",
    )
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp).padding(top = 8.dp)) {
        Canvas(Modifier.size(120.dp).rotate(angle)) {
            drawCircle(
                brush = Brush.sweepGradient(
                    listOf(theme.accent, theme.accent.copy(alpha = 0.1f), theme.accent),
                ),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f),
                radius = size.minDimension / 2f - 6f,
            )
        }
        Icon(
            Icons.Rounded.Bluetooth,
            contentDescription = null,
            tint = theme.accent,
            modifier = Modifier.size(52.dp),
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}

private val LIBRARIES = listOf(
    "Jetpack Compose" to "Apache 2.0",
    "Material 3" to "Apache 2.0",
    "Hilt (Dagger)" to "Apache 2.0",
    "Room" to "Apache 2.0",
    "DataStore" to "Apache 2.0",
    "Navigation Compose" to "Apache 2.0",
    "Kotlin Coroutines" to "Apache 2.0",
    "Accompanist Permissions" to "Apache 2.0",
)
