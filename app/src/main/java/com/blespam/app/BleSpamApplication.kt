package com.blespam.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point. [HiltAndroidApp] triggers Hilt's code generation and
 * creates the app-wide dependency graph. Kept intentionally tiny so cold start
 * stays fast — no eager work happens here.
 */
@HiltAndroidApp
class BleSpamApplication : Application()
