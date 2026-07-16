package com.blespam.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.blespam.app.ui.theme.AccentColor
import com.blespam.app.ui.theme.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/** User-facing app settings, backed by DataStore (Settings screen). */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentColor: AccentColor = AccentColor.INDIGO,
    val dynamicColor: Boolean = false,
    val amoled: Boolean = true,
    val animationSpeed: Float = 1.0f,
    val animationsEnabled: Boolean = true,
    val batterySaver: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val developerMode: Boolean = false,
    val language: String = "system",
    val acceptedResponsibleUse: Boolean = false,
)

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val ACCENT = stringPreferencesKey("accent_color")
        val DYNAMIC = booleanPreferencesKey("dynamic_color")
        val AMOLED = booleanPreferencesKey("amoled")
        val ANIM_SPEED = floatPreferencesKey("animation_speed")
        val ANIM_ENABLED = booleanPreferencesKey("animations_enabled")
        val BATTERY_SAVER = booleanPreferencesKey("battery_saver")
        val NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")
        val DEVELOPER = booleanPreferencesKey("developer_mode")
        val LANGUAGE = stringPreferencesKey("language")
        val ACCEPTED_USE = booleanPreferencesKey("accepted_responsible_use")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            themeMode = prefs[Keys.THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
            accentColor = prefs[Keys.ACCENT]?.let { runCatching { AccentColor.valueOf(it) }.getOrNull() }
                ?: AccentColor.INDIGO,
            dynamicColor = prefs[Keys.DYNAMIC] ?: false,
            amoled = prefs[Keys.AMOLED] ?: true,
            animationSpeed = prefs[Keys.ANIM_SPEED] ?: 1.0f,
            animationsEnabled = prefs[Keys.ANIM_ENABLED] ?: true,
            batterySaver = prefs[Keys.BATTERY_SAVER] ?: false,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS] ?: true,
            developerMode = prefs[Keys.DEVELOPER] ?: false,
            language = prefs[Keys.LANGUAGE] ?: "system",
            acceptedResponsibleUse = prefs[Keys.ACCEPTED_USE] ?: false,
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) = edit { it[Keys.THEME] = mode.name }
    suspend fun setAccent(accent: AccentColor) = edit { it[Keys.ACCENT] = accent.name }
    suspend fun setDynamicColor(enabled: Boolean) = edit { it[Keys.DYNAMIC] = enabled }
    suspend fun setAmoled(enabled: Boolean) = edit { it[Keys.AMOLED] = enabled }
    suspend fun setAnimationSpeed(speed: Float) = edit { it[Keys.ANIM_SPEED] = speed }
    suspend fun setAnimationsEnabled(enabled: Boolean) = edit { it[Keys.ANIM_ENABLED] = enabled }
    suspend fun setBatterySaver(enabled: Boolean) = edit { it[Keys.BATTERY_SAVER] = enabled }
    suspend fun setNotifications(enabled: Boolean) = edit { it[Keys.NOTIFICATIONS] = enabled }
    suspend fun setDeveloperMode(enabled: Boolean) = edit { it[Keys.DEVELOPER] = enabled }
    suspend fun setLanguage(language: String) = edit { it[Keys.LANGUAGE] = language }
    suspend fun setAcceptedResponsibleUse(accepted: Boolean) =
        edit { it[Keys.ACCEPTED_USE] = accepted }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.dataStore.edit(block)
    }
}
