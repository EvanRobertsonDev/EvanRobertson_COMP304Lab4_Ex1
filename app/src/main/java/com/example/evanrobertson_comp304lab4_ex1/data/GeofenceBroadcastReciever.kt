package com.example.evanrobertson_comp304lab4_ex1.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (GeofencingEvent.fromIntent(intent)!!.hasError()) {
            return
        }
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent!!.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            val requestId = geofencingEvent.triggeringGeofences?.firstOrNull()?.requestId
            requestId?.let {
                //Print to log
                println(it)
            }
        }
    }
}