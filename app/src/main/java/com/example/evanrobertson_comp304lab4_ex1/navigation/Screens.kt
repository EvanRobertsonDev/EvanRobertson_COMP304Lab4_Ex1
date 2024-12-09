package com.example.evanrobertson_comp304lab4_ex1.navigation

sealed class Screens(val route: String) {
    data object MapScreen : Screens("map")
    data object AddRouteScreen : Screens("addRoute")
}