package com.blespam.app.domain.export

import com.blespam.app.domain.model.AdvInterval
import com.blespam.app.domain.model.AdvertisingConfig
import com.blespam.app.domain.model.AdvertisingMode
import com.blespam.app.domain.model.HistoryRecord
import com.blespam.app.domain.model.SavedConfig
import com.blespam.app.domain.model.TxPower
import com.blespam.app.domain.util.HexUtils
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Serializes saved configurations and history to/from JSON for the Advanced tab
 * export/import features. Uses the platform's built-in [org.json] to avoid
 * pulling in a serialization dependency, keeping the APK small.
 *
 * The format is intentionally simple and forward-tolerant: unknown fields are
 * ignored and missing fields fall back to sane defaults, so a file from a newer
 * or older build still imports cleanly.
 */
@Singleton
class ConfigSerializer @Inject constructor() {

    fun exportConfigs(configs: List<SavedConfig>): String {
        val root = JSONObject()
        root.put("type", "ble_spam_configs")
        root.put("version", 1)
        val array = JSONArray()
        configs.forEach { array.put(configToJson(it)) }
        root.put("configs", array)
        return root.toString(2)
    }

    fun importConfigs(json: String): List<SavedConfig> = runCatching {
        val root = JSONObject(json)
        val array = root.optJSONArray("configs") ?: return emptyList()
        buildList {
            for (i in 0 until array.length()) {
                jsonToConfig(array.getJSONObject(i))?.let { add(it) }
            }
        }
    }.getOrDefault(emptyList())

    fun exportHistory(history: List<HistoryRecord>): String {
        val root = JSONObject()
        root.put("type", "ble_spam_history")
        root.put("version", 1)
        val array = JSONArray()
        history.forEach { record ->
            array.put(
                JSONObject().apply {
                    put("mode", record.mode.name)
                    put("txPower", record.txPower.name)
                    put("interval", record.interval.name)
                    put("startedAt", record.startedAt)
                    put("durationMillis", record.durationMillis)
                    put("packetsSent", record.packetsSent)
                    put("estimatedBatteryPerHour", record.estimatedBatteryPerHour.toDouble())
                },
            )
        }
        root.put("history", array)
        return root.toString(2)
    }

    private fun configToJson(saved: SavedConfig): JSONObject {
        val c = saved.config
        return JSONObject().apply {
            put("name", saved.name)
            put("isFavorite", saved.isFavorite)
            put("mode", c.mode.name)
            put("localName", c.localName ?: JSONObject.NULL)
            put("includeDeviceName", c.includeDeviceName)
            put("serviceUuid", c.serviceUuid ?: JSONObject.NULL)
            put("manufacturerId", c.manufacturerId ?: JSONObject.NULL)
            put("manufacturerData", HexUtils.toHex(c.manufacturerData))
            put("serviceData", HexUtils.toHex(c.serviceData))
            put("serviceDataUuid", c.serviceDataUuid ?: JSONObject.NULL)
            put("txPower", c.txPower.name)
            put("interval", c.interval.name)
            put("connectable", c.connectable)
            put("durationMillis", c.durationMillis)
            put("payloadSize", c.payloadSize)
        }
    }

    private fun jsonToConfig(obj: JSONObject): SavedConfig? = runCatching {
        SavedConfig(
            name = obj.optString("name", "Imported"),
            isFavorite = obj.optBoolean("isFavorite", false),
            config = AdvertisingConfig(
                mode = enumOrDefault(obj.optString("mode"), AdvertisingMode.GENERIC),
                localName = obj.optStringOrNull("localName"),
                includeDeviceName = obj.optBoolean("includeDeviceName", false),
                serviceUuid = obj.optStringOrNull("serviceUuid"),
                manufacturerId = if (obj.isNull("manufacturerId")) null else obj.optInt("manufacturerId"),
                manufacturerData = HexUtils.fromHex(obj.optString("manufacturerData")),
                serviceData = HexUtils.fromHex(obj.optString("serviceData")),
                serviceDataUuid = obj.optStringOrNull("serviceDataUuid"),
                txPower = enumOrDefault(obj.optString("txPower"), TxPower.MEDIUM),
                interval = enumOrDefault(obj.optString("interval"), AdvInterval.BALANCED),
                connectable = obj.optBoolean("connectable", true),
                durationMillis = obj.optInt("durationMillis", 0),
                payloadSize = obj.optInt("payloadSize", 16),
            ),
        )
    }.getOrNull()

    private fun JSONObject.optStringOrNull(key: String): String? =
        if (isNull(key)) null else optString(key).ifBlank { null }

    private inline fun <reified T : Enum<T>> enumOrDefault(name: String, default: T): T =
        runCatching { enumValueOf<T>(name) }.getOrDefault(default)
}
