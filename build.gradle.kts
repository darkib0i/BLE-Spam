// Top-level build file. Configuration common to all sub-projects/modules
// is declared here. Module-specific configuration lives in each module's
// own build.gradle.kts (see app/build.gradle.kts).
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
