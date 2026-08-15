package com.videodownloader.app.ui.components

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.videodownloader.app.ui.theme.TextMuted

/**
 * Looks and behaves like an ordinary (inert) search field. There is no visible
 * feedback of any kind: tapping it does nothing the user can see until it has
 * been tapped [tapsToUnlock] times in quick succession, at which point
 * [onUnlock] fires. The counter resets if there's a pause between taps, so
 * incidental taps never accumulate.
 */
@Composable
fun SecretSearchBar(
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier,
    tapsToUnlock: Int = 10,
    resetAfterMs: Long = 1400L,
) {
    // Kept in a holder so taps aren't lost across recompositions without any
    // state read (which would be a visible change) happening in the UI.
    val counter = remember { TapCounter() }

    Row(
        modifier = modifier
            .height(46.dp)
            .clip(RoundedCornerShape(23.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                val now = SystemClock.elapsedRealtime()
                counter.count = if (now - counter.last > resetAfterMs) 1 else counter.count + 1
                counter.last = now
                if (counter.count >= tapsToUnlock) {
                    counter.count = 0
                    onUnlock()
                }
            }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Search, contentDescription = "Search", tint = TextMuted, modifier = Modifier.size(20.dp))
        Text("  Search", color = TextMuted)
    }
}

private class TapCounter {
    var count: Int = 0
    var last: Long = 0L
}
