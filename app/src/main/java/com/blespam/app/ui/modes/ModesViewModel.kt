package com.blespam.app.ui.modes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blespam.app.data.repository.ConfigRepository
import com.blespam.app.domain.ble.AdvertisingController
import com.blespam.app.domain.ble.PayloadBuilder
import com.blespam.app.domain.model.AdvInterval
import com.blespam.app.domain.model.AdvertisingConfig
import com.blespam.app.domain.model.AdvertisingMode
import com.blespam.app.domain.model.SavedConfig
import com.blespam.app.domain.model.TxPower
import com.blespam.app.domain.util.HexUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs the Modes screen and the visual payload editor. All edits mutate the
 * shared config held by [AdvertisingController], so the Control screen and the
 * live advertisement stay in sync with what the user is building.
 */
@HiltViewModel
class ModesViewModel @Inject constructor(
    private val controller: AdvertisingController,
    private val repository: ConfigRepository,
) : ViewModel() {

    val config: StateFlow<AdvertisingConfig> = controller.config

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages = _messages.asSharedFlow()

    val modes: List<AdvertisingMode> = AdvertisingMode.entries

    fun selectMode(mode: AdvertisingMode) = controller.updateConfig { it.copy(mode = mode) }

    fun setLocalName(name: String) = controller.updateConfig {
        it.copy(localName = name.ifBlank { null }, includeDeviceName = name.isNotBlank())
    }

    fun setServiceUuid(uuid: String) = controller.updateConfig {
        it.copy(serviceUuid = uuid.ifBlank { null })
    }

    fun setManufacturerId(idHex: String) = controller.updateConfig {
        val id = idHex.toIntOrNull(16)
        it.copy(manufacturerId = id)
    }

    fun setManufacturerData(hex: String) {
        if (!HexUtils.isValidHex(hex)) {
            _messages.tryEmit("Manufacturer data must be valid hex.")
            return
        }
        controller.updateConfig { it.copy(manufacturerData = HexUtils.fromHex(hex)) }
    }

    fun setServiceData(hex: String) {
        if (!HexUtils.isValidHex(hex)) {
            _messages.tryEmit("Service data must be valid hex.")
            return
        }
        controller.updateConfig { it.copy(serviceData = HexUtils.fromHex(hex)) }
    }

    fun setServiceDataUuid(uuid: String) = controller.updateConfig {
        it.copy(serviceDataUuid = uuid.ifBlank { null })
    }

    fun setTxPower(power: TxPower) = controller.updateConfig { it.copy(txPower = power) }
    fun setInterval(interval: AdvInterval) = controller.updateConfig { it.copy(interval = interval) }
    fun setConnectable(connectable: Boolean) = controller.updateConfig { it.copy(connectable = connectable) }
    fun setDuration(millis: Int) = controller.updateConfig { it.copy(durationMillis = millis) }
    fun setPayloadSize(size: Int) = controller.updateConfig { it.copy(payloadSize = size) }

    fun randomizePayload() = controller.updateConfig {
        it.copy(manufacturerData = PayloadBuilder.randomBytes(it.payloadSize))
    }

    /** Live validation + hex preview for the editor. */
    fun validation() = PayloadBuilder.validate(config.value)
    fun hexPreview() = PayloadBuilder.hexPreview(config.value)

    fun saveCurrent(name: String) {
        viewModelScope.launch {
            repository.save(SavedConfig(name = name.ifBlank { "Untitled" }, config = config.value))
            _messages.tryEmit("Saved \"$name\"")
        }
    }
}
