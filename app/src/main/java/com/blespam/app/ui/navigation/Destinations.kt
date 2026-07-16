package com.blespam.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.ui.graphics.vector.ImageVector

/** Type-safe route keys for the whole app. */
object Routes {
    const val HOME = "home"
    const val BROADCAST = "broadcast"
    const val MODES = "modes"
    const val ADVANCED = "advanced"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val MODE_CONFIG = "mode_config"
    const val PERMISSIONS = "permissions"
}

/** The five primary tabs shown in the bottom navigation bar. */
enum class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    HOME(Routes.HOME, "Home", Icons.Rounded.Home),
    BROADCAST(Routes.BROADCAST, "Broadcast", Icons.Rounded.Dashboard),
    MODES(Routes.MODES, "Modes", Icons.Rounded.Widgets),
    ADVANCED(Routes.ADVANCED, "Advanced", Icons.Rounded.Tune),
    SETTINGS(Routes.SETTINGS, "Settings", Icons.Rounded.Settings),
}
