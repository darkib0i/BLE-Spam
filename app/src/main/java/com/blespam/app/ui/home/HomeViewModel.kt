package com.blespam.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blespam.app.domain.ble.AdvertisingController
import com.blespam.app.domain.ble.BleEnvironment
import com.blespam.app.domain.device.DeviceInfoProvider
import com.blespam.app.domain.model.BleCapabilities
import com.blespam.app.domain.model.PermissionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val capabilities: BleCapabilities = BleCapabilities(),
    val permissions: PermissionState = PermissionState(),
    val batteryLevel: Int = -1,
    val isCharging: Boolean = false,
    val isAdvertising: Boolean = false,
)

/**
 * Backs the Home screen: exposes the live capability/permission/battery snapshot
 * and refreshes it whenever the screen resumes (permissions can change while the
 * app is backgrounded in Settings).
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val environment: BleEnvironment,
    private val deviceInfo: DeviceInfoProvider,
    private val controller: AdvertisingController,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val batteryLevel: StateFlow<Int> = deviceInfo.batteryLevelFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), -1)

    init {
        refresh()
        viewModelScope.launch {
            controller.stats.collect { stats ->
                _uiState.value = _uiState.value.copy(isAdvertising = stats.isAdvertising)
            }
        }
    }

    /** Re-read everything that can change outside the app (permissions, BT toggle). */
    fun refresh() {
        _uiState.value = _uiState.value.copy(
            capabilities = environment.capabilities(),
            permissions = environment.permissionState(),
            batteryLevel = deviceInfo.batteryLevel(),
            isCharging = deviceInfo.isCharging(),
        )
    }
}
