package com.blespam.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.blespam.app.ui.theme.LocalAppTheme
import com.blespam.app.ui.theme.onColorFor
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape

/**
 * The hero Start/Stop control: a large circular button that breathes with a pulsing halo while
 * idle, springs on press, and cross-fades color/icon between the start and stop states. All motion
 * scales with the user's animation-speed preference.
 */
@Composable
fun PulsingStartButton(
    isActive: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 168.dp,
) {
    val theme = LocalAppTheme.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val pulse = rememberInfiniteTransition(label = "pulse")
    val halo by pulse.animateFloat(
        initialValue = 1f,
        targetValue = if (theme.animationScale <= 0f) 1f else 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween((1400 / theme.animationScale.coerceAtLeast(0.4f)).toInt()),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "halo",
    )
    // Spring-based press feedback (physics-based transition).
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "press",
    )

    val activeColor = MaterialTheme.colorScheme.primary
    val stopColor = Color(0xFFFF5C7A)
    val buttonColor by animateColorAsState(
        targetValue = if (isActive) stopColor else activeColor,
        animationSpec = tween(400),
        label = "color",
    )
    val disabledColor = MaterialTheme.colorScheme.surfaceVariant
    val finalColor = if (enabled) buttonColor else disabledColor

    Box(contentAlignment = Alignment.Center, modifier = modifier.size(size * 1.4f)) {
        // Glow halo.
        if (enabled && theme.animationScale > 0f) {
            Canvas(Modifier.size(size * 1.4f)) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(finalColor.copy(alpha = 0.45f), Color.Transparent),
                    ),
                    radius = (this.size.minDimension / 2f) * halo,
                )
            }
        }
        Box(
            modifier = Modifier
                .size(size)
                .scale(pressScale)
                .background(
                    brush = Brush.linearGradient(
                        listOf(finalColor, finalColor.copy(alpha = 0.82f)),
                    ),
                    shape = CircleShape,
                )
                .clickable(
                    interactionSource = interaction,
                    indication = androidx.compose.foundation.LocalIndication.current,
                    enabled = enabled,
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isActive) Icons.Rounded.Stop else Icons.Rounded.Bluetooth,
                contentDescription = if (isActive) "Stop advertising" else "Start advertising",
                tint = onColorFor(finalColor),
                modifier = Modifier.size(size / 2.6f),
            )
        }
    }
}
