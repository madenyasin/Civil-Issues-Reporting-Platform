package com.dawinder.btnjc.ui.composables.tabs

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.dawinder.btnjc.ui.data.UserData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun MapScreen(
    userData: UserData,
    fusedLocationClient: FusedLocationProviderClient,
) {
    val database = Firebase.database
    val postsRef = database.getReference("posts")

    val userLocationState = remember { mutableStateOf<LatLng?>(null) }
    val showDialog = remember { mutableStateOf(false) }
    val postsWithPositions = remember { mutableStateOf<Map<LatLng, Post>>(emptyMap()) }
    val bottomSheetState = rememberModalBottomSheetState()
    val selectedPost = remember { mutableStateOf<Post?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val cameraPositionState = rememberCameraPositionState()

    val updateLocation: () -> Unit = {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val latLng = LatLng(it.latitude, it.longitude)
                userLocationState.value = latLng
                cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)
                Log.d("Location", "Lat: ${it.latitude}, Long: ${it.longitude}")
            }
        }
    }

    LaunchedEffect(Unit) {
        updateLocation()
    }

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
            },
            cameraPositionState = cameraPositionState
        )
        FloatingActionButton(
            onClick = {
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
                    onPost = { title, description, userName, lat, long, imageUri ->
                        val storageRef = Firebase.storage.reference
                        val imageRef = storageRef.child("images/${UUID.randomUUID()}")
                        imageUri?.let {
                            imageRef.putFile(it)
                                .addOnSuccessListener { taskSnapshot ->
                                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                                        val newPostRef = postsRef.push()
                                        newPostRef.setValue(Post(title, description, userName, lat, long, uri.toString()))
                                    }
                                }
                        } ?: run {
                            val newPostRef = postsRef.push()
                            newPostRef.setValue(Post(title, description, userName, lat, long, null))
                        }
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
    onMarkerClick: (Post) -> Unit,
    cameraPositionState: CameraPositionState
) {
    val uiSettings by remember { mutableStateOf(MapUiSettings()) }
    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        uiSettings = uiSettings
            .copy(zoomControlsEnabled = false),
        properties = MapProperties(isMyLocationEnabled = true)
    ) {
        postsWithPositions.forEach { (latLng, post) ->
            Marker(
                state = MarkerState(position = latLng),
                title = post.title,
                onClick = {
                    onMarkerClick(post)
                    true
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
    onPost: (String, String, String, Double?, Double?, Uri?) -> Unit
) {
    val title = remember { mutableStateOf("") }
    val description = remember { mutableStateOf("") }
    val imageUri = remember { mutableStateOf<Uri?>(null) }
    val imageName = remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri.value = uri
        imageName.value = uri?.lastPathSegment
    }

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
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") }
                ) {
                    Text("Pick Image")
                }
                imageName.value?.let {
                    Text(text = "Selected image: $it", style = MaterialTheme.typography.bodySmall)
                }
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
                        userLocation?.longitude,
                        imageUri.value
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
    val longitude: Double? = null,
    val imageUrl: String? = null
)

@Composable
fun PostDetails(post: Post) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = post.title, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = post.description, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Posted by: ${post.userName}", style = MaterialTheme.typography.bodySmall)
        post.imageUrl?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Image(
                painter = rememberImagePainter(data = it),
                contentDescription = "Post Image",
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )
        }
    }
}
