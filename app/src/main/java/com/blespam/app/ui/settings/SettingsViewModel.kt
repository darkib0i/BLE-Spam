package com.blespam.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blespam.app.data.preferences.AppSettings
import com.blespam.app.data.preferences.SettingsRepository
import com.blespam.app.ui.theme.AccentColor
import com.blespam.app.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs the Settings screen and also supplies the app-wide theme settings that
 * [com.blespam.app.MainActivity] observes. Every write is fire-and-forget into
 * DataStore; the resulting flow drives the UI reactively.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = repository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    fun setThemeMode(mode: ThemeMode) = launch { repository.setThemeMode(mode) }
    fun setAccent(accent: AccentColor) = launch { repository.setAccent(accent) }
    fun setDynamicColor(enabled: Boolean) = launch { repository.setDynamicColor(enabled) }
    fun setAmoled(enabled: Boolean) = launch { repository.setAmoled(enabled) }
    fun setAnimationSpeed(speed: Float) = launch { repository.setAnimationSpeed(speed) }
    fun setAnimationsEnabled(enabled: Boolean) = launch { repository.setAnimationsEnabled(enabled) }
    fun setBatterySaver(enabled: Boolean) = launch {
        repository.setBatterySaver(enabled)
        // Battery saver implies calmer motion by default.
        if (enabled) repository.setAnimationSpeed(0.6f)
    }
    fun setNotifications(enabled: Boolean) = launch { repository.setNotifications(enabled) }
    fun setDeveloperMode(enabled: Boolean) = launch { repository.setDeveloperMode(enabled) }
    fun setLanguage(language: String) = launch { repository.setLanguage(language) }
    fun acceptResponsibleUse() = launch { repository.setAcceptedResponsibleUse(true) }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
