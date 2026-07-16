package com.blespam.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.blespam.app.ui.theme.StatusError
import com.blespam.app.ui.theme.StatusOk
import com.blespam.app.ui.theme.StatusWarn

/** Semantic status level driving the accent color of a [StatusCard]. */
enum class StatusLevel { OK, WARN, ERROR, NEUTRAL }

/**
 * A compact frosted status tile: icon in a tinted chip, a label, and a value. Used across the Home
 * screen for Bluetooth/permission/battery status. The tint animates when the level changes.
 */
@Composable
fun StatusCard(
    icon: ImageVector,
    label: String,
    value: String,
    level: StatusLevel,
    modifier: Modifier = Modifier,
) {
    val target = when (level) {
        StatusLevel.OK -> StatusOk
        StatusLevel.WARN -> StatusWarn
        StatusLevel.ERROR -> StatusError
        StatusLevel.NEUTRAL -> MaterialTheme.colorScheme.primary
    }
    val tint by animateColorAsState(targetValue = target, label = "statusTint")

    GlassCard(modifier = modifier, contentPadding = 16.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(tint.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** A tiny inline status dot + text used inside denser rows. */
@Composable
fun StatusDot(level: StatusLevel, text: String, modifier: Modifier = Modifier) {
    val color = when (level) {
        StatusLevel.OK -> StatusOk
        StatusLevel.WARN -> StatusWarn
        StatusLevel.ERROR -> StatusError
        StatusLevel.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}
