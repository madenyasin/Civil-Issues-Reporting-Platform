package com.dawinder.btnjc.ui.composables

import android.location.Location
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dawinder.btnjc.nav.NavItem
import com.dawinder.btnjc.ui.composables.tabs.HomeScreen
import com.dawinder.btnjc.ui.composables.tabs.ListScreen
import com.dawinder.btnjc.ui.composables.tabs.ProfileScreen
import com.dawinder.btnjc.ui.composables.tabs.SearchScreen
import com.dawinder.btnjc.ui.composables.tabs.SignInScreen
import com.dawinder.btnjc.ui.data.UserData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.Task

/**
 * Composable function that defines the navigation screens and their corresponding destinations.
 *
 * @param navController The navigation controller used for handling navigation between screens.
 */
@Composable
fun NavigationScreens(
    navController: NavHostController,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit,
    userData: UserData,
    fusedLocationClient: FusedLocationProviderClient,
) {
    NavHost(navController, startDestination = NavItem.Home.path) {
        composable(NavItem.Home.path) { HomeScreen(fusedLocationClient) }
        composable(NavItem.Search.path) { SearchScreen() }
        composable(NavItem.List.path) { ListScreen() }
        composable(NavItem.Profile.path) { ProfileScreen(userData, onSignOutClick) }
        composable(NavItem.SignIn.path) { SignInScreen(onSignInClick) }
    }
}

