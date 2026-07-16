package com.blespam.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point. [HiltAndroidApp] triggers Hilt's code generation and creates the
 * application-level dependency graph. Kept intentionally light so cold-start stays fast.
 */
@HiltAndroidApp
class BleSpamApplication : Application()
