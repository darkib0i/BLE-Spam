package com.blespam.app.ui.advanced

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blespam.app.ble.BleStateMonitor
import com.blespam.app.data.local.HistoryEntity
import com.blespam.app.data.local.SavedConfigEntity
import com.blespam.app.data.model.BleEnvironment
import com.blespam.app.data.repository.ConfigRepository
import com.blespam.app.data.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

/**
 * Backs the Advanced tab: history, saved configs, favorites, aggregate statistics, device/BLE
 * capability info, and JSON export/import of logs and configurations.
 */
@HiltViewModel
class AdvancedViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val historyRepository: HistoryRepository,
    private val configRepository: ConfigRepository,
    bleStateMonitor: BleStateMonitor,
) : ViewModel() {

    data class UiState(
        val history: List<HistoryEntity> = emptyList(),
        val savedConfigs: List<SavedConfigEntity> = emptyList(),
        val favorites: List<SavedConfigEntity> = emptyList(),
        val totalSessions: Int = 0,
        val totalPackets: Long = 0,
        val environment: BleEnvironment = BleEnvironment(),
    )

    val uiState: StateFlow<UiState> = combine(
        historyRepository.observeRecent(),
        configRepository.observeSavedConfigs(),
        configRepository.observeFavorites(),
        historyRepository.observeTotalPackets(),
        bleStateMonitor.environment,
    ) { history, configs, favorites, packets, env ->
        UiState(
            history = history,
            savedConfigs = configs,
            favorites = favorites,
            totalSessions = history.size,
            totalPackets = packets,
            environment = env,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UiState(environment = bleStateMonitor.snapshot()),
    )

    fun toggleFavorite(entity: SavedConfigEntity) {
        viewModelScope.launch { configRepository.setFavorite(entity.id, !entity.isFavorite) }
    }

    fun deleteConfig(entity: SavedConfigEntity) {
        viewModelScope.launch { configRepository.delete(entity) }
    }

    fun clearHistory() {
        viewModelScope.launch { historyRepository.clear() }
    }

    /** Serializes the history log to JSON and writes it to the user-picked [uri]. */
    fun exportLogs(uri: Uri, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = runCatching {
                val entries = historyRepository.exportAll()
                val json = JSONArray().apply {
                    entries.forEach { put(it.toJson()) }
                }
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(json.toString(2).toByteArray())
                    }
                }
            }.isSuccess
            onDone(ok)
        }
    }

    /** Serializes saved configurations to JSON for backup/sharing. */
    fun exportConfigs(uri: Uri, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = runCatching {
                val configs = configRepository.exportAll()
                val json = JSONArray().apply { configs.forEach { put(it.toJson()) } }
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(json.toString(2).toByteArray())
                    }
                }
            }.isSuccess
            onDone(ok)
        }
    }

    /** Reads a JSON config export from [uri] and merges it into the library. */
    fun importConfigs(uri: Uri, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = runCatching {
                val text = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                } ?: return@runCatching false
                val array = JSONArray(text)
                val entities = (0 until array.length()).map { i -> array.getJSONObject(i).toConfigEntity() }
                configRepository.importAll(entities)
                true
            }.getOrDefault(false)
            onDone(ok)
        }
    }
}

private fun HistoryEntity.toJson(): JSONObject = JSONObject().apply {
    put("modeId", modeId)
    put("modeName", modeName)
    put("localName", localName)
    put("txPower", txPower)
    put("speed", speed)
    put("estimatedPackets", estimatedPackets)
    put("durationMs", durationMs)
    put("payloadHex", payloadHex)
    put("succeeded", succeeded)
    put("errorMessage", errorMessage ?: JSONObject.NULL)
    put("startedAt", startedAt)
}

private fun SavedConfigEntity.toJson(): JSONObject = JSONObject().apply {
    put("name", name)
    put("modeId", modeId)
    put("localName", localName)
    put("includeDeviceName", includeDeviceName)
    put("serviceUuid", serviceUuid)
    put("includeServiceUuid", includeServiceUuid)
    put("manufacturerId", manufacturerId)
    put("manufacturerDataHex", manufacturerDataHex)
    put("serviceDataHex", serviceDataHex)
    put("txPower", txPower)
    put("speed", speed)
    put("includeTxPower", includeTxPower)
    put("connectable", connectable)
    put("durationMs", durationMs)
    put("isFavorite", isFavorite)
    put("createdAt", createdAt)
}

private fun JSONObject.toConfigEntity(): SavedConfigEntity = SavedConfigEntity(
    id = 0,
    name = optString("name", "Imported"),
    modeId = optString("modeId", "generic"),
    localName = optString("localName", "BLE-Spam"),
    includeDeviceName = optBoolean("includeDeviceName", true),
    serviceUuid = optString("serviceUuid", ""),
    includeServiceUuid = optBoolean("includeServiceUuid", true),
    manufacturerId = optInt("manufacturerId", 0xFFFF),
    manufacturerDataHex = optString("manufacturerDataHex", ""),
    serviceDataHex = optString("serviceDataHex", ""),
    txPower = optString("txPower", "MEDIUM"),
    speed = optString("speed", "BALANCED"),
    includeTxPower = optBoolean("includeTxPower", true),
    connectable = optBoolean("connectable", true),
    durationMs = optInt("durationMs", 0),
    isFavorite = optBoolean("isFavorite", false),
    createdAt = optLong("createdAt", System.currentTimeMillis()),
)
