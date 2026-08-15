# Video Downloader

A gorgeous, fully‑animated Android app that downloads videos from **any
platform** — TikTok, YouTube, Instagram, X (Twitter), Facebook, Reddit and
1,800+ other sites — at the **highest available quality**.

Built with 100% Jetpack Compose and powered on‑device by
[`yt-dlp`](https://github.com/yt-dlp/yt-dlp) (via
[youtubedl‑android](https://github.com/JunkFood02/youtubedl-android)) with a
bundled `ffmpeg` for merging and `aria2c` for accelerated downloads.

## Features

- **Any platform** — paste a link from virtually any video site and it just works.
- **Top quality** — grabs the best video stream and merges it with the best
  audio (4K/1080p when the source offers it). Presets for Best / 1080p / 720p /
  audio‑only MP3.
- **Insane animations** — a living aurora background, a shimmering neon title,
  a rotating gradient progress ring with an animated percentage, a
  confetti‑and‑checkmark success burst, a springy glass bottom bar, and
  animated screen transitions throughout.
- **Share‑to‑download** — share a link from another app straight into it.
- **Library** — every download is listed with one‑tap open and share.
- **No storage permission** — files are saved to the app's scoped storage and
  opened/shared via a `FileProvider`.

## Get the APK

You don't need Android Studio. Every push builds a debug APK in CI:

1. Open the repo's **Actions** tab → the latest **Build APK** run → download the
   `video-downloader-debug` artifact, **or**
2. Grab it from the **Releases** page (the rolling `latest` release).

Then on your Android phone: enable *Install unknown apps* for your browser/file
manager and open the `.apk`.

## Build it yourself

Requires the Android SDK (API 35) and JDK 17.

```bash
./gradlew assembleDebug
# output: app/build/outputs/apk/debug/app-debug.apk
```

## Tech

| Area        | Choice                                            |
|-------------|---------------------------------------------------|
| Language    | Kotlin                                            |
| UI          | Jetpack Compose + Material 3 (custom neon theme)   |
| Download    | youtubedl‑android (yt‑dlp) + ffmpeg + aria2c       |
| Min / Target| Android 7.0 (API 24) / Android 15 (API 35)         |

## Responsible use

Only download content you own or have permission to use. Respect each
platform's Terms of Service and applicable copyright law.
