package com.blespam.app.domain.model

/**
 * The set of BLE advertising payload builders the app supports.
 *
 * Every mode produces an advertisement that is assembled through the standard
 * Android [android.bluetooth.le.BluetoothLeAdvertiser] API and therefore stays
 * within the platform's size and rate limits. The "…-style" modes reproduce the
 * *structure* of well-known advertisement formats for interoperability testing
 * (e.g. verifying that your own scanner parses them correctly) — they do not
 * bypass any OS or specification limit.
 */
enum class AdvertisingMode(
    val displayName: String,
    val description: String,
) {
    GENERIC(
        "Generic BLE Advertisement",
        "A minimal, spec-compliant connectable advertisement with flags only.",
    ),
    RANDOM_MANUFACTURER(
        "Random Manufacturer Data",
        "Random manufacturer-specific data under a configurable company ID.",
    ),
    CUSTOM_LOCAL_NAME(
        "Custom Local Name",
        "Advertises a configurable complete local name.",
    ),
    RANDOM_UUID(
        "Random UUID Advertisement",
        "Advertises one or more randomly generated 128-bit service UUIDs.",
    ),
    APPLE_STYLE(
        "Apple-style Nearby Format",
        "Structure of Apple continuity manufacturer data (0x004C) for parser interoperability testing.",
    ),
    SAMSUNG_STYLE(
        "Samsung-style Nearby Format",
        "Structure of Samsung manufacturer data (0x0075) for parser interoperability testing.",
    ),
    GOOGLE_FAST_PAIR(
        "Google Fast Pair Structure",
        "Fast Pair service-data (0xFE2C) structure for testing.",
    ),
    MICROSOFT_SWIFT_PAIR(
        "Microsoft Swift Pair Structure",
        "Swift Pair manufacturer data (0x0006) structure for testing.",
    ),
    CUSTOM_PAYLOAD(
        "Custom Payload Builder",
        "Fully user-defined advertisement built from the visual payload editor.",
    ),
    RANDOM_PAYLOAD(
        "Random Payload Generator",
        "Randomised, size-bounded payloads for fuzz-style scanner testing.",
    );

    val requiresParserDisclaimer: Boolean
        get() = this == APPLE_STYLE || this == SAMSUNG_STYLE ||
            this == GOOGLE_FAST_PAIR || this == MICROSOFT_SWIFT_PAIR
}
