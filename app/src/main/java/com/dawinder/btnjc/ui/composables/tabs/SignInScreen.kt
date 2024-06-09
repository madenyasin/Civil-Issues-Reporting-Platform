package com.dawinder.btnjc.ui.composables.tabs

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dawinder.btnjc.R
import com.example.compose.onPrimaryLight
import com.example.compose.primaryLight

@Composable
fun SignInScreen(onSignInClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Application Logo
            Image(
                painter = painterResource(id = R.drawable.app_logo), // Replace with your logo resource
                contentDescription = null,
                modifier = Modifier
                    .size(128.dp)
                    .padding(bottom = 16.dp)
            )

            // Application Name
            Text(
                text = stringResource(id = R.string.app_name), // Replace with your app name resource
                style = typography.titleLarge,
                color = primaryLight,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Sign in with Google Button
            Button(
                onClick = onSignInClick,
                //  colors = ButtonDefaults.buttonColors(backgroundColor = md_theme_light_primary)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.google_logo), // Replace with your Google logo resource
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.sign_in_with_google),
                        style = typography.titleLarge,
                        color = onPrimaryLight
                    )
                }
            }
        }
    }
}