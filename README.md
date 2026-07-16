# BLE Spam — Bluetooth Low Energy Testing Suite

A premium, production-quality Android application for **legitimate BLE development, debugging,
interoperability testing, and education**. BLE Spam builds, broadcasts, and inspects Bluetooth Low
Energy advertisements entirely within Android's public BLE APIs — it never attempts to bypass
hardware, operating-system, or Bluetooth-specification limits.

<p align="center"><i>Kotlin · Jetpack Compose · Material 3 · MVVM · Hilt · Room · Coroutines/Flow · Navigation Compose</i></p>

---

## Features

### Home
- Animated radar/Bluetooth hero logo, large title, and a pulsing **Start** button.
- Live status cards: Bluetooth state, Advertise/Scan/Nearby-Devices/Location/Notification
  permissions, BLE support, adapter name, and battery level.

### Broadcast (main control)
- Large animated **Start/Stop** button backed by a foreground service.
- Glowing live counters (packets sent, sessions) and a live throughput graph.
- Live stats: elapsed time, current mode, power level, advertising interval, estimated battery
  usage, and duration.

### Modes
Ten advertising payload shapes, all within the 31-byte legacy limit:
- Generic Advertisement, Random Manufacturer Data, Custom Local Name, Random UUID,
  Apple-/Samsung-style Nearby (interoperability testing), Google Fast Pair / Microsoft Swift Pair
  style structures (testing), Custom Payload Builder, and Random Payload Generator.
- **Visual payload editor** with a live **hex preview** and a running byte-count against Android's
  limit. Configure local name, UUID, manufacturer ID/data, service data, TX power, advertising
  interval, duration, and connectability.

### Advanced
- Aggregate statistics, saved configurations, favorites, and the full advertising history.
- JSON **export** of logs and configurations, and **import** of configurations.
- **BLE capability report**, device & battery information, and an entry to developer settings.

### Settings & About
- Theme (System / Light / Dark / **AMOLED**), accent color, Material You dynamic color, animation
  speed, battery saver, notifications, keep-screen-on, and developer options.
- Animated About screen with version, developer, license, privacy statement, and open-source
  library credits.

### Design & motion
Glassmorphic frosted cards, an animated mesh-gradient dynamic background, spring/physics-based
press feedback, smooth page transitions, glowing counters, skeleton loaders, a floating glass
bottom navigation bar, and an AMOLED-friendly dark mode. All motion respects an
**animation-speed** preference (including Off for accessibility/battery).

---

## Architecture

Clean **MVVM** with a unidirectional data flow:

```
ui/  (Compose screens + ViewModels)         →  observes StateFlow, sends events
 ├─ SessionViewModel        shared advertising config + live stats
 ├─ AdvancedViewModel       history / configs / export-import
 └─ SettingsViewModel       user preferences

ble/ (domain + platform)
 ├─ PayloadBuilder          BleConfig → AdvertiseData (+ hex preview, validation)
 ├─ BleAdvertiser           thin wrapper over BluetoothLeAdvertiser
 ├─ AdvertisingController   owns the logical session + live AdvertisingStats flow
 ├─ AdvertisingService      foreground service (ongoing notification)
 ├─ AdvertisingManager      UI-facing facade (start/stop)
 └─ BleStateMonitor         adapter/capability/battery snapshots

data/
 ├─ model/                  BleConfig, AdvertisingMode, stats, environment
 ├─ local/                  Room database, entities, DAOs
 ├─ preferences/            DataStore-backed user preferences
 └─ repository/             ConfigRepository, HistoryRepository

di/  (Hilt modules)
```

Dependencies are provided by **Hilt**; persistence uses **Room** (configs + history) and
**DataStore** (preferences); all asynchronous work runs on **Coroutines/Flow**.

---

## Building

Requirements: JDK 17, Android SDK Platform 34, Build-Tools 34.0.0.

```bash
# Point the build at your SDK (or set ANDROID_SDK_ROOT / create local.properties)
echo "sdk.dir=/path/to/Android/sdk" > local.properties

./gradlew :app:assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

Minimum SDK **29 (Android 10)**, target/compile SDK **34**. Runtime permissions are handled for
both the Android 12+ Bluetooth model (`BLUETOOTH_ADVERTISE/SCAN/CONNECT`, `neverForLocation`) and
the Android 10–11 model (`ACCESS_FINE_LOCATION`), plus `POST_NOTIFICATIONS` on Android 13+.

### Supported devices
Android 10+ phones and tablets, including Samsung, Google Pixel, Xiaomi, OnePlus, Nothing Phone,
and Motorola. BLE peripheral (advertising) support is required and is surfaced in the app's
capability report when unavailable.

---

## Responsible use

BLE Spam is a developer/testing tool. The "*-style" modes reproduce only the *publicly documented
structure* of well-known advertisement formats — filled with placeholder bytes — so you can verify
your own scanners and receivers parse them correctly. The app does not impersonate real devices,
flood, or exceed platform limits. Please use it only on hardware and networks you own or are
authorized to test.

## License

Apache License 2.0.
