package com.blespam.app.data.model

/**
 * The catalogue of BLE advertising payload shapes the app can construct.
 *
 * Every mode stays strictly within what the Android [android.bluetooth.le.BluetoothLeAdvertiser]
 * API allows: legacy advertising payloads are capped at 31 bytes, and we never attempt to
 * spoof, flood, or bypass any OS/hardware limit. The "*-style" modes reproduce the *public*
 * structure of well-known advertisement formats purely so developers can validate that their
 * own scanners/receivers parse them correctly (interoperability testing).
 */
enum class AdvertisingMode(
    val id: String,
    val displayName: String,
    val description: String,
    /** Whether the mode auto-generates its payload (vs. using the user's manual fields). */
    val isGenerated: Boolean = false,
) {
    GENERIC(
        id = "generic",
        displayName = "Generic Advertisement",
        description = "A standard connectable BLE advertisement with the configured local name and service UUID.",
    ),
    RANDOM_MANUFACTURER(
        id = "random_manufacturer",
        displayName = "Random Manufacturer Data",
        description = "Emits a random manufacturer-specific data block on each session for parser fuzz testing.",
        isGenerated = true,
    ),
    CUSTOM_LOCAL_NAME(
        id = "custom_local_name",
        displayName = "Custom Local Name",
        description = "Advertises only a complete local name — useful for name-based discovery tests.",
    ),
    RANDOM_UUID(
        id = "random_uuid",
        displayName = "Random UUID",
        description = "Advertises a freshly generated 128-bit service UUID each session.",
        isGenerated = true,
    ),
    APPLE_NEARBY(
        id = "apple_nearby",
        displayName = "Apple-style Nearby (test)",
        description = "Reproduces the public Apple continuity manufacturer-data layout for interoperability testing only.",
    ),
    SAMSUNG_NEARBY(
        id = "samsung_nearby",
        displayName = "Samsung-style Nearby (test)",
        description = "Reproduces the public Samsung manufacturer-data layout for interoperability testing only.",
    ),
    GOOGLE_FAST_PAIR(
        id = "google_fast_pair",
        displayName = "Google Fast Pair-style (test)",
        description = "Emits a Fast Pair service-data structure with a sample model ID for receiver testing.",
    ),
    MICROSOFT_SWIFT_PAIR(
        id = "microsoft_swift_pair",
        displayName = "Microsoft Swift Pair-style (test)",
        description = "Emits a Swift Pair manufacturer-data structure for receiver testing.",
    ),
    CUSTOM_PAYLOAD(
        id = "custom_payload",
        displayName = "Custom Payload Builder",
        description = "Fully manual: you control every advertised field and inspect the raw hex.",
    ),
    RANDOM_PAYLOAD(
        id = "random_payload",
        displayName = "Random Payload Generator",
        description = "Generates a random valid payload within Android's 31-byte legacy limit.",
        isGenerated = true,
    );

    companion object {
        fun fromId(id: String): AdvertisingMode = entries.firstOrNull { it.id == id } ?: GENERIC
    }
}
