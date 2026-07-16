package com.blespam.app.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.blespam.app.ui.about.AboutScreen
import com.blespam.app.ui.advanced.AdvancedScreen
import com.blespam.app.ui.components.AnimatedGradientBackground
import com.blespam.app.ui.control.ControlScreen
import com.blespam.app.ui.home.HomeScreen
import com.blespam.app.ui.modes.ModesScreen
import com.blespam.app.ui.modes.PayloadEditorScreen
import com.blespam.app.ui.navigation.GlassNavBar
import com.blespam.app.ui.navigation.Routes
import com.blespam.app.ui.navigation.TopLevelDestination
import com.blespam.app.ui.settings.SettingsScreen

/**
 * The root composable: an animated-gradient background, a floating glass bottom
 * bar, and a [NavHost] with physics-y slide+fade transitions between screens.
 *
 * @param animationsEnabled global reduced-motion switch (Settings / Battery Saver).
 * @param animationSpeed     multiplier applied to background motion.
 */
@Composable
fun BleSpamApp(
    animationsEnabled: Boolean = true,
    animationSpeed: Float = 1f,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val topLevel = TopLevelDestination.entries
    val isTopLevel = topLevel.any { it.route == currentRoute }

    AnimatedGradientBackground(animated = animationsEnabled, speed = animationSpeed) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                if (isTopLevel) {
                    GlassNavBar(
                        destinations = topLevel,
                        currentRoute = currentRoute,
                        onSelect = { dest -> navController.navigateTopLevel(dest.route) },
                    )
                }
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                AppNavHost(navController, animationsEnabled, animationSpeed)
            }
        }
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    animationsEnabled: Boolean,
    animationSpeed: Float,
) {
    // Physics-based spring for premium, fluid page transitions.
    val spring = spring<Float>(dampingRatio = 0.85f, stiffness = 320f)
    val slideSpring = spring<androidx.compose.ui.unit.IntOffset>(dampingRatio = 0.9f, stiffness = 320f)

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        enterTransition = {
            slideInHorizontally(slideSpring) { it / 6 } + fadeIn(spring)
        },
        exitTransition = {
            fadeOut(spring)
        },
        popEnterTransition = {
            slideInHorizontally(slideSpring) { -it / 6 } + fadeIn(spring)
        },
        popExitTransition = {
            slideOutHorizontally(slideSpring) { it / 6 } + fadeOut(spring)
        },
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onStart = { navController.navigateTopLevel(Routes.CONTROL) },
                onOpenAbout = { navController.navigate(Routes.ABOUT) },
            )
        }
        composable(Routes.CONTROL) {
            ControlScreen(onOpenModes = { navController.navigateTopLevel(Routes.MODES) })
        }
        composable(Routes.MODES) {
            ModesScreen(onOpenEditor = { navController.navigate(Routes.PAYLOAD_EDITOR) })
        }
        composable(Routes.PAYLOAD_EDITOR) {
            PayloadEditorScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.ADVANCED) {
            AdvancedScreen()
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onOpenAbout = { navController.navigate(Routes.ABOUT) })
        }
        composable(Routes.ABOUT) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}

/** Navigate between top-level tabs without stacking duplicates. */
private fun NavHostController.navigateTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
