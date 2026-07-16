package com.blespam.app.data.preferences

/** UI theme selection. SYSTEM follows the OS; AMOLED is a true-black dark variant. */
enum class ThemeMode { SYSTEM, LIGHT, DARK, AMOLED }

/** Accent seed colors offered in Settings; drives the Compose color scheme. */
enum class AccentColor(val displayName: String, val seedArgb: Long) {
    VIOLET("Violet", 0xFF7C5CFF),
    CYAN("Cyan", 0xFF17C3E6),
    EMERALD("Emerald", 0xFF2BD9A6),
    AMBER("Amber", 0xFFFFB23E),
    ROSE("Rose", 0xFFFF5C8A),
    BLUE("Blue", 0xFF4C6FFF);

    companion object {
        fun fromName(name: String): AccentColor = entries.firstOrNull { it.name == name } ?: VIOLET
    }
}

/** Global animation intensity — lets users trade motion for battery/accessibility. */
enum class AnimationSpeed(val displayName: String, val scale: Float) {
    OFF("Off", 0f),
    CALM("Calm", 0.6f),
    NORMAL("Normal", 1f),
    VIVID("Vivid", 1.4f);

    companion object {
        fun fromName(name: String): AnimationSpeed = entries.firstOrNull { it.name == name } ?: NORMAL
    }
}

/** Immutable snapshot of all user preferences, exposed as a Flow by [UserPreferencesRepository]. */
data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentColor: AccentColor = AccentColor.VIOLET,
    val useDynamicColor: Boolean = true,
    val animationSpeed: AnimationSpeed = AnimationSpeed.NORMAL,
    val batterySaver: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val developerMode: Boolean = false,
    val keepScreenOn: Boolean = false,
    val hasCompletedOnboarding: Boolean = false,
)
