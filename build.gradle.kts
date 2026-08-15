// Top-level build file. Plugin versions come from the version catalog
// (gradle/libs.versions.toml). Module config lives in app/build.gradle.kts.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
