package com.videodownloader.app.ui.theme

import androidx.compose.ui.graphics.Color

// Monochrome theme: black base, white/grey accents.
val Night = Color(0xFF080808)
val NightElevated = Color(0xFF161616)
val Surface = Color(0xFF1C1C1C)

// "Accent" slots kept by name so the whole app recolors from here. In the
// monochrome scheme these are light greys / white used as foreground tints.
val Violet = Color(0xFF9E9E9E)
val Magenta = Color(0xFFE0E0E0)
val Cyan = Color(0xFFE6E6E6)
val Pink = Color(0xFFDADADA)
val Lime = Color(0xFFFFFFFF)

val TextPrimary = Color(0xFFF5F5F5)
val TextSecondary = Color(0xFFB5B5B5)
val TextMuted = Color(0xFF7A7A7A)

// Mid-grey gradient used as a BACKGROUND fill (buttons, pills, chips); white
// text/icons stay readable on top of it.
val NeonSweep = listOf(
    Color(0xFF3A3A3A),
    Color(0xFF565656),
    Color(0xFF6E6E6E),
    Color(0xFF565656),
    Color(0xFF3A3A3A),
)

// Lighter gradient used only for the shimmering title text.
val TitleSweep = listOf(
    Color(0xFF8C8C8C),
    Color(0xFFFFFFFF),
    Color(0xFFC4C4C4),
    Color(0xFFFFFFFF),
    Color(0xFF8C8C8C),
)
