package com.dawinder.btnjc.ui.composables.tabs

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dawinder.btnjc.ui.data.UserData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch


/**
 * Composable function that represents the home screen of the application.
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun HomeScreen(
    userData: UserData,
    fusedLocationClient: FusedLocationProviderClient,
) {
    // Write a message to the database
    val database = Firebase.database
    val postsRef = database.getReference("posts")

    // State to hold the user's current location
    val userLocationState = remember { mutableStateOf<LatLng?>(null) }
    val showDialog = remember { mutableStateOf(false) }

    // State to hold the list of posts with their positions
    val postsWithPositions = remember { mutableStateOf<Map<LatLng, Post>>(emptyMap()) }

    // State to manage the bottom sheet
    val bottomSheetState = rememberModalBottomSheetState()
    val selectedPost = remember { mutableStateOf<Post?>(null) }

    // Scope for launching coroutines
    val coroutineScope = rememberCoroutineScope()

    // Function to get the current location and update the state
    val updateLocation: () -> Unit = {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val latLng = LatLng(it.latitude, it.longitude)
                userLocationState.value = latLng
                Log.d("Location", "Lat: ${it.latitude}, Long: ${it.longitude}")
            }
        }
    }
    // Fetch posts from Firebase and update posts with positions
    postsRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val positions = snapshot.children.mapNotNull { postSnapshot ->
                val post = postSnapshot.getValue(Post::class.java)
                post?.latitude?.let { lat ->
                    post.longitude?.let { lng ->
                        LatLng(lat, lng) to post
                    }
                }
            }.toMap()
            postsWithPositions.value = positions
        }

        override fun onCancelled(error: DatabaseError) {
            Log.w("Firebase", "loadPost:onCancelled", error.toException())
        }
    })



    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 75.dp)
    ) {
        MyMap(
            modifier = Modifier.fillMaxSize(),
            postsWithPositions = postsWithPositions.value,
            onMarkerClick = { post ->
                selectedPost.value = post
                coroutineScope.launch {
                    bottomSheetState.show()

                }
            }
        )
        FloatingActionButton(
            onClick = {
                // Update location before showing the dialog
                updateLocation()
                showDialog.value = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
        }

        if (showDialog.value) {
            userData.username?.let {
                PostCreationDialog(
                    userName = it,
                    userLocation = userLocationState.value,
                    onDismiss = { showDialog.value = false },
                    onPost = { title, description, userName, lat, long ->
                        // Send the post data to Firebase
                        val newPostRef = postsRef.push()
                        newPostRef.setValue(Post(title, description, userName, lat, long))
                        showDialog.value = false
                    }
                )
            }
        }
        selectedPost.value?.let { post ->
            LaunchedEffect(bottomSheetState, selectedPost.value) {
                coroutineScope.launch {
                    if (bottomSheetState.isVisible) {
                        bottomSheetState.hide()
                    } else {
                        bottomSheetState.show()
                    }
                }
            }
            ModalBottomSheet(
                sheetState = bottomSheetState,
                onDismissRequest = {
                    coroutineScope.launch {
                        bottomSheetState.hide()
                        selectedPost.value = null
                    }
                }
            ) {
                PostDetails(post = post)
            }
        }

    }
}

@Composable
fun MyMap(
    modifier: Modifier,
    postsWithPositions: Map<LatLng, Post>,
    onMarkerClick: (Post) -> Unit
) {
    val kutahya = LatLng(39.47, 29.90)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(kutahya, 10f)
    }
    val uiSettings by remember { mutableStateOf(MapUiSettings()) }
    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        uiSettings = uiSettings
            .copy(zoomControlsEnabled = false),
        properties = MapProperties(isMyLocationEnabled = true)
    ) {
        // Add markers from the posts with positions
        postsWithPositions.forEach { (latLng, post) ->
            Marker(
                state = MarkerState(position = latLng),
                title = post.title,
                onClick = {
                    onMarkerClick(post)
                    true // Return true to indicate that the click has been handled
                }
            )
        }
    }
}

@Composable
fun PostCreationDialog(
    userName: String,
    userLocation: LatLng?,
    onDismiss: () -> Unit,
    onPost: (String, String, String, Double?, Double?) -> Unit
) {
    val title = remember { mutableStateOf("") }
    val description = remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Create Post") },
        text = {
            Column {
                OutlinedTextField(
                    value = title.value,
                    onValueChange = { title.value = it },
                    label = { Text("Title") }
                )
                OutlinedTextField(
                    value = description.value,
                    onValueChange = { description.value = it },
                    label = { Text("Description") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onPost(
                        title.value,
                        description.value,
                        userName,
                        userLocation?.latitude,
                        userLocation?.longitude
                    )
                }
            ) {
                Text("Send")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

data class Post(
    val title: String = "",
    val description: String = "",
    val userName: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Composable
fun PostDetails(post: Post) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
        ,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = post.title, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = post.description, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Posted by: ${post.userName}", style = MaterialTheme.typography.bodySmall)
    }
}