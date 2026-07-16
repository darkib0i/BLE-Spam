package com.blespam.app.ble

/** Small, allocation-light helpers for converting between hex strings and byte arrays. */
object HexUtils {

    private val HEX_CHARS = "0123456789ABCDEF".toCharArray()

    /** Parses a hex string (spaces/colons/0x prefixes tolerated) into bytes, or empty on failure. */
    fun decode(input: String): ByteArray {
        val cleaned = input
            .replace("0x", "", ignoreCase = true)
            .replace(Regex("[^0-9a-fA-F]"), "")
        if (cleaned.isEmpty() || cleaned.length % 2 != 0) return ByteArray(0)
        return ByteArray(cleaned.length / 2) { i ->
            val hi = Character.digit(cleaned[i * 2], 16)
            val lo = Character.digit(cleaned[i * 2 + 1], 16)
            ((hi shl 4) or lo).toByte()
        }
    }

    /** Returns true when [input] is a well-formed, even-length hex payload. */
    fun isValid(input: String): Boolean {
        val cleaned = input.replace("0x", "", ignoreCase = true).replace(Regex("[^0-9a-fA-F]"), "")
        return cleaned.isNotEmpty() && cleaned.length % 2 == 0
    }

    /** Encodes bytes as an upper-case, space-separated hex string for the payload preview. */
    fun encode(bytes: ByteArray, grouped: Boolean = true): String {
        val sb = StringBuilder(bytes.size * 3)
        bytes.forEachIndexed { index, b ->
            val v = b.toInt() and 0xFF
            sb.append(HEX_CHARS[v ushr 4])
            sb.append(HEX_CHARS[v and 0x0F])
            if (grouped && index != bytes.lastIndex) sb.append(' ')
        }
        return sb.toString()
    }
}
