package com.dawinder.btnjc.ui.activities

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.dawinder.btnjc.R
import com.dawinder.btnjc.ui.composables.MainScreen
import com.dawinder.btnjc.ui.data.UserData
import com.dawinder.btnjc.ui.theme.BottomTabNavigationJetpackComposeTheme
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        setContent {
            BottomTabNavigationJetpackComposeTheme {
                val navController = rememberNavController()
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen(
                        navController = navController,
                        { signIn() },
                        UserData(
                            userId = Firebase.auth.currentUser?.uid ?: "Unknown",
                            username = Firebase.auth.currentUser?.displayName,
                            profilePictureUrl = Firebase.auth.currentUser?.photoUrl.toString()
                        )
                    )
                }
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
                                        Log.d("TAGxxx", "signInWithCredential:success")
                                        Log.d("TAGxxx", "${user?.displayName}")
                                    } else {
                                        // If sign in fails, display a message to the user
                                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                                        //updateUI(null)

                                        Log.d("TAGxxx", "signInWithCredential:failure")
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
        val currentUser = auth.currentUser
        Log.d("TAGxxx", "Current User: $currentUser")
    }

    private fun getCurrentUserName(firebaseAuth: FirebaseAuth): String {
        return firebaseAuth.currentUser?.displayName.toString()
    }


}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    val navController = rememberNavController()
    BottomTabNavigationJetpackComposeTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            //MainScreen(navController = navController)
        }
    }
}