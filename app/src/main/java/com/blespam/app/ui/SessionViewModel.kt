package com.blespam.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blespam.app.ble.AdvertisingManager
import com.blespam.app.ble.BleStateMonitor
import com.blespam.app.ble.PayloadBuilder
import com.blespam.app.data.model.AdvertisingMode
import com.blespam.app.data.model.AdvertisingStats
import com.blespam.app.data.model.BleConfig
import com.blespam.app.data.model.BleEnvironment
import com.blespam.app.data.model.PermissionState
import com.blespam.app.data.repository.ConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Shared, activity-scoped state holder for the current advertising configuration and live session.
 * Home, Broadcast, and Modes all observe/edit the same [config] and [stats] through this VM, so the
 * "current mode" and running state stay consistent as the user moves between tabs.
 */
@HiltViewModel
class SessionViewModel @Inject constructor(
    private val advertisingManager: AdvertisingManager,
    private val bleStateMonitor: BleStateMonitor,
    private val payloadBuilder: PayloadBuilder,
    private val configRepository: ConfigRepository,
) : ViewModel() {

    private val _config = MutableStateFlow(BleConfig())
    val config: StateFlow<BleConfig> = _config.asStateFlow()

    val stats: StateFlow<AdvertisingStats> = advertisingManager.stats

    private val _permissions = MutableStateFlow(PermissionState())
    val permissions: StateFlow<PermissionState> = _permissions.asStateFlow()

    /** Environment (adapter, battery, capabilities) re-emitted on system broadcasts. */
    val environment: StateFlow<BleEnvironment> = bleStateMonitor.environment.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = bleStateMonitor.snapshot(),
    )

    /** Live payload preview for the current config, rebuilt whenever the config changes. */
    private val _preview = MutableStateFlow(PayloadBuilder.Built(
        android.bluetooth.le.AdvertiseData.Builder().build(), "", 0,
    ))
    val preview: StateFlow<PayloadBuilder.Built> = _preview.asStateFlow()

    init {
        rebuildPreview()
    }

    // --- Config editing ------------------------------------------------------------------------

    fun selectMode(mode: AdvertisingMode) = update { it.copy(mode = mode) }
    fun setLocalName(name: String) = update { it.copy(localName = name.take(24)) }
    fun setIncludeDeviceName(include: Boolean) = update { it.copy(includeDeviceName = include) }
    fun setServiceUuid(uuid: String) = update { it.copy(serviceUuid = uuid.trim()) }
    fun setIncludeServiceUuid(include: Boolean) = update { it.copy(includeServiceUuid = include) }
    fun setManufacturerId(id: Int) = update { it.copy(manufacturerId = id and 0xFFFF) }
    fun setManufacturerData(hex: String) = update { it.copy(manufacturerDataHex = hex) }
    fun setServiceData(hex: String) = update { it.copy(serviceDataHex = hex) }
    fun setTxPower(power: com.blespam.app.data.model.TxPowerLevel) = update { it.copy(txPower = power) }
    fun setSpeed(speed: com.blespam.app.data.model.AdvertisingSpeed) = update { it.copy(speed = speed) }
    fun setIncludeTxPower(include: Boolean) = update { it.copy(includeTxPower = include) }
    fun setConnectable(connectable: Boolean) = update { it.copy(connectable = connectable) }
    fun setDurationMs(ms: Int) = update { it.copy(durationMs = ms.coerceIn(0, BleConfig.MAX_DURATION_MS)) }

    fun applyConfig(config: BleConfig) {
        _config.value = config
        rebuildPreview()
    }

    private fun update(transform: (BleConfig) -> BleConfig) {
        _config.value = transform(_config.value)
        rebuildPreview()
    }

    private fun rebuildPreview() {
        _preview.value = payloadBuilder.build(_config.value)
    }

    // --- Permissions ---------------------------------------------------------------------------

    /** Called from the UI after a permission check/result to refresh gating state. */
    fun updatePermissions(state: PermissionState) {
        _permissions.value = state
    }

    fun refreshEnvironment() {
        // Environment is a hot flow; nothing to do but nudge subscribers if needed.
    }

    // --- Advertising control -------------------------------------------------------------------

    val isAdvertising: Boolean get() = stats.value.isAdvertising

    fun toggleAdvertising() {
        if (stats.value.isAdvertising) stop() else start()
    }

    fun start() {
        if (!_permissions.value.canAdvertise) return
        val preview = payloadBuilder.build(_config.value)
        if (!preview.isValid) return
        advertisingManager.start(_config.value)
    }

    fun stop() {
        advertisingManager.stop()
    }

    // --- Saving --------------------------------------------------------------------------------

    fun saveCurrentConfig(name: String) {
        viewModelScope.launch { configRepository.save(_config.value, name.ifBlank { "Config" }) }
    }
}
