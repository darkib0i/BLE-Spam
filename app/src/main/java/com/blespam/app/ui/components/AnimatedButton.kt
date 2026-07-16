package com.blespam.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blespam.app.ui.theme.LocalGlass

/**
 * A large, premium call-to-action button with a moving gradient fill, a
 * breathing glow that pulses while [active], a spring press animation, and a
 * ripple. Used for the Home "Start" and Control "Start/Stop" actions.
 */
@Composable
fun GradientActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    active: Boolean = false,
    enabled: Boolean = true,
    animated: Boolean = true,
) {
    val glass = LocalGlass.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f),
        label = "btn-scale",
    )

    val transition = rememberInfiniteTransition(label = "btn")
    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue = if (animated) 1f else 0f,
        animationSpec = infiniteRepeatable(tween(3500), RepeatMode.Reverse),
        label = "shift",
    )
    val glowPulse by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = if (active && animated) 0.85f else 0.4f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "glow",
    )

    val start = if (active) Color(0xFFFB7185) else glass.accent
    val end = if (active) Color(0xFFF43F5E) else glass.accentSecondary
    val a by animateColorAsState(start, tween(400), label = "a")
    val b by animateColorAsState(end, tween(400), label = "b")

    val brush = Brush.linearGradient(
        colors = listOf(a, b, a),
        start = androidx.compose.ui.geometry.Offset(shift * 400f, 0f),
        end = androidx.compose.ui.geometry.Offset(400f + shift * 400f, 220f),
    )

    val alpha = if (enabled) 1f else 0.4f

    Row(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = 26.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = a.copy(alpha = glowPulse),
                spotColor = b.copy(alpha = glowPulse),
            )
            .clip(RoundedCornerShape(28.dp))
            .background(brush)
            .clickable(
                interactionSource = interaction,
                indication = ripple(color = Color.White),
                enabled = enabled,
                onClick = onClick,
            )
            .padding(horizontal = 32.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = alpha),
                modifier = Modifier.padding(end = 12.dp),
            )
        }
        Text(
            text = text,
            color = Color.White.copy(alpha = alpha),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
}
