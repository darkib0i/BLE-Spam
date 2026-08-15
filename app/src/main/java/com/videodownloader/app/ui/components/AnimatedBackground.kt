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
import com.videodownloader.app.ui.theme.Night
import kotlin.math.cos
import kotlin.math.sin

/**
 * A cheap, smooth backdrop: two soft grey glows drift slowly over a black
 * base. Only two radial-gradient draws per frame with normal blending — light
 * enough to stay at 60fps on low-end devices (the previous four-blob additive
 * version was the main source of jank).
 */
@Composable
fun AnimatedGradientBackground(modifier: Modifier = Modifier) {
    val t = rememberInfiniteTransition(label = "bg")
    val phase by t.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(22_000, easing = LinearEasing),
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
        val r = maxOf(w, h) * 0.75f
        val glow = Color(0xFF3A3A3A)

        val c1 = Offset((0.30f + 0.14f * cos(phase)) * w, (0.24f + 0.10f * sin(phase)) * h)
        drawCircle(
            brush = Brush.radialGradient(
                listOf(glow.copy(alpha = 0.55f), Color.Transparent),
                center = c1, radius = r,
            ),
            radius = r, center = c1,
        )

        val c2 = Offset((0.74f + 0.12f * cos(phase * 1.2f + 2f)) * w, (0.78f + 0.10f * sin(phase * 1.1f)) * h)
        drawCircle(
            brush = Brush.radialGradient(
                listOf(glow.copy(alpha = 0.40f), Color.Transparent),
                center = c2, radius = r,
            ),
            radius = r, center = c2,
        )
    }
}
