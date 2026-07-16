package com.blespam.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.blespam.app.ui.theme.LocalGlass
import kotlin.math.max

/**
 * A smooth, gradient-filled live area chart for the throughput samples on the
 * Control screen. Draws a Catmull-Rom-ish smoothed line with a translucent fill
 * beneath it. Redraws are confined to the Canvas draw phase for 120 Hz-friendly
 * performance.
 */
@Composable
fun LiveAreaChart(
    samples: List<Float>,
    modifier: Modifier = Modifier,
    height: Dp = 120.dp,
    lineColor: Color? = null,
) {
    val glass = LocalGlass.current
    val color = lineColor ?: glass.accent

    // Animate the vertical scale so new peaks glide in rather than snap.
    val peak = max(1f, samples.maxOrNull() ?: 1f)
    val animatedPeak by animateFloatAsState(peak, tween(500), label = "peak")

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
    ) {
        if (samples.size < 2) return@Canvas
        val w = size.width
        val h = size.height
        val stepX = w / (samples.size - 1).coerceAtLeast(1)

        fun pointAt(i: Int): Offset {
            val v = (samples[i] / animatedPeak).coerceIn(0f, 1f)
            return Offset(i * stepX, h - v * h * 0.9f - h * 0.05f)
        }

        val line = Path().apply {
            moveTo(0f, pointAt(0).y)
            for (i in 1 until samples.size) {
                val prev = pointAt(i - 1)
                val cur = pointAt(i)
                val midX = (prev.x + cur.x) / 2f
                cubicTo(midX, prev.y, midX, cur.y, cur.x, cur.y)
            }
        }

        val fill = Path().apply {
            addPath(line)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }

        drawPath(
            path = fill,
            brush = Brush.verticalGradient(
                listOf(color.copy(alpha = 0.35f), Color.Transparent),
            ),
        )
        drawPath(
            path = line,
            color = color,
            style = Stroke(width = 4f),
        )
    }
}

/** A compact vertical bar visualiser used for small inline stats. */
@Composable
fun MiniBars(
    values: List<Float>,
    modifier: Modifier = Modifier,
    height: Dp = 40.dp,
    barColor: Color? = null,
) {
    val glass = LocalGlass.current
    val color = barColor ?: glass.accent
    val peak = max(1f, values.maxOrNull() ?: 1f)

    Canvas(modifier = modifier.fillMaxWidth().height(height)) {
        if (values.isEmpty()) return@Canvas
        val gap = 3f
        val barWidth = (size.width - gap * (values.size - 1)) / values.size
        values.forEachIndexed { i, v ->
            val bh = (v / peak).coerceIn(0.02f, 1f) * size.height
            drawRoundRect(
                color = color.copy(alpha = 0.85f),
                topLeft = Offset(i * (barWidth + gap), size.height - bh),
                size = androidx.compose.ui.geometry.Size(barWidth, bh),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2f),
            )
        }
    }
}
