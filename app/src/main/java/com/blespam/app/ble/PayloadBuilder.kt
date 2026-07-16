package com.blespam.app.ble

import android.bluetooth.le.AdvertiseData
import android.os.ParcelUuid
import com.blespam.app.data.model.AdvertisingMode
import com.blespam.app.data.model.BleConfig
import java.util.UUID
import kotlin.random.Random

/**
 * Turns a [BleConfig] into a concrete [AdvertiseData] plus a human-readable hex preview.
 *
 * IMPORTANT: everything here is constrained to Android's public advertising API. Legacy payloads
 * are limited to 31 bytes by the platform; if a configuration would exceed that we surface a
 * validation error rather than trying to force it through. The "*-style" builders reproduce only
 * the publicly documented *structure* of well-known formats so developers can verify their own
 * receivers parse them — no real device identities are impersonated.
 */
class PayloadBuilder {

    /** Result of building a payload: the platform object plus a preview and validation status. */
    data class Built(
        val data: AdvertiseData,
        val previewHex: String,
        val estimatedBytes: Int,
        val error: String? = null,
    ) {
        val isValid: Boolean get() = error == null
    }

    fun build(config: BleConfig): Built {
        return try {
            when (config.mode) {
                AdvertisingMode.GENERIC -> generic(config)
                AdvertisingMode.CUSTOM_LOCAL_NAME -> localNameOnly(config)
                AdvertisingMode.RANDOM_UUID -> randomUuid(config)
                AdvertisingMode.RANDOM_MANUFACTURER -> randomManufacturer(config)
                AdvertisingMode.APPLE_NEARBY -> manufacturerStructure(config, APPLE_COMPANY_ID, sampleAppleContinuity())
                AdvertisingMode.SAMSUNG_NEARBY -> manufacturerStructure(config, SAMSUNG_COMPANY_ID, sampleSamsung())
                AdvertisingMode.GOOGLE_FAST_PAIR -> fastPair(config)
                AdvertisingMode.MICROSOFT_SWIFT_PAIR -> manufacturerStructure(config, MICROSOFT_COMPANY_ID, sampleSwiftPair())
                AdvertisingMode.CUSTOM_PAYLOAD -> custom(config)
                AdvertisingMode.RANDOM_PAYLOAD -> randomPayload(config)
            }
        } catch (t: Throwable) {
            Built(AdvertiseData.Builder().build(), "", 0, t.message ?: "Failed to build payload")
        }
    }

    // --- Individual builders -------------------------------------------------------------------

    private fun generic(config: BleConfig): Built {
        val builder = AdvertiseData.Builder()
            .setIncludeDeviceName(config.includeDeviceName)
            .setIncludeTxPowerLevel(config.includeTxPower)
        var bytes = 0
        val preview = StringBuilder()
        if (config.includeServiceUuid) {
            val uuid = parseUuid(config.serviceUuid) ?: return invalid("Invalid service UUID")
            builder.addServiceUuid(ParcelUuid(uuid))
            bytes += uuidLen(uuid)
            preview.append("UUID: ").append(config.serviceUuid).append('\n')
        }
        if (config.manufacturerDataHex.isNotBlank()) {
            val md = HexUtils.decode(config.manufacturerDataHex)
            builder.addManufacturerData(config.manufacturerId, md)
            bytes += md.size + 2
            preview.append("Mfr[").append(hex4(config.manufacturerId)).append("]: ")
                .append(HexUtils.encode(md)).append('\n')
        }
        return finalize(builder, preview.toString(), bytes, config)
    }

    private fun localNameOnly(config: BleConfig): Built {
        val builder = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .setIncludeTxPowerLevel(config.includeTxPower)
        val nameBytes = config.localName.toByteArray(Charsets.UTF_8).size + 2
        return finalize(builder, "Local Name: ${config.localName}", nameBytes, config)
    }

    private fun randomUuid(config: BleConfig): Built {
        val uuid = UUID.randomUUID()
        val builder = AdvertiseData.Builder()
            .setIncludeDeviceName(config.includeDeviceName)
            .addServiceUuid(ParcelUuid(uuid))
        return finalize(builder, "Random UUID: $uuid", 16, config)
    }

    private fun randomManufacturer(config: BleConfig): Built {
        // Random block of 4..16 bytes, always within the legacy budget.
        val size = Random.nextInt(4, 17)
        val data = ByteArray(size).also { Random.nextBytes(it) }
        val builder = AdvertiseData.Builder()
            .addManufacturerData(config.manufacturerId, data)
            .setIncludeTxPowerLevel(config.includeTxPower)
        return finalize(
            builder,
            "Mfr[${hex4(config.manufacturerId)}]: ${HexUtils.encode(data)}",
            data.size + 2,
            config,
        )
    }

    private fun manufacturerStructure(config: BleConfig, companyId: Int, data: ByteArray): Built {
        val builder = AdvertiseData.Builder()
            .addManufacturerData(companyId, data)
            .setIncludeTxPowerLevel(false)
        return finalize(
            builder,
            "Mfr[${hex4(companyId)}]: ${HexUtils.encode(data)}",
            data.size + 2,
            config,
            includeNameOverride = false,
        )
    }

    private fun fastPair(config: BleConfig): Built {
        // Fast Pair advertises a 24-bit model ID inside service data under UUID 0xFE2C.
        val serviceUuid = ParcelUuid.fromString("0000FE2C-0000-1000-8000-00805F9B34FB")
        val modelId = byteArrayOf(0x00, 0x00, 0x00) // Sample/placeholder model ID for testing.
        val builder = AdvertiseData.Builder()
            .addServiceData(serviceUuid, modelId)
            .setIncludeTxPowerLevel(false)
        return finalize(
            builder,
            "Fast Pair svc FE2C: ${HexUtils.encode(modelId)}",
            modelId.size + 4,
            config,
            includeNameOverride = false,
        )
    }

