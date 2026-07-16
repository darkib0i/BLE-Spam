package com.blespam.app.ui.advanced

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blespam.app.data.repository.ConfigRepository
import com.blespam.app.domain.ble.AdvertisingController
import com.blespam.app.domain.ble.BleEnvironment
import com.blespam.app.domain.device.DeviceInfoProvider
import com.blespam.app.domain.export.ConfigSerializer
import com.blespam.app.domain.model.BleCapabilities
import com.blespam.app.domain.model.HistoryRecord
import com.blespam.app.domain.model.SavedConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DeviceReport(
    val model: String,
    val androidVersion: String,
    val capabilities: BleCapabilities,
    val ignoringBatteryOptimizations: Boolean,
    val batteryLevel: Int,
)

/**
 * Backs the Advanced tab: history, saved configs, favorites, export/import,
 * statistics and the device/BLE capability report.
 */
@HiltViewModel
class AdvancedViewModel @Inject constructor(
    private val repository: ConfigRepository,
    private val environment: BleEnvironment,
    private val deviceInfo: DeviceInfoProvider,
    private val serializer: ConfigSerializer,
    private val controller: AdvertisingController,
) : ViewModel() {

    val history: StateFlow<List<HistoryRecord>> = repository.history
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedConfigs: StateFlow<List<SavedConfig>> = repository.savedConfigs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<SavedConfig>> = repository.favorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalPackets: StateFlow<Long> = repository.totalPackets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalSessions: StateFlow<Int> = repository.historyCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalDuration: StateFlow<Long> = repository.totalDuration
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages = _messages.asSharedFlow()

    fun deviceReport(): DeviceReport = DeviceReport(
        model = deviceInfo.deviceModel(),
        androidVersion = deviceInfo.androidVersion(),
        capabilities = environment.capabilities(),
        ignoringBatteryOptimizations = deviceInfo.isIgnoringBatteryOptimizations(),
        batteryLevel = deviceInfo.batteryLevel(),
    )

    fun toggleFavorite(config: SavedConfig) {
        viewModelScope.launch { repository.setFavorite(config.id, !config.isFavorite) }
    }

    fun delete(config: SavedConfig) {
        viewModelScope.launch { repository.delete(config) }
    }

    fun loadConfig(config: SavedConfig) {
        controller.setConfig(config.config)
        _messages.tryEmit("Loaded \"${config.name}\"")
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            _messages.tryEmit("History cleared")
        }
    }

    /** Serialize saved configs to a shareable JSON string (Export Logs). */
    suspend fun exportConfigsJson(): String = serializer.exportConfigs(repository.exportConfigs())

    /** Serialize history to JSON (Export Logs). */
    suspend fun exportHistoryJson(): String = serializer.exportHistory(repository.exportHistory())

    fun importConfigsJson(json: String) {
        viewModelScope.launch {
            val imported = serializer.importConfigs(json)
            if (imported.isEmpty()) {
                _messages.tryEmit("Nothing to import or invalid file.")
            } else {
                repository.importConfigs(imported)
                _messages.tryEmit("Imported ${imported.size} configuration(s)")
            }
        }
    }
}
