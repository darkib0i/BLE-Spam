package com.blespam.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/** User-selectable accent colors (Settings → Accent color). */
enum class AccentColor(val seed: Color) {
    INDIGO(Indigo),
    CYAN(Cyan),
    EMERALD(Emerald),
    AMBER(Amber),
    ROSE(Rose),
    SKY(Sky),
}

/** Theme mode (Settings → Theme). */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * Extra design tokens that Material's [MaterialTheme.colorScheme] doesn't
 * model — the glass tints and the accent gradient used across the UI.
 */
data class GlassTokens(
    val glass: Color,
    val glassBorder: Color,
    val accent: Color,
    val accentSecondary: Color,
    val isDark: Boolean,
)

val LocalGlass = staticCompositionLocalOf {
    GlassTokens(GlassDark, GlassBorderDark, Indigo, Violet, true)
}

private fun darkScheme(accent: Color) = darkColorScheme(
    primary = accent,
    onPrimary = Color.White,
    secondary = Violet,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    error = StatusError,
)

private fun lightScheme(accent: Color) = lightColorScheme(
    primary = accent,
    onPrimary = Color.White,
    secondary = Violet,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    error = StatusError,
)

/**
 * Root theme.
 *
 * @param themeMode  system / light / dark override.
 * @param accent     accent color seed.
 * @param dynamicColor  when true and supported (Android 12+), derives the
 *                      scheme from the device wallpaper (Material You).
 * @param amoled     forces a pure-black background in dark mode for AMOLED panels.
 */
@Composable
fun BleSpamTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    accent: AccentColor = AccentColor.INDIGO,
    dynamicColor: Boolean = false,
    amoled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val context = LocalContext.current

    var colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> darkScheme(accent.seed)
        else -> lightScheme(accent.seed)
    }

    if (dark && amoled) {
        colorScheme = colorScheme.copy(background = Color.Black)
    }

    val glass = GlassTokens(
        glass = if (dark) GlassDark else GlassLight,
        glassBorder = if (dark) GlassBorderDark else GlassBorderLight,
        accent = if (dynamicColor) colorScheme.primary else accent.seed,
        accentSecondary = if (dark) Violet else Indigo,
        isDark = dark,
    )

    CompositionLocalProvider(LocalGlass provides glass) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}
