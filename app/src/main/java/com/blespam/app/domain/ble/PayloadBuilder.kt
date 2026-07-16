package com.blespam.app.domain.ble

import android.bluetooth.le.AdvertiseData
import android.os.ParcelUuid
import com.blespam.app.domain.model.AdvertisingConfig
import com.blespam.app.domain.model.AdvertisingMode
import java.util.UUID
import kotlin.random.Random

/**
 * Turns an [AdvertisingConfig] into a platform [AdvertiseData] and produces a
 * human-readable hex preview for the visual payload editor.
 *
 * Design notes:
 *  - Legacy BLE advertising has a hard 31-byte data budget. We never try to
 *    exceed it; [validate] reports overflow so the UI can warn the user instead
 *    of the OS silently rejecting the broadcast.
 *  - The "…-style" builders only reproduce the *shape* of well-known formats
 *    (company IDs, type bytes, length fields) so a scanner/parser can be tested
 *    against them. They do not replicate proprietary payloads.
 */
object PayloadBuilder {

    const val LEGACY_MAX_BYTES = 31

    // Well-known company identifiers, used purely to shape test packets.
    private const val COMPANY_APPLE = 0x004C
    private const val COMPANY_SAMSUNG = 0x0075
    private const val COMPANY_MICROSOFT = 0x0006

    // Assigned 16-bit service UUIDs referenced by the structural test modes.
    private val FAST_PAIR_UUID = ParcelUuid.fromString("0000FE2C-0000-1000-8000-00805F9B34FB")

    /**
     * Build the [AdvertiseData] for [config]. Throws nothing: callers should run
     * [validate] first to surface size problems in the UI.
     */
    fun build(config: AdvertisingConfig): AdvertiseData {
        val builder = AdvertiseData.Builder()
            .setIncludeTxPowerLevel(true)
            .setIncludeDeviceName(config.includeDeviceName)

        when (config.mode) {
            AdvertisingMode.GENERIC -> {
                // Flags-only advertisement; nothing else needed.
            }

            AdvertisingMode.CUSTOM_LOCAL_NAME -> {
                // The name itself is applied via setIncludeDeviceName + adapter
                // name at the advertiser layer; here we optionally attach a
                // service UUID so the packet is non-empty.
                config.serviceUuid?.let { builder.addServiceUuid(ParcelUuid.fromString(it)) }
            }

            AdvertisingMode.RANDOM_MANUFACTURER -> {
                val id = config.manufacturerId ?: Random.nextInt(0x0000, 0xFFFF)
                val data = config.manufacturerData.orIfEmpty { randomBytes(config.payloadSize) }
                builder.addManufacturerData(id, data)
            }

            AdvertisingMode.RANDOM_UUID -> {
                builder.addServiceUuid(ParcelUuid(UUID.randomUUID()))
            }

            AdvertisingMode.APPLE_STYLE -> {
                // [type][length][payload] shape under the Apple company id.
                val payload = config.manufacturerData.orIfEmpty { randomBytes(8) }
                val framed = byteArrayOf(0x02, payload.size.toByte()) + payload
                builder.addManufacturerData(COMPANY_APPLE, framed)
            }

            AdvertisingMode.SAMSUNG_STYLE -> {
                val payload = config.manufacturerData.orIfEmpty { randomBytes(8) }
                val framed = byteArrayOf(0x01, 0x00) + payload
                builder.addManufacturerData(COMPANY_SAMSUNG, framed)
            }

            AdvertisingMode.GOOGLE_FAST_PAIR -> {
                // Fast Pair advertises a 3-byte model id in service data.
                val model = config.serviceData.orIfEmpty { randomBytes(3) }
                builder.addServiceData(FAST_PAIR_UUID, model)
            }

            AdvertisingMode.MICROSOFT_SWIFT_PAIR -> {
                // Swift Pair beacon shape: [0x03,0x00,0x80] + optional name.
                val header = byteArrayOf(0x03, 0x00, 0x80.toByte())
                val name = (config.localName ?: "BLE-Test").encodeToByteArray().take(8).toByteArray()
                builder.addManufacturerData(COMPANY_MICROSOFT, header + name)
            }

            AdvertisingMode.CUSTOM_PAYLOAD -> {
                config.serviceUuid?.let { builder.addServiceUuid(ParcelUuid.fromString(it)) }
                if (config.manufacturerData.isNotEmpty()) {
                    builder.addManufacturerData(
                        config.manufacturerId ?: 0xFFFF,
                        config.manufacturerData,
                    )
                }
                if (config.serviceData.isNotEmpty() && config.serviceDataUuid != null) {
                    builder.addServiceData(
                        ParcelUuid.fromString(config.serviceDataUuid),
                        config.serviceData,
                    )
                }
            }

            AdvertisingMode.RANDOM_PAYLOAD -> {
                builder.addManufacturerData(
                    config.manufacturerId ?: 0xFFFF,
                    randomBytes(config.payloadSize.coerceIn(1, 24)),
                )
            }
        }

        return builder.build()
    }

