package com.blespam.app.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.blespam.app.ui.SessionViewModel
import com.blespam.app.ui.about.AboutScreen
import com.blespam.app.ui.advanced.AdvancedScreen
import com.blespam.app.ui.advanced.AdvancedViewModel
import com.blespam.app.ui.broadcast.BroadcastScreen
import com.blespam.app.ui.home.HomeScreen
import com.blespam.app.ui.modes.ModeConfigScreen
import com.blespam.app.ui.modes.ModesScreen
import com.blespam.app.ui.settings.SettingsScreen
import com.blespam.app.ui.settings.SettingsViewModel

/**
 * Central navigation graph. Uses physics-flavored slide+fade transitions between destinations for
 * the "smooth page transition" feel. The [SessionViewModel] is hoisted by the caller and shared
 * across Home/Broadcast/Modes so advertising state is consistent everywhere.
 */
@Composable
fun BleSpamNavHost(
    navController: NavHostController,
    sessionViewModel: SessionViewModel,
    onRequestPermissions: () -> Unit,
    contentPadding: PaddingValues,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        enterTransition = { slideInHorizontally(tween(320)) { it / 6 } + fadeIn(tween(320)) },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { slideInHorizontally(tween(320)) { -it / 6 } + fadeIn(tween(320)) },
        popExitTransition = { slideOutHorizontally(tween(200)) { it / 6 } + fadeOut(tween(200)) },
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                viewModel = sessionViewModel,
                onStart = { navController.navigate(Routes.BROADCAST) },
                onRequestPermissions = onRequestPermissions,
                contentPadding = contentPadding,
            )
        }
        composable(Routes.BROADCAST) {
            BroadcastScreen(
                viewModel = sessionViewModel,
                onRequestPermissions = onRequestPermissions,
                contentPadding = contentPadding,
            )
        }
        composable(Routes.MODES) {
            ModesScreen(
                viewModel = sessionViewModel,
                onConfigure = { navController.navigate(Routes.MODE_CONFIG) },
                contentPadding = contentPadding,
            )
        }
        composable(Routes.MODE_CONFIG) {
            ModeConfigScreen(
                viewModel = sessionViewModel,
                onBack = { navController.popBackStack() },
                contentPadding = contentPadding,
            )
        }
        composable(Routes.ADVANCED) {
            val advancedViewModel: AdvancedViewModel = hiltViewModel()
            AdvancedScreen(
                viewModel = advancedViewModel,
                sessionViewModel = sessionViewModel,
                onOpenDeveloperSettings = { navController.navigate(Routes.SETTINGS) },
                contentPadding = contentPadding,
            )
        }
        composable(Routes.SETTINGS) {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                viewModel = settingsViewModel,
                onOpenAbout = { navController.navigate(Routes.ABOUT) },
                contentPadding = contentPadding,
            )
        }
        composable(Routes.ABOUT) {
            AboutScreen(
                onBack = { navController.popBackStack() },
                contentPadding = contentPadding,
            )
        }
    }
}
