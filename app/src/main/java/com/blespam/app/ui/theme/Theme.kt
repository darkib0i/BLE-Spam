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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import com.blespam.app.data.preferences.AccentColor
import com.blespam.app.data.preferences.AnimationSpeed
import com.blespam.app.data.preferences.ThemeMode

/**
 * Exposes the resolved accent + animation-speed to the whole tree so any component can pull the
 * live glow color or scale its motion without prop-drilling.
 */
data class AppThemeState(
    val accent: Color,
    val isDark: Boolean,
    val isAmoled: Boolean,
    val animationScale: Float,
)

val LocalAppTheme = staticCompositionLocalOf {
    AppThemeState(accent = Color(0xFF7C5CFF), isDark = true, isAmoled = false, animationScale = 1f)
}

/**
 * Root theme. Resolves dark/light/AMOLED, applies Material You dynamic color when the user opts in
 * (Android 12+), and otherwise builds a scheme seeded from the chosen accent color.
 */
@Composable
fun BleSpamTheme(
    themeMode: ThemeMode,
    accentColor: AccentColor,
    useDynamicColor: Boolean,
    animationSpeed: AnimationSpeed,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK, ThemeMode.AMOLED -> true
    }
    val isAmoled = themeMode == ThemeMode.AMOLED
    val accent = Color(accentColor.seedArgb)
    val context = LocalContext.current

    val dynamicAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    var colorScheme = when {
        useDynamicColor && dynamicAvailable && isDark -> dynamicDarkColorScheme(context)
        useDynamicColor && dynamicAvailable && !isDark -> dynamicLightColorScheme(context)
        isDark -> darkColorScheme(
            primary = accent,
            secondary = accent.copy(alpha = 0.85f),
            tertiary = shiftHue(accent),
            background = DarkSurface,
            surface = DarkSurface,
            surfaceVariant = DarkSurfaceElevated,
            outline = DarkOutline,
            onBackground = TextHighDark,
            onSurface = TextHighDark,
            onSurfaceVariant = TextMediumDark,
        )
        else -> lightColorScheme(
            primary = accent,
            secondary = accent.copy(alpha = 0.85f),
            tertiary = shiftHue(accent),
            background = LightSurface,
            surface = LightSurface,
            surfaceVariant = LightSurfaceElevated,
            outline = LightOutline,
            onBackground = TextHighLight,
            onSurface = TextHighLight,
            onSurfaceVariant = TextMediumLight,
        )
    }

    // For AMOLED, force true black backgrounds to save power on OLED panels.
    if (isAmoled) {
        colorScheme = colorScheme.copy(background = InkBlack, surface = AmoledSurface)
    }

    val themeState = AppThemeState(
        accent = if (useDynamicColor && dynamicAvailable) colorScheme.primary else accent,
        isDark = isDark,
        isAmoled = isAmoled,
        animationScale = animationSpeed.scale,
    )

    CompositionLocalProvider(LocalAppTheme provides themeState) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}

/** Rotates a color's hue by ~40° to derive a complementary tertiary/gradient stop. */
private fun shiftHue(color: Color): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.RGBToHSV(
        (color.red * 255).toInt(),
        (color.green * 255).toInt(),
        (color.blue * 255).toInt(),
        hsv,
    )
    hsv[0] = (hsv[0] + 40f) % 360f
    return Color(android.graphics.Color.HSVToColor(hsv))
}

/** Convenience for choosing on-color contrast against an arbitrary background. */
fun onColorFor(background: Color): Color =
    if (background.luminance() > 0.5f) Color.Black else Color.White
