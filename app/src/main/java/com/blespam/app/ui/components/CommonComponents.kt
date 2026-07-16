package com.blespam.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.blespam.app.ui.theme.LocalAppTheme

/** A section header with an optional trailing slot, used to title groups of cards. */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        trailing?.invoke()
    }
}

/**
 * A shimmering skeleton placeholder. A gradient sweeps horizontally across a rounded box while
 * content is loading, matching the shimmer treatment seen in premium OEM apps.
 */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    height: Dp = 20.dp,
    cornerRadius: Dp = 10.dp,
) {
    val theme = LocalAppTheme.current
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween((1200 / theme.animationScale.coerceAtLeast(0.4f)).toInt()),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerProgress",
    )
    val base = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val highlight = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(base, highlight, base),
                    startX = progress * 300f,
                    endX = (progress + 1f) * 300f,
                ),
            ),
    )
}

/** Convenience skeleton list for the loading state of history/config lists. */
@Composable
fun SkeletonList(rows: Int = 4, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(rows) {
            GlassCard(contentPadding = 16.dp) {
                SkeletonBox(Modifier.fillMaxWidth(0.5f), height = 18.dp)
                Spacer(Modifier.height(10.dp))
                SkeletonBox(Modifier.fillMaxWidth(), height = 14.dp)
            }
        }
    }
}

/** A small pill label used for tags/badges (mode name, "TEST", etc.). */
@Composable
fun Pill(
    text: String,
    modifier: Modifier = Modifier,
    color: Color? = null,
) {
    val theme = LocalAppTheme.current
    val c = color ?: theme.accent
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(c.copy(alpha = 0.16f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = c, fontWeight = FontWeight.SemiBold)
    }
}
