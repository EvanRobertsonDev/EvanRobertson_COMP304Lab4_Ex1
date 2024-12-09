package com.example.evanrobertson_comp304lab4_ex1.navigation

import android.location.Location
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.evanrobertson_comp304lab4_ex1.data.LocationRepository
import com.example.evanrobertson_comp304lab4_ex1.viewModels.LocationViewModel
import com.google.android.gms.maps.model.LatLng
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavigationContent(
    onHomeClicked: () -> Unit,
    userLocation: LatLng,
    navHostController: NavHostController,
) {
    val locationViewModel : LocationViewModel = koinViewModel()

    //Update user location in repo
    locationViewModel.updateLocation(userLocation)

    Row(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        Scaffold(
            content = { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    AppNavigation(
                        //Get location from repo
                        locationViewModel.getLocation()!!,
                        navHostController = navHostController
                    )
                }
            }
        )
    }
}