    private fun custom(config: BleConfig): Built {
        val builder = AdvertiseData.Builder()
            .setIncludeDeviceName(config.includeDeviceName)
            .setIncludeTxPowerLevel(config.includeTxPower)
        var bytes = 0
        val preview = StringBuilder()

        if (config.includeServiceUuid && config.serviceUuid.isNotBlank()) {
            val uuid = parseUuid(config.serviceUuid) ?: return invalid("Invalid service UUID")
            builder.addServiceUuid(ParcelUuid(uuid))
            bytes += uuidLen(uuid)
            preview.append("UUID: ").append(config.serviceUuid).append('\n')
        }
        if (config.manufacturerDataHex.isNotBlank()) {
            if (!HexUtils.isValid(config.manufacturerDataHex)) return invalid("Manufacturer data is not valid hex")
            val md = HexUtils.decode(config.manufacturerDataHex)
            builder.addManufacturerData(config.manufacturerId, md)
            bytes += md.size + 2
            preview.append("Mfr[").append(hex4(config.manufacturerId)).append("]: ")
                .append(HexUtils.encode(md)).append('\n')
        }
        if (config.serviceDataHex.isNotBlank()) {
            if (!HexUtils.isValid(config.serviceDataHex)) return invalid("Service data is not valid hex")
            val uuid = parseUuid(config.serviceUuid) ?: return invalid("Service data requires a valid UUID")
            val sd = HexUtils.decode(config.serviceDataHex)
            builder.addServiceData(ParcelUuid(uuid), sd)
            bytes += sd.size + 4
            preview.append("SvcData: ").append(HexUtils.encode(sd)).append('\n')
        }
        return finalize(builder, preview.toString().ifBlank { "Empty payload" }, bytes, config)
    }

    private fun randomPayload(config: BleConfig): Built {
        // Fill up to a random slice of the legacy budget with random manufacturer data.
        val budget = BleConfig.MAX_LEGACY_PAYLOAD_BYTES - 4
        val size = Random.nextInt(2, budget)
        val data = ByteArray(size).also { Random.nextBytes(it) }
        val builder = AdvertiseData.Builder().addManufacturerData(config.manufacturerId, data)
        return finalize(
            builder,
            "Random ${data.size}B: ${HexUtils.encode(data)}",
            data.size + 2,
            config,
        )
    }

    // --- Helpers -------------------------------------------------------------------------------

    private fun finalize(
        builder: AdvertiseData.Builder,
        preview: String,
        bytes: Int,
        config: BleConfig,
        includeNameOverride: Boolean? = null,
    ): Built {
        val includeName = includeNameOverride ?: config.includeDeviceName
        // The device name is added by the advertiser via the local name; account for it in the size.
        val nameBytes = if (includeName) config.localName.toByteArray(Charsets.UTF_8).size + 2 else 0
        val txBytes = if (config.includeTxPower) 3 else 0
        val total = bytes + nameBytes + txBytes + 3 /* flags AD structure */
        if (total > BleConfig.MAX_LEGACY_PAYLOAD_BYTES) {
            return Built(
                builder.build(),
                preview,
                total,
                "Payload is $total bytes, exceeding the ${BleConfig.MAX_LEGACY_PAYLOAD_BYTES}-byte legacy limit. " +
                    "Shorten the local name or data.",
            )
        }
        return Built(builder.build(), preview.trim(), total)
    }

    private fun invalid(message: String): Built =
        Built(AdvertiseData.Builder().build(), "", 0, message)

    private fun parseUuid(raw: String): UUID? = runCatching {
        val trimmed = raw.trim()
        // Support both 16-bit shorthand (e.g. FEAA) and full 128-bit UUIDs.
        if (trimmed.length == 4) {
            UUID.fromString("0000$trimmed-0000-1000-8000-00805F9B34FB")
        } else {
            UUID.fromString(trimmed)
        }
    }.getOrNull()

    private fun uuidLen(uuid: UUID): Int {
        val s = uuid.toString().uppercase()
        // A 16-bit assigned UUID compresses to 2 bytes; everything else is 16.
        return if (s.startsWith("0000") && s.endsWith("-0000-1000-8000-00805F9B34FB")) 2 else 16
    }

    private fun hex4(v: Int): String = "0x%04X".format(v and 0xFFFF)

    // --- Sample structures for interoperability testing ---------------------------------------
    // These are illustrative, publicly documented layouts filled with placeholder bytes.

    private fun sampleAppleContinuity(): ByteArray = byteArrayOf(
        0x10, 0x05, 0x01, 0x00, 0x00, 0x00, 0x00, // type/length + placeholder status bytes
    )

    private fun sampleSamsung(): ByteArray = byteArrayOf(
        0x42.toByte(), 0x09, 0x01, 0x02, 0x00, 0x00,
    )

    private fun sampleSwiftPair(): ByteArray = byteArrayOf(
        0x03, 0x00, 0x80.toByte(), // Swift Pair scenario header (placeholder)
    )

    companion object {
        // Public Bluetooth SIG company identifiers, used only to shape valid AD structures.
        const val APPLE_COMPANY_ID = 0x004C
        const val SAMSUNG_COMPANY_ID = 0x0075
        const val MICROSOFT_COMPANY_ID = 0x0006
    }
}
