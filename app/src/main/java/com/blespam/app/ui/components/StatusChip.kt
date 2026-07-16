package com.blespam.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.blespam.app.ui.theme.StatusError
import com.blespam.app.ui.theme.StatusOk
import com.blespam.app.ui.theme.StatusWarn

enum class StatusLevel(val color: Color) {
    OK(StatusOk),
    WARN(StatusWarn),
    ERROR(StatusError),
    NEUTRAL(Color(0xFF9AA0B4)),
}

/**
 * A single status row (icon + label + value + live pulsing status dot) used on
 * the Home screen for Bluetooth / permission / battery states.
 */
@Composable
fun StatusRow(
    icon: ImageVector,
    label: String,
    value: String,
    level: StatusLevel,
    modifier: Modifier = Modifier,
    animated: Boolean = true,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(14.dp))
        androidx.compose.foundation.layout.Column(Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        PulsingDot(level.color, animated)
    }
}

/** A live, glowing status dot that gently pulses to signal "live" state. */
@Composable
fun PulsingDot(color: Color, animated: Boolean = true, size: Int = 12) {
    val transition = rememberInfiniteTransition(label = "dot")
    val pulse by transition.animateFloat(
        initialValue = if (animated) 0.4f else 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "pulse",
    )
    Box(
        modifier = Modifier
            .size(size.dp)
            .drawBehind {
                drawCircle(color = color.copy(alpha = 0.25f * pulse), radius = this.size.minDimension)
                drawCircle(color = color.copy(alpha = pulse), radius = this.size.minDimension / 2f)
            },
    )
}

/** A small rounded pill chip, used for tags and quick states. */
@Composable
fun Pill(text: String, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .drawBehind { }
            .padding(0.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier
                .size(8.dp)
                .drawBehind { drawCircle(color) }
                .padding(end = 6.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, color = color)
    }
}
