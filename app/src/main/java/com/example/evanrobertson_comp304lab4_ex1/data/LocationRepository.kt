package com.example.evanrobertson_comp304lab4_ex1.data

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LocationRepository(private val context: Context) {
    private val _userLocation = MutableStateFlow<LatLng?>(null)

    //Current location
    val userLocation: StateFlow<LatLng?> = _userLocation

    //Updates locations
    fun updateLocation(latitude: Double, longitude: Double) {
        _userLocation.value = LatLng(latitude, longitude)
    }
}