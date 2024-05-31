package com.dawinder.btnjc.ui.composables.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.dawinder.btnjc.ui.data.UserData

/**
 * Composable function that represents the profile screen of the application.
 */
@Composable
fun ProfileScreen(userData: UserData) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = userData.username ?: "Unknown",
            textAlign = TextAlign.Center,
            fontSize = 36.sp
        )
    }
}