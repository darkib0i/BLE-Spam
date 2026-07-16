package com.blespam.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.border
import com.blespam.app.ui.theme.LocalAppTheme

/**
 * A frosted, glassmorphic surface: a translucent fill, a subtle top-lit gradient sheen, and a
 * hairline border that catches the accent light. True Gaussian blur of *background* content isn't
 * available on all API levels, so we emulate the frosted look with layered translucency, which is
 * cheap and looks consistent across every device we target.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(26.dp),
    contentPadding: Dp = 20.dp,
    highlight: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val theme = LocalAppTheme.current
    val fill = if (theme.isDark) {
        Color.White.copy(alpha = if (theme.isAmoled) 0.05f else 0.07f)
    } else {
        Color.White.copy(alpha = 0.65f)
    }
    val sheen = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = if (theme.isDark) 0.10f else 0.35f),
            Color.Transparent,
        ),
    )
    val borderColor = if (highlight) {
        theme.accent.copy(alpha = 0.55f)
    } else if (theme.isDark) {
        Color.White.copy(alpha = 0.12f)
    } else {
        Color.White.copy(alpha = 0.7f)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(fill, shape)
            .background(sheen, shape)
            .border(BorderStroke(1.dp, borderColor), shape),
    ) {
        Column(Modifier.padding(contentPadding), content = content)
    }
}
