package com.blespam.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.blespam.app.ui.theme.LocalGlass

/**
 * The signature frosted-glass surface used everywhere in the app.
 *
 * Compose can't apply a true backdrop blur to arbitrary content on all
 * supported API levels, so the "frost" is faked convincingly: a translucent
 * tint over the animated background, a soft top-lit gradient sheen, a subtle
 * light border, and a premium drop shadow for depth. When the card is
 * [onClick]-able it springs down on press (physics-based) with a ripple.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    elevation: Dp = 18.dp,
    onClick: (() -> Unit)? = null,
    contentPadding: Dp = 20.dp,
    glowColor: Color? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val glass = LocalGlass.current
    val shape = RoundedCornerShape(cornerRadius)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 600f),
        label = "press-scale",
    )

    val sheen = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = if (glass.isDark) 0.10f else 0.55f),
            Color.White.copy(alpha = 0.0f),
        ),
    )

    var surface = modifier
        .scale(scale)
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = glowColor ?: glass.accent.copy(alpha = 0.5f),
            spotColor = glowColor ?: glass.accent.copy(alpha = 0.5f),
        )
        .clip(shape)
        .background(glass.glass, shape)
        .background(sheen, shape)
        .border(BorderStroke(1.dp, glass.glassBorder), shape)

    if (onClick != null) {
        surface = surface.clickable(
            interactionSource = interaction,
            indication = ripple(color = glass.accent),
            onClick = onClick,
        )
    }

    Box(modifier = surface.padding(contentPadding), content = content)
}
