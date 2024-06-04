package com.dawinder.btnjc.ui.composables.tabs

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Firebase
import com.google.firebase.database.database
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState


/**
 * Composable function that represents the home screen of the application.
 */
@SuppressLint("MissingPermission")
@Composable
fun HomeScreen(
    fusedLocationClient: FusedLocationProviderClient,
) {

    // Write a message to the database
    val database = Firebase.database
    val latitudeRef = database.getReference("latitude")
    val longitudeRef = database.getReference("longitude")

    // State to hold the user's current location
    val userLocationState = remember { mutableStateOf<LatLng?>(null) }

    // Function to get the current location and update the state
    val updateLocation: () -> Unit = {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val latLng = LatLng(it.latitude, it.longitude)
                userLocationState.value = latLng

                //send to firebase location
                latitudeRef.setValue(it.latitude)
                longitudeRef.setValue(it.longitude)

                Log.d("Location", "Lat: ${it.latitude}, Long: ${it.longitude}")
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 75.dp)
    ) {
        MyMap(
            modifier = Modifier
                .fillMaxSize()
        )
        FloatingActionButton(
            onClick = {

                updateLocation()
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
        }

    }
}

@Composable
fun MyMap(modifier: Modifier) {
    val kutahya = LatLng(39.47, 29.90)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(kutahya, 10f)
    }
    val uiSettings by remember { mutableStateOf(MapUiSettings()) }
    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        uiSettings = uiSettings.copy(zoomControlsEnabled = false)
    ) {
        Marker(
            state = MarkerState(position = kutahya),
            title = "Singapore",
            snippet = "Marker in Singapore"
        )
    }
}
