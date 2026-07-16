package com.blespam.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.blespam.app.ui.theme.LocalGlass
import kotlin.math.cos
import kotlin.math.sin

/**
 * A living, animated background: two large radial "aurora" blobs drift on
 * offset sinusoidal paths over the base surface. This is the dynamic,
 * softly-glowing backdrop the whole app sits on.
 *
 * Performance: the animation only invalidates the draw phase (via [drawBehind]),
 * never re-lays-out or re-composes children, so it stays cheap even at 120 Hz.
 * When [animated] is false (Battery Saver / reduced-motion) the blobs are static.
 */
@Composable
fun AnimatedGradientBackground(
    modifier: Modifier = Modifier,
    animated: Boolean = true,
    speed: Float = 1f,
    content: @Composable BoxScope.() -> Unit,
) {
    val glass = LocalGlass.current
    val base = if (glass.isDark) Color.Black else Color(0xFFF6F6FB)

    val transition = rememberInfiniteTransition(label = "aurora")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = if (animated) (2f * Math.PI.toFloat()) else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (16000 / speed.coerceIn(0.2f, 3f)).toInt()),
            repeatMode = RepeatMode.Restart,
        ),
        label = "phase",
    )

    val accent = glass.accent.copy(alpha = if (glass.isDark) 0.38f else 0.22f)
    val accent2 = glass.accentSecondary.copy(alpha = if (glass.isDark) 0.30f else 0.18f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(base)
                val w = size.width
                val h = size.height

                val c1 = Offset(
                    x = w * (0.30f + 0.16f * cos(phase)),
                    y = h * (0.22f + 0.10f * sin(phase)),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(accent, Color.Transparent),
                        center = c1,
                        radius = w * 0.9f,
                    ),
                    radius = w * 0.9f,
                    center = c1,
                )

                val c2 = Offset(
                    x = w * (0.72f + 0.14f * sin(phase * 0.8f)),
                    y = h * (0.70f + 0.12f * cos(phase * 1.1f)),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(accent2, Color.Transparent),
                        center = c2,
                        radius = w * 0.8f,
                    ),
                    radius = w * 0.8f,
                    center = c2,
                )
            },
        content = content,
    )
}
