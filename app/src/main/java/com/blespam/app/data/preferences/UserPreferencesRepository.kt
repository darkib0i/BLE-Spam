package com.blespam.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

/**
 * Persists user settings with Jetpack DataStore. Reads are a cold [Flow] so the whole UI reacts to
 * theme/accent/animation changes instantly, and writes are cheap suspend functions.
 */
@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val ACCENT = stringPreferencesKey("accent_color")
        val DYNAMIC = booleanPreferencesKey("dynamic_color")
        val ANIM = stringPreferencesKey("animation_speed")
        val BATTERY_SAVER = booleanPreferencesKey("battery_saver")
        val NOTIFICATIONS = booleanPreferencesKey("notifications")
        val DEVELOPER = booleanPreferencesKey("developer_mode")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val ONBOARDING = booleanPreferencesKey("onboarding_done")
    }

    val preferences: Flow<UserPreferences> = context.dataStore.data.map { p ->
        UserPreferences(
            themeMode = p[Keys.THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM,
            accentColor = AccentColor.fromName(p[Keys.ACCENT] ?: AccentColor.VIOLET.name),
            useDynamicColor = p[Keys.DYNAMIC] ?: true,
            animationSpeed = AnimationSpeed.fromName(p[Keys.ANIM] ?: AnimationSpeed.NORMAL.name),
            batterySaver = p[Keys.BATTERY_SAVER] ?: false,
            notificationsEnabled = p[Keys.NOTIFICATIONS] ?: true,
            developerMode = p[Keys.DEVELOPER] ?: false,
            keepScreenOn = p[Keys.KEEP_SCREEN_ON] ?: false,
            hasCompletedOnboarding = p[Keys.ONBOARDING] ?: false,
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) = edit { it[Keys.THEME] = mode.name }
    suspend fun setAccentColor(color: AccentColor) = edit { it[Keys.ACCENT] = color.name }
    suspend fun setDynamicColor(enabled: Boolean) = edit { it[Keys.DYNAMIC] = enabled }
    suspend fun setAnimationSpeed(speed: AnimationSpeed) = edit { it[Keys.ANIM] = speed.name }
    suspend fun setBatterySaver(enabled: Boolean) = edit { it[Keys.BATTERY_SAVER] = enabled }
    suspend fun setNotifications(enabled: Boolean) = edit { it[Keys.NOTIFICATIONS] = enabled }
    suspend fun setDeveloperMode(enabled: Boolean) = edit { it[Keys.DEVELOPER] = enabled }
    suspend fun setKeepScreenOn(enabled: Boolean) = edit { it[Keys.KEEP_SCREEN_ON] = enabled }
    suspend fun setOnboardingComplete(done: Boolean) = edit { it[Keys.ONBOARDING] = done }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.dataStore.edit(block)
    }
}
