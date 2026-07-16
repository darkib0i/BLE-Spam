package com.blespam.app.ui.control

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blespam.app.domain.ble.AdvertisingController
import com.blespam.app.domain.ble.BleEnvironment
import com.blespam.app.domain.model.AdvertiseResult
import com.blespam.app.domain.model.AdvertisingConfig
import com.blespam.app.domain.model.AdvertisingStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs the Control screen. Owns the start/stop action and surfaces one-off
 * error messages (e.g. "Bluetooth is off") through [messages] so the UI can
 * show a snackbar without duplicating on recomposition.
 */
@HiltViewModel
class ControlViewModel @Inject constructor(
    private val controller: AdvertisingController,
    private val environment: BleEnvironment,
) : ViewModel() {

    val stats: StateFlow<AdvertisingStats> = controller.stats

    val config: StateFlow<AdvertisingConfig> = controller.config

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages = _messages.asSharedFlow()

    /** Whether the environment currently permits advertising. */
    fun canAdvertise(): Boolean = environment.hasAdvertisePermission()

    fun toggle() {
        viewModelScope.launch {
            if (!environment.capabilities().bluetoothEnabled) {
                _messages.tryEmit("Turn on Bluetooth to start advertising.")
                return@launch
            }
            when (val result = controller.toggle()) {
                is AdvertiseResult.Failure -> _messages.tryEmit(result.reason)
                AdvertiseResult.Success -> Unit
            }
        }
    }
}
