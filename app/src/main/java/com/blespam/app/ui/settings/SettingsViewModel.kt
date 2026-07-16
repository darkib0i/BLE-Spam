package com.blespam.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blespam.app.data.preferences.AccentColor
import com.blespam.app.data.preferences.AnimationSpeed
import com.blespam.app.data.preferences.ThemeMode
import com.blespam.app.data.preferences.UserPreferences
import com.blespam.app.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Exposes user preferences and the setters the Settings screen binds to. */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: UserPreferencesRepository,
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = repository.preferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserPreferences(),
    )

    fun setTheme(mode: ThemeMode) = launch { repository.setThemeMode(mode) }
    fun setAccent(color: AccentColor) = launch { repository.setAccentColor(color) }
    fun setDynamicColor(enabled: Boolean) = launch { repository.setDynamicColor(enabled) }
    fun setAnimationSpeed(speed: AnimationSpeed) = launch { repository.setAnimationSpeed(speed) }
    fun setBatterySaver(enabled: Boolean) = launch { repository.setBatterySaver(enabled) }
    fun setNotifications(enabled: Boolean) = launch { repository.setNotifications(enabled) }
    fun setDeveloperMode(enabled: Boolean) = launch { repository.setDeveloperMode(enabled) }
    fun setKeepScreenOn(enabled: Boolean) = launch { repository.setKeepScreenOn(enabled) }

    private inline fun launch(crossinline block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
