package com.example.evanrobertson_comp304lab4_ex1.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.toList

class RoutesRepository {
    private val routes = MutableStateFlow<List<Route>>(emptyList())

    //Returns list of tasks as StateFlow
    fun getRoutes(): StateFlow<List<Route>> = routes.asStateFlow()

    //Adds route
    fun addRoute(route : Route) {
        routes.value += route
    }

    //Removes route
    fun removeRoute(route : Route) {
        routes.value -= route
    }
}