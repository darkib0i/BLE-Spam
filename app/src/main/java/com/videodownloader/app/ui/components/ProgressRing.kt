package com.videodownloader.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.videodownloader.app.ui.theme.NeonSweep

/**
 * A circular progress indicator with a rotating neon sweep. When [progress] is
 * null it spins as an indeterminate "preparing" state; otherwise it fills the
 * arc smoothly to the target fraction. [center] renders inside the ring.
 */
@Composable
fun ProgressRing(
    progress: Float?,
    modifier: Modifier = Modifier,
    center: @Composable () -> Unit = {},
) {
    val t = rememberInfiniteTransition(label = "ring")
    val rotation by t.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "rot",
    )
    // Indeterminate arc length pulses in and out while preparing.
    val indeterminateSweep by t.animateFloat(
        initialValue = 30f, targetValue = 300f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Reverse),
        label = "indet",
    )
    val animatedProgress by animateFloatAsState(
        targetValue = progress ?: 0f,
        animationSpec = tween(500, easing = LinearEasing),
        label = "prog",
    )

    Box(modifier = modifier.size(220.dp), contentAlignment = Alignment.Center) {
        Canvas(
            Modifier
                .size(220.dp)
                .graphicsLayer { rotationZ = rotation },
        ) {
            val stroke = 16.dp.toPx()
            val diameter = size.minDimension - stroke
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)

            // Track.
            drawArc(
                color = Color.White.copy(alpha = 0.08f),
                startAngle = 0f, sweepAngle = 360f, useCenter = false,
                topLeft = topLeft, size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )

            val sweepAngle = if (progress == null) indeterminateSweep else animatedProgress * 360f
            val brush = Brush.sweepGradient(NeonSweep)

            // Outer glow pass.
            drawArc(
                brush = brush,
                startAngle = -90f, sweepAngle = sweepAngle, useCenter = false,
                topLeft = topLeft, size = arcSize,
                style = Stroke(width = stroke * 1.9f, cap = StrokeCap.Round),
                alpha = 0.25f,
            )
            // Crisp arc.
            drawArc(
                brush = brush,
                startAngle = -90f, sweepAngle = sweepAngle, useCenter = false,
                topLeft = topLeft, size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
        center()
    }
}
