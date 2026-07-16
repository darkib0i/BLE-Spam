package com.blespam.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.blespam.app.ui.theme.LocalAppTheme
import kotlin.math.cos
import kotlin.math.sin

/**
 * A softly drifting, blurred mesh-gradient backdrop. Two accent-tinted radial blobs orbit slowly
 * behind a translucent scrim, giving the whole app the "dynamic lighting" feel without ever
 * dropping frames — it's a single [Canvas] animated by one infinite transition.
 *
 * Respects the user's animation-speed setting: at 0 (Off) the blobs are static.
 */
@Composable
fun AnimatedBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val theme = LocalAppTheme.current
    val base = if (theme.isAmoled) Color.Black else if (theme.isDark) Color(0xFF07070B) else Color(0xFFEFEDF7)
    val accent = theme.accent
    val accent2 = theme.accent.copy(alpha = 0.6f)

    val transition = rememberInfiniteTransition(label = "bg")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = if (theme.animationScale <= 0f) 0f else (2f * Math.PI.toFloat()),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (18000 / theme.animationScale.coerceAtLeast(0.35f)).toInt(),
                easing = { it },
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "phase",
    )

    Box(modifier = modifier.fillMaxSize().background(base)) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val blobRadius = w * 0.75f

            val c1 = Offset(
                x = w * (0.3f + 0.18f * cos(phase)),
                y = h * (0.22f + 0.12f * sin(phase)),
            )
            val c2 = Offset(
                x = w * (0.72f + 0.16f * cos(phase + 2.2f)),
                y = h * (0.68f + 0.14f * sin(phase + 1.1f)),
            )

            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(accent.copy(alpha = if (theme.isDark) 0.45f else 0.30f), Color.Transparent),
                    center = c1,
                    radius = blobRadius,
                ),
                size = Size(w, h),
            )
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(accent2.copy(alpha = if (theme.isDark) 0.35f else 0.22f), Color.Transparent),
                    center = c2,
                    radius = blobRadius,
                ),
                size = Size(w, h),
            )
            // Vignette scrim keeps foreground content legible over the glow.
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(base.copy(alpha = 0.1f), base.copy(alpha = 0.55f)),
                ),
                size = Size(w, h),
            )
        }
        content()
    }
}
