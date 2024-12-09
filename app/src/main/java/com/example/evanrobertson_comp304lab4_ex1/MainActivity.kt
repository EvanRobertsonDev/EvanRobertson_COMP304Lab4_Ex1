package com.example.evanrobertson_comp304lab4_ex1

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.evanrobertson_comp304lab4_ex1.data.LocationRepository
import com.example.evanrobertson_comp304lab4_ex1.di.appModules
import com.example.evanrobertson_comp304lab4_ex1.navigation.AppNavigationContent
import com.example.evanrobertson_comp304lab4_ex1.navigation.Screens
import com.example.evanrobertson_comp304lab4_ex1.views.MainView
import com.example.evanrobertson_comp304lab4_ex1.workers.BackgroundLocationWorker
import com.example.evanrobertson_comp304lab4_ex1.workers.GeofenceWorker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.maps.android.compose.*
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val defaultLocation = LatLng(43.6532, -79.3832) // Toronto as fallback

    //Location
    private var userLocationState = mutableStateOf(defaultLocation)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startKoin {
            androidContext(applicationContext)
            modules(appModules)
        }

        //Start WorkManagers
        scheduleBackgroundLocationUpdates()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //Ask for permissions
        val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                fetchCurrentLocation { location ->
                    // Update the location state from the result
                    userLocationState.value = location
                }
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        setContent {
            var locationPermissionGranted by remember { mutableStateOf(false) }
            val navController = rememberNavController()

            //Get Location
            LaunchedEffect(Unit) {
                // Check location permissions
                if (ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    locationPermissionGranted = true
                    fetchCurrentLocation { location ->
                        userLocationState.value = location
                    }
                } else {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }

            //UI
            AppNavigationContent(
                onHomeClicked = {
                    navController.navigate(Screens.MapScreen.route)
                },
                userLocationState.value,
                navHostController = navController
            )
        }
    }

    //Get current location
    private fun fetchCurrentLocation(onLocationFetched: (LatLng) -> Unit) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    onLocationFetched(latLng)
                } else {
                    Toast.makeText(this, "Unable to fetch location", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //Starts the location update worker
    private fun scheduleBackgroundLocationUpdates() {
        //Repeat every 5 minutes
        val workRequest = PeriodicWorkRequestBuilder<BackgroundLocationWorker>(5, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "LocationUpdateWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}