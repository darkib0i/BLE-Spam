package com.blespam.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * The BLE Spam palette.
 *
 * The design language draws from One UI 8, Nothing OS, iOS 26 and Material
 * Expressive: deep AMOLED blacks, luminous indigo/violet accents, and a set
 * of gradient stops used for the animated backgrounds and glowing counters.
 */

// --- Brand accents (selectable in Settings) ---
val Indigo = Color(0xFF6C5CE7)
val Violet = Color(0xFF8E7BFF)
val Cyan = Color(0xFF22D3EE)
val Emerald = Color(0xFF34D399)
val Amber = Color(0xFFFBBF24)
val Rose = Color(0xFFFB7185)
val Sky = Color(0xFF38BDF8)

// --- Dark scheme (AMOLED) ---
val DarkBackground = Color(0xFF000000)
val DarkSurface = Color(0xFF0B0B12)
val DarkSurfaceVariant = Color(0xFF15151F)
val DarkOnSurface = Color(0xFFF3F3F8)
val DarkOnSurfaceVariant = Color(0xFFB4B4C6)
val DarkOutline = Color(0xFF2A2A38)

// --- Light scheme ---
val LightBackground = Color(0xFFF6F6FB)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFECECF5)
val LightOnSurface = Color(0xFF14141C)
val LightOnSurfaceVariant = Color(0xFF4A4A5A)
val LightOutline = Color(0xFFD9D9E6)

// --- Semantic status colors ---
val StatusOk = Color(0xFF34D399)
val StatusWarn = Color(0xFFFBBF24)
val StatusError = Color(0xFFFB7185)

/**
 * Glass tints for frosted cards. The alpha is deliberately low so the animated
 * gradient background reads through the surface.
 */
val GlassDark = Color(0x14FFFFFF)
val GlassLight = Color(0x40FFFFFF)
val GlassBorderDark = Color(0x22FFFFFF)
val GlassBorderLight = Color(0x33FFFFFF)
