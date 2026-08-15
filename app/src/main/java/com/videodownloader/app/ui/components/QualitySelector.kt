package com.videodownloader.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.videodownloader.app.download.Quality
import com.videodownloader.app.ui.theme.NeonSweep
import com.videodownloader.app.ui.theme.TextMuted

/** Segmented quality picker; the selected chip lights up with the neon fill. */
@Composable
fun QualitySelector(
    selected: Quality,
    onSelect: (Quality) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(6.dp),
    ) {
        Quality.entries.forEach { quality ->
            QualityChip(
                quality = quality,
                selected = quality == selected,
                onClick = { onSelect(quality) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun QualityChip(
    quality: Quality,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val labelColor by animateColorAsState(
        targetValue = if (selected) Color.White else TextMuted,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "label",
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(15.dp))
            .then(
                if (selected)
                    Modifier.background(Brush.horizontalGradient(NeonSweep))
                else Modifier,
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = quality.label,
                color = labelColor,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
            )
            Text(
                text = quality.tagline,
                color = if (selected) Color.White.copy(alpha = 0.85f) else TextMuted.copy(alpha = 0.7f),
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}
