package com.example.evanrobertson_comp304lab4_ex1.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.evanrobertson_comp304lab4_ex1.data.LocationPoint
import com.example.evanrobertson_comp304lab4_ex1.viewModels.RoutesViewModel
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.flow.count
import org.koin.androidx.compose.koinViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import com.google.android.gms.maps.CameraUpdateFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRouteScreen(
    modifier: Modifier = Modifier,
    userLocation: LatLng,
    onRouteSaved: () -> Unit,
    viewModel: RoutesViewModel = koinViewModel()
) {
    //Waypoints list
    val waypoints = remember { mutableStateListOf<LocationPoint>() }

    //Route name
    val routeName = remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Route") },
                navigationIcon = {
                    IconButton(onClick = { onRouteSaved() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            //Google Map for selecting waypoints
            GoogleMapView(
                waypoints = waypoints,
                onMapClick = { location -> waypoints += location },
                userLocation,
                modifier = Modifier
                    .weight(1f)
            )

            //Input field for route name
            OutlinedTextField(
                value = routeName.value,
                onValueChange = { routeName.value = it },
                label = { Text("Route Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .weight(0.3f)
                    .padding(8.dp)
            ) {
                items(waypoints) { waypoint ->
                    //Show lat and lon for each waypoint
                    SuggestionChip(label = {Text("Lat: ${waypoint.latitude}, Lng: ${waypoint.longitude}")}, onClick = {})
                }
            }

            // Save Button
            Button(
                onClick = {
                    //Add route to repo using viewmodel
                    viewModel.addRoute(
                        id = viewModel.routes.value.size.toString(),
                        name = routeName.value,
                        startPoint = waypoints.first(),
                        endPoint = waypoints.last(),
                        waypoints = waypoints
                    )
                    onRouteSaved()
                },
                enabled = routeName.value.isNotBlank() && waypoints.size >= 2,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
            ) {
                Text("Save Route")
            }
        }
    }
}

@Composable
fun GoogleMapView(
    waypoints: List<LocationPoint>,
    onMapClick: (LocationPoint) -> Unit,
    userLocation: LatLng,
    modifier: Modifier
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    AndroidView(
        factory = { mapView.apply { onCreate(null); onResume() } },
        modifier = modifier
            .fillMaxWidth(),
        update = { map ->
            map.getMapAsync { googleMap ->
                googleMap.uiSettings.isZoomControlsEnabled = true

                //Add existing waypoints
                googleMap.clear()
                waypoints.forEach { waypoint ->
                    val latLng = LatLng(waypoint.latitude, waypoint.longitude)
                    googleMap.addMarker(MarkerOptions().position(latLng))
                }

                //Handle map click
                googleMap.setOnMapClickListener { latLng ->
                    val location = LocationPoint(latLng.latitude, latLng.longitude)
                    onMapClick(location)
                }

                //Move camera to user location
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
            }
        }
    )
}