package com.blespam.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.ui.graphics.vector.ImageVector

/** Type-safe route keys for the whole app. */
object Routes {
    const val HOME = "home"
    const val CONTROL = "control"
    const val MODES = "modes"
    const val ADVANCED = "advanced"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val PAYLOAD_EDITOR = "payload_editor"
    const val PERMISSIONS = "permissions"
}

/** The five primary destinations shown in the bottom navigation bar. */
enum class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    HOME(Routes.HOME, "Home", Icons.Rounded.Home),
    CONTROL(Routes.CONTROL, "Control", Icons.Rounded.Sensors),
    MODES(Routes.MODES, "Modes", Icons.Rounded.GridView),
    ADVANCED(Routes.ADVANCED, "Advanced", Icons.Rounded.Tune),
    SETTINGS(Routes.SETTINGS, "Settings", Icons.Rounded.Settings),
}
