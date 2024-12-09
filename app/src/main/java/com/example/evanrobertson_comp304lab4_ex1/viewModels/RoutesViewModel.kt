package com.example.evanrobertson_comp304lab4_ex1.viewModels

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.evanrobertson_comp304lab4_ex1.data.GeofenceBroadcastReceiver
import com.example.evanrobertson_comp304lab4_ex1.data.LocationPoint
import com.example.evanrobertson_comp304lab4_ex1.data.Route
import com.example.evanrobertson_comp304lab4_ex1.data.RoutesRepository
import com.example.evanrobertson_comp304lab4_ex1.workers.GeofenceWorker
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RoutesViewModel (private val routesRepository: RoutesRepository): ViewModel() {

    // Gets all routes from repo
    val routes = routesRepository.getRoutes()

    // Adds route to repo
    fun addRoute(id: String, name: String, startPoint: LocationPoint, endPoint: LocationPoint, waypoints: List<LocationPoint>) {
        viewModelScope.launch {
            routesRepository.addRoute(Route(id, name, startPoint, endPoint, waypoints))
        }
    }

    // Removes route from repo
    fun removeRoute(id: String, name: String, startPoint: LocationPoint, endPoint: LocationPoint, waypoints: List<LocationPoint>) {
        viewModelScope.launch {
            routesRepository.removeRoute(Route(id, name, startPoint, endPoint, waypoints))
        }
    }

    // Creates a geofence given a waypoint
    private fun createGeofenceForWaypoint(waypoint: LocationPoint): Geofence {
        return Geofence.Builder()
            .setRequestId("${waypoint.latitude}, ${waypoint.longitude}") // Unique ID for each geofence
            .setCircularRegion(waypoint.latitude, waypoint.longitude, 500f) // 100 meters radius
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()
    }

    //Creates Geofences for each location provided
    fun addGeofences(context: Context, waypoints: List<LocationPoint>) {
        val geofencingClient = LocationServices.getGeofencingClient(context)

        //Create geofences for each waypoint
        val geofences = waypoints.map { createGeofenceForWaypoint(it) }

        //Create geofence request
        val geofenceRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(geofences)
            .build()

        val geofencePendingIntent: PendingIntent = Intent(context, GeofenceBroadcastReceiver::class.java)
            .let { intent ->
                PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            }

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Add geofences using worker
            scheduleGeofencingNotifications(waypoints, context)
        }
    }

    //Clears all geofences on client
    fun clearGeofences(context: Context) {
        val geofencingClient = LocationServices.getGeofencingClient(context)

        val geofencePendingIntent: PendingIntent = Intent(context, GeofenceBroadcastReceiver::class.java)
            .let { intent ->
                PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            }

        //Remove all geofences
        geofencingClient.removeGeofences(geofencePendingIntent)
    }

    //Assign geofences using worker
    private fun scheduleGeofencingNotifications(waypoints: List<LocationPoint>, context: Context) {
        val geofenceData = waypoints.map { waypoint ->
            workDataOf(
                "geofenceId" to "${waypoint.latitude}, ${waypoint.longitude}",
                "latitude" to waypoint.latitude,
                "longitude" to waypoint.longitude,
                "radius" to 500f
            )
        }

        // Enqueue the WorkManager request for each waypoint
        geofenceData.forEach { data ->
            val geofenceWorkRequest = OneTimeWorkRequestBuilder<GeofenceWorker>()
                .setInputData(data)
                .build()

            WorkManager.getInstance(context).enqueue(geofenceWorkRequest)
        }
    }
}