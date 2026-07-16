# BLE Spam — Bluetooth Low Energy Testing Suite

A premium, flagship-styled Android application for **BLE development, debugging,
interoperability testing and educational demonstrations**. Built from scratch
with a clean MVVM architecture, Jetpack Compose, Material 3 and a heavy emphasis
on a fluid, animated, glassmorphic user experience.

> **Responsible use.** BLE Spam advertises *only* through the standard Android
> `BluetoothLeAdvertiser` API and stays within the platform's own size, timing
> and rate limits — it does **not** attempt to bypass any hardware, OS or
> Bluetooth-specification restriction. Use it to test devices you own or are
> authorised to test. Broadcasting to disrupt, deceive or harass people or
> devices you do not control may be illegal.

---

## Features

- **Home** — live system status: Bluetooth state, permissions (location / scan /
  nearby devices), BLE support, adapter name and battery level, with a large
  animated Start action.
- **Control** — Start/Stop advertising, glowing live counters, a live throughput
  graph, and session statistics (packets, sessions, elapsed time, mode, power,
  interval, estimated battery usage).
- **Modes** — ten advertising payload builders, from generic advertisements and
  random manufacturer data to structural *interoperability-testing* formats for
  Apple/Samsung/Google Fast Pair/Microsoft Swift Pair packet shapes, plus a
  fully custom payload builder.
- **Visual Payload Editor** — compose an advertisement field by field with a
  live, color-coded hex preview and a running 31-byte legacy budget check.
- **Advanced** — advertising history, saved configurations, favorites, JSON
  export/import, aggregate statistics, and a full device / BLE capability report
  including battery-optimization status.
- **Settings** — theme (system/light/dark), accent color, Material You dynamic
  color, AMOLED black, animation speed & toggle, battery saver, notifications,
  developer options.
- **About** — animated hero, version/developer/license/privacy, responsible-use
  statement and open-source library credits.

## Tech stack

| Concern            | Choice                                   |
|--------------------|------------------------------------------|
| Language           | Kotlin                                   |
| UI                 | Jetpack Compose + Material 3             |
| Architecture       | MVVM (unidirectional data flow)          |
| Async              | Coroutines + Flow                        |
| DI                 | Hilt                                     |
| Persistence        | Room + DataStore Preferences             |
| Navigation         | Navigation Compose                       |
| Min / Target SDK   | 29 (Android 10) / 35                      |

## Architecture

```
com.blespam.app
├── data/            Room entities, DAOs, mappers, repositories, DataStore
├── domain/          BLE layer (advertiser, payload builder, environment),
│                    device info, export, and pure Kotlin models
├── di/              Hilt modules
└── ui/              Compose theme, reusable components, per-screen ViewModels
                     and screens (home, control, modes, advanced, settings, about)
```

- `BleEnvironment` is the single, version-aware source of truth for Bluetooth
  capabilities and runtime-permission state (handles the Android 10/11 vs 12+
  permission split).
- `BleAdvertiser` owns the radio lifecycle and exposes live `AdvertisingStats`
  via a `StateFlow`; `AdvertisingController` shares session + config state across
  screens and records history.
- `PayloadBuilder` converts a configuration into a platform `AdvertiseData`,
  validates it against the legacy size budget, and produces the editor's hex
  preview.

## Building

The project uses the Gradle wrapper. With the Android SDK installed
(`ANDROID_HOME` / `local.properties` pointing at it):

```bash
./gradlew assembleDebug      # build a debug APK
./gradlew installDebug       # build & install on a connected device
./gradlew assembleRelease    # R8-shrunk release build
```

Open the folder in Android Studio (Ladybug or newer) to build and run with a
single click.

## Supported devices

Android 10+ phones and tablets, including Samsung, Google Pixel, Xiaomi,
OnePlus, Nothing Phone and Motorola. BLE advertising availability ultimately
depends on the device's Bluetooth chipset; the app detects and reports this at
runtime and degrades gracefully when advertising isn't supported.

## License

Apache License 2.0 — see [LICENSE](LICENSE).
