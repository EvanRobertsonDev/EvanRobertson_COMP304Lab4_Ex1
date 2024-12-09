package com.example.evanrobertson_comp304lab4_ex1.viewModels

import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.evanrobertson_comp304lab4_ex1.data.LocationPoint
import com.example.evanrobertson_comp304lab4_ex1.data.LocationRepository
import com.example.evanrobertson_comp304lab4_ex1.data.Route
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch

class LocationViewModel(private val locationRepository: LocationRepository) : ViewModel() {

    //Updates current location in repo
    fun updateLocation(location: LatLng) {
        viewModelScope.launch {
            locationRepository.updateLocation(location.latitude, location.longitude)
        }
    }

    //Retrieves current location from repo
    fun getLocation() : LatLng? {
        return locationRepository.userLocation.value
    }
}