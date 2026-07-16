package com.blespam.app.domain.util

/** Small, allocation-light hex <-> bytes helpers used across persistence and the payload editor. */
object HexUtils {

    fun toHex(bytes: ByteArray, separator: String = ""): String =
        bytes.joinToString(separator) { "%02X".format(it) }

    /**
     * Parse a hex string into bytes. Ignores spaces and common separators, and
     * is tolerant of an odd trailing nibble (dropped) so live editing never throws.
     */
    fun fromHex(input: String): ByteArray {
        val cleaned = input.filter { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }
        val even = if (cleaned.length % 2 == 0) cleaned else cleaned.dropLast(1)
        if (even.isEmpty()) return ByteArray(0)
        return ByteArray(even.length / 2) { i ->
            even.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
    }

    fun isValidHex(input: String): Boolean {
        val cleaned = input.filter { !it.isWhitespace() }
        return cleaned.isEmpty() || cleaned.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }
    }
}
