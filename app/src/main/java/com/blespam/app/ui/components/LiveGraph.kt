package com.blespam.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.blespam.app.ui.theme.LocalAppTheme
import kotlin.math.max

/**
 * A lightweight live line-graph for the throughput samples. Draws a smoothed accent-colored line
 * with a soft gradient fill beneath it. Purely a [Canvas] so it stays smooth at high refresh rates.
 */
@Composable
fun LiveGraph(
    samples: List<Float>,
    modifier: Modifier = Modifier,
    height: Dp = 120.dp,
    lineColor: Color? = null,
) {
    val theme = LocalAppTheme.current
    val color = lineColor ?: theme.accent

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
    ) {
        if (samples.size < 2) return@Canvas
        val maxValue = max(samples.maxOrNull() ?: 1f, 1f)
        val stepX = size.width / (samples.size - 1)
        val bottom = size.height

        fun pointFor(index: Int): Offset {
            val x = index * stepX
            val norm = (samples[index] / maxValue).coerceIn(0f, 1f)
            val y = bottom - (norm * bottom * 0.9f) - bottom * 0.05f
            return Offset(x, y)
        }

        val line = Path().apply {
            val first = pointFor(0)
            moveTo(first.x, first.y)
            for (i in 1 until samples.size) {
                val prev = pointFor(i - 1)
                val cur = pointFor(i)
                // Catmull-Rom-ish smoothing via midpoint control points.
                val midX = (prev.x + cur.x) / 2f
                cubicTo(midX, prev.y, midX, cur.y, cur.x, cur.y)
            }
        }

        val fill = Path().apply {
            addPath(line)
            lineTo(size.width, bottom)
            lineTo(0f, bottom)
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
