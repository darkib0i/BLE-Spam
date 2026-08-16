plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// Each CI build gets a higher versionCode (from the workflow run number) so
// Android treats the new APK as an update; local builds default to 1.
val buildVersionCode = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 1

android {
    namespace = "com.videodownloader.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.videodownloader.app"
        // yt-dlp / python bundle from youtubedl-android needs API 24+.
        minSdk = 24
        targetSdk = 35
        versionCode = buildVersionCode
        versionName = "1.0.$buildVersionCode"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }

        // Ship only the ARM ABIs real phones use; dropping x86_64 (emulators)
        // roughly halves the APK, which matters given the bundled yt-dlp/ffmpeg.
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
        }
    }

    // A committed, stable debug key so every build (local or CI) is signed
    // identically — that's what lets a new APK install *over* the old one
    // instead of Android rejecting it as a conflicting package.
    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        release {
            // R8 would strip the reflection youtubedl-android relies on, so
            // shrinking stays off for this build to keep downloads working.
            isMinifyEnabled = false
        }
        debug {
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.animation.ExperimentalAnimationApi",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        // youtubedl-android ships its python/ffmpeg payload as .so files that
        // must be extracted at install time rather than mmap'd from the APK.
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {
    // AndroidX core + lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.splashscreen)

    // Compose (BOM keeps every artifact on one aligned version)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    debugImplementation(libs.androidx.ui.tooling)

    // Async
    implementation(libs.kotlinx.coroutines.android)

    // yt-dlp download engine (library + ffmpeg for merging + aria2c for speed)
    implementation(libs.youtubedl.library)
    implementation(libs.youtubedl.ffmpeg)
    implementation(libs.youtubedl.aria2c)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
}
