package com.example.evanrobertson_comp304lab4_ex1.navigation

import android.location.Location
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.evanrobertson_comp304lab4_ex1.viewModels.RoutesViewModel
import com.example.evanrobertson_comp304lab4_ex1.views.AddRouteScreen
import com.example.evanrobertson_comp304lab4_ex1.views.MainView
import com.google.android.gms.maps.model.LatLng
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.androidx.compose.koinViewModel
import java.net.URLDecoder
import java.net.URLEncoder

@Composable
fun AppNavigation(
    userLocation: LatLng,
    navHostController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navHostController,
        startDestination = Screens.MapScreen.route
    ) {
        //Main screen
        composable(Screens.MapScreen.route) {
            MainView(
                modifier = Modifier,
                userLocation,
                onAddRouteClicked = {
                    navHostController.navigate(Screens.AddRouteScreen.route)
                }
            )
        }
        //Add route screen
        composable(Screens.AddRouteScreen.route) {
            AddRouteScreen(
                modifier = Modifier,
                userLocation,
                onRouteSaved = {
                    navHostController.navigate(Screens.MapScreen.route)
                }
            )
        }
    }
}