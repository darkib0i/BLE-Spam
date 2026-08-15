package com.videodownloader.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.Text
import com.videodownloader.app.ui.theme.NeonSweep

/**
 * Text whose fill is a neon gradient that slides horizontally forever,
 * producing a continuous shimmer across the glyphs.
 */
@Composable
fun AuroraText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
) {
    val t = rememberInfiniteTransition(label = "shimmer")
    val shift by t.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shift",
    )

    val span = 900f
    val start = -span + shift * span * 2f
    val brush = Brush.linearGradient(
        colors = NeonSweep,
        start = Offset(start, 0f),
        end = Offset(start + span, 0f),
    )

    Text(
        text = text,
        modifier = modifier,
        style = style.copy(brush = brush),
    )
}
