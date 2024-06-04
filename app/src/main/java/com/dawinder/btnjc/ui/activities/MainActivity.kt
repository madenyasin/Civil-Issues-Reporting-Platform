package com.dawinder.btnjc.ui.activities

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.dawinder.btnjc.R
import com.dawinder.btnjc.ui.composables.MainScreen
import com.dawinder.btnjc.ui.data.UserData
import com.dawinder.btnjc.ui.theme.BottomTabNavigationJetpackComposeTheme
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

const val USER = "user-info"

class MainActivity : ComponentActivity() {
    /**
     * Called when the activity is starting. This is where most initialization should go:
     * calling `setContentView`, instantiating UI components, and binding data.
     *
     * @param savedInstanceState The previously saved instance state, if any.
     */
    private lateinit var auth: FirebaseAuth

    private val REQ_ONE_TAP = 2  // Unique request code for sign-in
    private var showOneTapUI = true
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val viewModel by viewModels<AuthViewModel> {
        AuthViewModelFactory(Firebase.auth)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d(TAG, "Location permission granted")
            } else {
                Log.d(TAG, "Location permission denied")
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Initialize One Tap client
        oneTapClient = Identity.getSignInClient(this)

        // Configure sign-in request
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.your_web_client_id)) // Replace with your server client ID
                    .setFilterByAuthorizedAccounts(true)
                    .build()
            )
            .build()
        // Check for location permission
        checkLocationPermission()

        setContent {
            BottomTabNavigationJetpackComposeTheme {
                val navController = rememberNavController()
                val userData by viewModel.userData.collectAsState()
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen(
                        navController = navController,
                        onSignInClick = { signIn() },
                        userData = userData,
                        onSignOutClick = { signOut() },
                        fusedLocationClient = fusedLocationClient
                    )
                }
            }
        }
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d(TAG, "Location permission already granted")
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                // Show an explanation to the user
                Log.d(TAG, "Showing location permission rationale")
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }

            else -> {
                // Directly ask for the permission
                Log.d(TAG, "Requesting location permission")
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun signIn() {
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(this) { result ->
                try {
                    startIntentSenderForResult(
                        result.pendingIntent.intentSender, REQ_ONE_TAP,
                        null, 0, 0, 0, null
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                }
            }
            .addOnFailureListener(this) { e ->
                Log.e(TAG, e.localizedMessage)
            }
    }

    private fun signOut() {
        Firebase.auth.signOut()
        Log.d(USER, auth.currentUser?.displayName.toString())
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQ_ONE_TAP -> {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(data)
                    val idToken = credential.googleIdToken
                    when {
                        idToken != null -> {
                            // Got an ID token from Google. Use it to authenticate with Firebase
                            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                            auth.signInWithCredential(firebaseCredential)
                                .addOnCompleteListener(this) { task ->
                                    if (task.isSuccessful) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "signInWithCredential:success")
                                        val user = auth.currentUser
                                        //updateUI(user)
                                        viewModel.updateUser(auth.currentUser)
                                        Log.d(USER, "signInWithCredential:success")
                                        Log.d(USER, "${user?.displayName}")
                                    } else {
                                        // If sign in fails, display a message to the user
                                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                                        //updateUI(null)
                                        viewModel.updateUser(null)
                                        Log.d(USER, "signInWithCredential:failure")
                                    }
                                }
                        }

                        else -> {
                            // Shouldn't happen
                            Log.d(TAG, "No ID token!")
                        }
                    }
                } catch (e: ApiException) {
                    // Handle error
                    Log.e(TAG, "Sign-in failed", e)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in and update UI accordingly
        viewModel.updateUser(auth.currentUser)
        Log.d(USER, "Current User: $auth.currentUser")
    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    //val navController = rememberNavController()
    BottomTabNavigationJetpackComposeTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            //MainScreen(navController = navController)
        }
    }
}

class AuthViewModel(private val auth: FirebaseAuth) : ViewModel() {
    private val _userData = MutableStateFlow(UserData("Unknown", "Unknown", null))
    val userData: StateFlow<UserData> = _userData

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            updateUser(user)
        }
    }

    fun updateUser(user: FirebaseUser?) {
        val userId = user?.uid ?: "Unknown"
        val username = user?.displayName ?: "Unknown"
        val profilePictureUrl = user?.photoUrl?.toString()

        _userData.value = UserData(userId, username, profilePictureUrl)
    }
}

class AuthViewModelFactory(private val auth: FirebaseAuth) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(auth) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}