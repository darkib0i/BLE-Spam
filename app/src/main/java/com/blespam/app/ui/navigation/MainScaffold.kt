package com.blespam.app.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.blespam.app.ui.components.AnimatedBackground
import com.blespam.app.ui.theme.LocalAppTheme
import com.blespam.app.ui.theme.onColorFor

/**
 * App chrome: the animated background behind everything plus a floating, glassy bottom navigation
 * bar. The bar hides on secondary destinations (mode config, about) so those feel like full pages.
 */
@Composable
fun MainScaffold(
    navController: NavHostController,
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in TopLevelDestination.entries.map { it.route }

    AnimatedBackground {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                if (showBottomBar) {
                    GlassBottomBar(
                        currentDestination = backStackEntry?.destination,
                        onNavigate = { destination ->
                            navController.navigate(destination.route) {
                                // Standard bottom-nav behavior: single top, restore state, pop to start.
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            },
            content = content,
        )
    }
}

@Composable
private fun GlassBottomBar(
    currentDestination: androidx.navigation.NavDestination?,
    onNavigate: (TopLevelDestination) -> Unit,
) {
    val theme = LocalAppTheme.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(30.dp))
                .background(
                    if (theme.isDark) Color.White.copy(alpha = if (theme.isAmoled) 0.06f else 0.08f)
                    else Color.White.copy(alpha = 0.7f),
                )
                .border(
                    1.dp,
                    if (theme.isDark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.8f),
                    RoundedCornerShape(30.dp),
                )
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TopLevelDestination.entries.forEach { destination ->
                val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
                NavItem(
                    destination = destination,
                    selected = selected,
                    onClick = { onNavigate(destination) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    destination: TopLevelDestination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val theme = LocalAppTheme.current
    val scale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.94f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "navScale",
    )
    val pillColor by animateColorAsState(
        targetValue = if (selected) theme.accent else Color.Transparent,
        label = "navPill",
    )
    val interaction = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .scale(scale)
                .clip(CircleShape)
                .background(pillColor.copy(alpha = if (selected) 0.18f else 0f))
                .padding(horizontal = 16.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = destination.icon,
                contentDescription = destination.label,
                tint = if (selected) theme.accent else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
        }
        Text(
            text = destination.label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) theme.accent else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
