package com.videodownloader.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.videodownloader.app.ui.theme.TextPrimary

/** A percentage read-out that animates smoothly toward each new value. */
@Composable
fun AnimatedPercentage(
    fraction: Float,
    modifier: Modifier = Modifier,
) {
    val animated by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = tween(500),
        label = "pct",
    )
    Text(
        text = "${(animated * 100).toInt()}%",
        modifier = modifier,
        style = TextStyle(fontWeight = FontWeight.Black, fontSize = 44.sp),
        color = TextPrimary,
    )
}
