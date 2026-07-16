package com.blespam.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.unit.dp
import com.blespam.app.ui.theme.LocalAppTheme

/**
 * A large numeric readout that softly animates to new values and casts an accent-colored glow
 * (a blurred duplicate of the text behind the crisp foreground). Used for the hero counters.
 */
@Composable
fun GlowingCounter(
    value: Long,
    label: String,
    modifier: Modifier = Modifier,
    glowColor: Color? = null,
) {
    val theme = LocalAppTheme.current
    val glow = glowColor ?: theme.accent
    // Animate through intermediate values so the counter "rolls" instead of snapping.
    val animated by animateFloatAsState(
        targetValue = value.toFloat(),
        animationSpec = tween(durationMillis = 600),
        label = "counter",
    )
    val display = animated.toLong().toString()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (theme.animationScale > 0f) {
                Text(
                    text = display,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = glow.copy(alpha = 0.7f),
                    modifier = Modifier.blur(18.dp),
                )
            }
            Text(
                text = display,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
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
