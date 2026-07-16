package com.blespam.app.ui.components

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blespam.app.ui.theme.LocalGlass

/**
 * A large numeric counter that animates smoothly to its target value and casts
 * a soft colored glow (a blurred copy of itself behind the crisp text). Used
 * for the live "Packets Sent" / statistic counters.
 */
@Composable
fun GlowingCounter(
    value: Long,
    label: String,
    modifier: Modifier = Modifier,
    accent: Color? = null,
) {
    val glass = LocalGlass.current
    val color = accent ?: glass.accent

    // Animate through intermediate values for a lively "ticking" feel.
    val animated by animateIntAsState(
        targetValue = value.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
        animationSpec = tween(600),
        label = "counter",
    )
    val display = formatCount(animated.toLong())

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
            // Blurred glow copy behind the crisp number.
            Text(
                text = display,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = color.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.blur(18.dp),
            )
            Text(
                text = display,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                textAlign = TextAlign.Center,
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/** Compact 1.2K / 3.4M style formatting for large counters. */
fun formatCount(value: Long): String = when {
    value < 1_000 -> value.toString()
    value < 1_000_000 -> "%.1fK".format(value / 1_000.0)
    value < 1_000_000_000 -> "%.1fM".format(value / 1_000_000.0)
    else -> "%.1fB".format(value / 1_000_000_000.0)
}