    /** Random test bytes, clamped so a single field can never overflow the packet. */
    fun randomBytes(size: Int): ByteArray =
        Random.nextBytes(size.coerceIn(1, LEGACY_MAX_BYTES - 4))

    /** ByteArray has no stdlib `ifEmpty`, so provide a tiny local equivalent. */
    private inline fun ByteArray.orIfEmpty(default: () -> ByteArray): ByteArray =
        if (isEmpty()) default() else this

    /**
     * Estimate the encoded size of [config] and report whether it fits in a
     * legacy advertisement. This mirrors how the controller framing adds a
     * 2-byte AD header per structure plus a company/UUID prefix.
     */
    fun validate(config: AdvertisingConfig): ValidationResult {
        var bytes = 3 // flags AD structure
        if (config.includeDeviceName) bytes += 2 + (config.localName?.length ?: 8)
        config.serviceUuid?.let { bytes += 2 + 16 }
        if (config.manufacturerData.isNotEmpty()) bytes += 2 + 2 + config.manufacturerData.size
        if (config.serviceData.isNotEmpty()) bytes += 2 + 2 + config.serviceData.size

        val fits = bytes <= LEGACY_MAX_BYTES
        return ValidationResult(
            estimatedBytes = bytes,
            fitsLegacy = fits,
            message = if (fits) {
                "$bytes / $LEGACY_MAX_BYTES bytes"
            } else {
                "Payload is $bytes bytes — over the ${LEGACY_MAX_BYTES}-byte legacy limit"
            },
        )
    }

    /**
     * Produce a "hexdump"-style preview of what the assembled AD structures look
     * like, for the visual payload editor. This is a representation of the AD
     * structures, not a raw radio capture.
     */
    fun hexPreview(config: AdvertisingConfig): List<HexLine> {
        val lines = mutableListOf<HexLine>()
        lines += HexLine("Flags", byteArrayOf(0x02, 0x01, 0x06))

        if (config.includeDeviceName) {
            val name = (config.localName ?: "BLE Spam").encodeToByteArray()
            lines += HexLine("Complete Local Name", byteArrayOf((name.size + 1).toByte(), 0x09) + name)
        }
        config.serviceUuid?.let {
            val uuidBytes = uuidToLittleEndian(UUID.fromString(it))
            lines += HexLine("128-bit Service UUID", byteArrayOf(0x11, 0x07) + uuidBytes)
        }
        if (config.manufacturerData.isNotEmpty()) {
            val id = config.manufacturerId ?: 0xFFFF
            val idBytes = byteArrayOf((id and 0xFF).toByte(), ((id shr 8) and 0xFF).toByte())
            val body = idBytes + config.manufacturerData
            lines += HexLine("Manufacturer Data", byteArrayOf((body.size + 1).toByte(), 0xFF.toByte()) + body)
        }
        if (config.serviceData.isNotEmpty()) {
            lines += HexLine("Service Data", byteArrayOf((config.serviceData.size + 1).toByte(), 0x16) + config.serviceData)
        }
        return lines
    }

    private fun uuidToLittleEndian(uuid: UUID): ByteArray {
        val b = ByteArray(16)
        var msb = uuid.mostSignificantBits
        var lsb = uuid.leastSignificantBits
        for (i in 0 until 8) { b[15 - i] = (msb and 0xFF).toByte(); msb = msb shr 8 }
        for (i in 0 until 8) { b[7 - i] = (lsb and 0xFF).toByte(); lsb = lsb shr 8 }
        return b
    }

    data class ValidationResult(
        val estimatedBytes: Int,
        val fitsLegacy: Boolean,
        val message: String,
    )

    data class HexLine(val label: String, val bytes: ByteArray) {
        val hex: String get() = bytes.joinToString(" ") { "%02X".format(it) }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is HexLine) return false
            return label == other.label && bytes.contentEquals(other.bytes)
        }

        override fun hashCode(): Int = 31 * label.hashCode() + bytes.contentHashCode()
    }
}
