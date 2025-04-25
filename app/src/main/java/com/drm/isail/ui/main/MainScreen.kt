package com.drm.isail.ui.screens.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import com.drm.isail.ui.navigation.AppNavigationActions
import com.drm.isail.ui.screens.home.HomeScreen
import com.drm.isail.ui.screens.matches.MatchesScreen
import com.drm.isail.ui.screens.profile.ProfileScreen

enum class MainTabs {
    HOME, MATCHES, PROFILE
}

data class BottomNavItem(
    val name: String,
    val route: MainTabs,
    val icon: ImageVector
)

@Composable
fun MainScreen(
    navigationActions: AppNavigationActions,
    viewModel: MainViewModel = hiltViewModel()
) {
    val mainState by viewModel.mainState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    
    var selectedTab by remember { mutableStateOf(MainTabs.HOME) }
    
    // Bottom navigation items
    val bottomNavItems = listOf(
        BottomNavItem("Home", MainTabs.HOME, Icons.Default.Home),
        BottomNavItem("Matches", MainTabs.MATCHES, Icons.Default.Person),
        BottomNavItem("Profile", MainTabs.PROFILE, Icons.Default.AccountCircle)
    )
    
    LaunchedEffect(mainState) {
        if (mainState is MainState.Unauthenticated) {
            navigationActions.navigateToLogin()
        }
    }
    
    Scaffold(
        bottomBar = {
            BottomNavigation {
                bottomNavItems.forEach { item ->
                    BottomNavigationItem(
                        icon = { Icon(item.icon, contentDescription = item.name) },
                        label = { Text(item.name) },
                        selected = selectedTab == item.route,
                        onClick = { selectedTab = item.route }
                    )
                }
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            MainTabs.HOME -> {
                HomeScreen(
                    onNavigateToShipForm = { navigationActions.navigateToShipForm() },
                    onNavigateToLandForm = { navigationActions.navigateToLandForm() },
                    onSignOut = { viewModel.signOut() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            MainTabs.MATCHES -> {
                MatchesScreen(
                    onNavigateToSearch = { navigationActions.navigateToSearch() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            MainTabs.PROFILE -> {
                ProfileScreen(
                    onSignOut = { viewModel.signOut() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
} 