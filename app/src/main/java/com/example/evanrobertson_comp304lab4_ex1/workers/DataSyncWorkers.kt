package com.example.evanrobertson_comp304lab4_ex1.workers

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.evanrobertson_comp304lab4_ex1.data.GeofenceBroadcastReceiver
import com.example.evanrobertson_comp304lab4_ex1.data.LocationRepository
import com.example.evanrobertson_comp304lab4_ex1.data.RoutesRepository
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await

//Frequently checks user's current location and updates UI
class BackgroundLocationWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val locationRepository: LocationRepository
) : CoroutineWorker(appContext, workerParams) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext)

    override suspend fun doWork(): Result {
        return try {
            //Check for location permission
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val location = fusedLocationClient.lastLocation.await()
                location?.let {
                    val latitude = it.latitude
                    val longitude = it.longitude

                    //Update location repo
                    locationRepository.updateLocation(latitude, longitude)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}

//Creates geofences for each waypoint in a route
class GeofenceWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val geofencingClient = LocationServices.getGeofencingClient(appContext)

    override suspend fun doWork(): Result {

        //Get passed geofence data
        val geofenceId = inputData.getString("geofenceId") ?: return Result.failure()
        val latitude = inputData.getDouble("latitude", 0.0)
        val longitude = inputData.getDouble("longitude", 0.0)
        val radius = inputData.getFloat("radius", 100f)

        try {
            //Check for location permission
            if (ContextCompat.checkSelfPermission(applicationContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                //Create geofence
                val geofence = Geofence.Builder()
                    .setRequestId(geofenceId)
                    .setCircularRegion(latitude, longitude, radius)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build()

                val geofencingRequest = GeofencingRequest.Builder()
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    .addGeofence(geofence)
                    .build()

                val pendingIntent = PendingIntent.getBroadcast(
                    applicationContext,
                    0,
                    Intent(applicationContext, GeofenceBroadcastReceiver::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                //Add geofence to client
                geofencingClient.addGeofences(geofencingRequest, pendingIntent).await()
            }

            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }
}