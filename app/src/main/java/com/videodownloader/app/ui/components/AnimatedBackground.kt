package com.videodownloader.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.BlendMode
import com.videodownloader.app.ui.theme.Cyan
import com.videodownloader.app.ui.theme.Magenta
import com.videodownloader.app.ui.theme.Night
import com.videodownloader.app.ui.theme.Pink
import com.videodownloader.app.ui.theme.Violet
import kotlin.math.cos
import kotlin.math.sin

/**
 * A living aurora backdrop: several oversized neon blobs drift on independent
 * sine paths and additively blend, so the whole screen breathes and shifts
 * colour without ever repeating exactly. Pure Compose/Canvas — no assets.
 */
@Composable
fun AnimatedGradientBackground(modifier: Modifier = Modifier) {
    val t = rememberInfiniteTransition(label = "aurora")
    val phase by t.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(18_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "phase",
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Night),
    ) {
        val w = size.width
        val h = size.height
        val r = maxOf(w, h) * 0.85f

        data class Blob(val color: Color, val cx: Float, val cy: Float, val sx: Float, val sy: Float, val speed: Float)

        val blobs = listOf(
            Blob(Violet, 0.25f, 0.20f, 0.22f, 0.16f, 1.0f),
            Blob(Magenta, 0.80f, 0.30f, 0.18f, 0.20f, 1.4f),
            Blob(Cyan, 0.30f, 0.82f, 0.20f, 0.14f, 0.8f),
            Blob(Pink, 0.75f, 0.78f, 0.16f, 0.18f, 1.7f),
        )

        blobs.forEach { b ->
            val x = (b.cx + b.sx * cos(phase * b.speed)) * w
            val y = (b.cy + b.sy * sin(phase * b.speed * 1.3f)) * h
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(b.color.copy(alpha = 0.55f), Color.Transparent),
                    center = Offset(x, y),
                    radius = r,
                ),
                radius = r,
                center = Offset(x, y),
                blendMode = BlendMode.Plus,
            )
        }

        // Darken the edges to keep foreground text readable (vignette).
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, Night.copy(alpha = 0.65f)),
                center = Offset(w * 0.5f, h * 0.42f),
                radius = maxOf(w, h) * 0.75f,
            ),
        )
    }
}
