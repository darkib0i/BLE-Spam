package com.videodownloader.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import com.videodownloader.app.ui.theme.Cyan
import com.videodownloader.app.ui.theme.Lime
import com.videodownloader.app.ui.theme.Magenta
import com.videodownloader.app.ui.theme.Pink
import com.videodownloader.app.ui.theme.Violet
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * A one-shot celebration: a ring pops, a checkmark draws itself stroke-first,
 * and a burst of neon confetti flies outward and fades. Runs once on entry.
 */
@Composable
fun SuccessBurst(modifier: Modifier = Modifier) {
    val ring = remember { Animatable(0f) }
    val check = remember { Animatable(0f) }
    val confetti = remember { Animatable(0f) }

    val pieces = remember {
        val colors = listOf(Violet, Magenta, Pink, Cyan, Lime)
        List(28) {
            ConfettiPiece(
                angle = Random.nextFloat() * 2f * Math.PI.toFloat(),
                distance = 0.55f + Random.nextFloat() * 0.55f,
                color = colors[it % colors.size],
                size = 5f + Random.nextFloat() * 7f,
                spin = Random.nextFloat() * 6f - 3f,
            )
        }
    }

    LaunchedEffect(Unit) {
        ring.animateTo(1f, tween(420, easing = FastOutSlowInEasing))
        check.animateTo(1f, tween(360, easing = FastOutSlowInEasing))
        confetti.animateTo(1f, tween(900, easing = LinearEasing))
    }

    Box(modifier = modifier.size(160.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(160.dp)) {
            val c = Offset(size.width / 2f, size.height / 2f)
            val maxR = size.minDimension / 2f

            // Confetti.
            val cp = confetti.value
            pieces.forEach { p ->
                val d = p.distance * maxR * 1.6f * cp
                val pos = Offset(c.x + cos(p.angle) * d, c.y + sin(p.angle) * d)
                drawCircle(
                    color = p.color.copy(alpha = (1f - cp).coerceIn(0f, 1f)),
                    radius = p.size * (1f - cp * 0.4f),
                    center = pos,
                )
            }

            // Expanding ring.
            val rr = ring.value
            drawCircle(
                color = Lime.copy(alpha = (1f - rr) * 0.9f),
                radius = maxR * 0.55f * rr,
                center = c,
                style = Stroke(width = 6f),
            )
            // Solid disc.
            drawCircle(
                color = Lime.copy(alpha = 0.18f),
                radius = maxR * 0.42f,
                center = c,
            )

            // Checkmark, drawn progressively.
            val prog = check.value
            if (prog > 0f) {
                val p1 = Offset(c.x - maxR * 0.20f, c.y + maxR * 0.02f)
                val p2 = Offset(c.x - maxR * 0.04f, c.y + maxR * 0.18f)
                val p3 = Offset(c.x + maxR * 0.24f, c.y - maxR * 0.18f)
                val path = Path().apply {
                    moveTo(p1.x, p1.y)
                    if (prog <= 0.5f) {
                        val f = prog / 0.5f
                        lineTo(p1.x + (p2.x - p1.x) * f, p1.y + (p2.y - p1.y) * f)
                    } else {
                        lineTo(p2.x, p2.y)
                        val f = (prog - 0.5f) / 0.5f
                        lineTo(p2.x + (p3.x - p2.x) * f, p2.y + (p3.y - p2.y) * f)
                    }
                }
                drawPath(
                    path = path,
                    color = Lime,
                    style = Stroke(width = 12f, cap = StrokeCap.Round),
                )
            }
        }
    }
}

private data class ConfettiPiece(
    val angle: Float,
    val distance: Float,
    val color: androidx.compose.ui.graphics.Color,
    val size: Float,
    val spin: Float,
)
