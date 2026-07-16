package com.blespam.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.blespam.app.ui.theme.LocalAppTheme
import com.blespam.app.ui.theme.onColorFor

/**
 * A filled, gradient pill button that springs on press. The default variant uses the accent
 * gradient; [outlined] renders a translucent glass variant for secondary actions.
 */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    outlined: Boolean = false,
    content: (@Composable RowScope.() -> Unit)? = null,
) {
    val theme = LocalAppTheme.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "btnScale",
    )

    val accent = theme.accent
    val brush = if (outlined) {
        Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
    } else {
        Brush.horizontalGradient(listOf(accent, accent.copy(alpha = 0.82f)))
    }
    val contentColor = if (outlined) accent else onColorFor(accent)
    val alpha = if (enabled) 1f else 0.4f

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(50))
            .then(
                if (outlined) {
                    Modifier.background(accent.copy(alpha = 0.12f))
                } else {
                    Modifier.background(brush)
                }
            )
            .clickable(
                interactionSource = interaction,
                indication = androidx.compose.foundation.LocalIndication.current,
                enabled = enabled,
                onClick = onClick,
            )
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            content?.invoke(this)
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = contentColor.copy(alpha = alpha),
            )
        }
    }
}
