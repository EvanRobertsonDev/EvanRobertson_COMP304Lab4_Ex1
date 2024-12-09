package com.example.evanrobertson_comp304lab4_ex1.views

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.evanrobertson_comp304lab4_ex1.data.LocationPoint
import com.example.evanrobertson_comp304lab4_ex1.data.Route
import com.example.evanrobertson_comp304lab4_ex1.viewModels.RoutesViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainView(
    modifier: Modifier,
    userLocation: LatLng,
    onAddRouteClicked: () -> Unit
) {
    //Viewmodel
    val routesViewModel : RoutesViewModel = koinViewModel()

    //All routes
    val routes = routesViewModel.routes.collectAsState()

    //Menu boolean
    var isMenuExpanded by remember { mutableStateOf(false) }

    //Current route
    var selectedRoute by remember { mutableStateOf<Route?>(null) }

    Scaffold(
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            //Google maps integration
            MapViewWithLocation(
                waypoints = selectedRoute?.waypoints ?: emptyList(),
                onMarkerClick = { clickedWaypoint ->
                    println("Marker clicked: $clickedWaypoint")
                },
                userLocation,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            //Expandable dropdown for routes
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { isMenuExpanded = !isMenuExpanded }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        //Update if open or closed
                        text = if (isMenuExpanded) "Hide Routes" else "Show Routes",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    //Dropdown Icon
                    Icon(
                        //Update if open or closed
                        imageVector = if (isMenuExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }


            //Show routes only if the menu is expanded
            AnimatedVisibility(visible = isMenuExpanded) {
                RouteList(
                    onRouteClicked = {
                        selectedRoute = it // Update selected route
                        isMenuExpanded = false // Collapse the menu
                    },
                    routes = routes.value,
                    viewModel = routesViewModel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            }
            //Add route button
            Button(
                onClick = {
                    onAddRouteClicked()
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                //Add icon
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Route"
                )
            }
        }
    }
}

@Composable
fun MapViewWithLocation(
    waypoints: List<LocationPoint>,
    onMarkerClick: (LocationPoint) -> Unit,
    userLocation: LatLng,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val routesViewModel : RoutesViewModel = koinViewModel()
    val mapView = remember { MapView(context) }


    AndroidView(
        factory = { mapView.apply { onCreate(null); onResume() } },
        modifier = modifier,
        update = { map ->
            map.getMapAsync { googleMap ->
                // Enable map controls
                googleMap.uiSettings.apply {
                    isZoomControlsEnabled = true
                    isCompassEnabled = true
                    isMyLocationButtonEnabled = true
                }

                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    googleMap.isMyLocationEnabled = true
                }

                // Clear previous markers and polylines
                googleMap.clear()

                //User Location Marker
                googleMap.addMarker(
                    MarkerOptions()
                        .position(userLocation)
                        .title("You are here")
                )

                //Clear previous geofence locations
                routesViewModel.clearGeofences(context)

                //Create geofence location for each waypoint
                if (waypoints.isNotEmpty())  routesViewModel.addGeofences(context, waypoints)

                //Add markers for waypoints
                waypoints.forEach { waypoint ->
                    val latLng = LatLng(waypoint.latitude, waypoint.longitude)
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title("Lat: ${waypoint.latitude}, Lon: ${waypoint.longitude}")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    )
                }

                //Add polylines to connect waypoints
                if (waypoints.size > 1) {
                    val polylineOptions = PolylineOptions().apply {
                        waypoints.forEach { waypoint ->
                            add(LatLng(waypoint.latitude, waypoint.longitude))
                        }
                        color(Color.Blue.toArgb())
                        width(5f)
                    }
                    googleMap.addPolyline(polylineOptions)
                }

                //Adjust camera to include all waypoints or focus on user location
                if (waypoints.isNotEmpty()) {
                    val bounds = LatLngBounds.Builder().apply {
                        waypoints.forEach { waypoint ->
                            include(LatLng(waypoint.latitude, waypoint.longitude))
                        }
                    }.build()
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                } else {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                }

                //Display lat and lon when marker is clicked
                googleMap.setOnMarkerClickListener { marker ->
                    val locationPoint = LocationPoint(marker.position.latitude, marker.position.longitude)
                    onMarkerClick(locationPoint)
                    marker.showInfoWindow()
                    true
                }
            }
        }
    )
}