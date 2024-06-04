package com.dawinder.btnjc.ui.composables

import android.annotation.SuppressLint
import android.location.Location
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.dawinder.btnjc.ui.data.UserData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.Task

/**
 * Composable function that represents the main screen of the application.
 *
 * @param navController The navigation controller used for handling navigation between screens.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(
    navController: NavHostController,
    onSignInClick: () -> Unit,
    userData: UserData,
    onSignOutClick: () -> Unit,
    fusedLocationClient: FusedLocationProviderClient,
) {
    Scaffold(bottomBar = {
        BottomAppBar { BottomNavigationBar(navController = navController) }
    }) {
        NavigationScreens(
            navController = navController,
            onSignInClick = onSignInClick,
            onSignOutClick = onSignOutClick,
            userData = userData,
            fusedLocationClient = fusedLocationClient,
        )
    }
}