package com.example.evanrobertson_comp304lab4_ex1.data

import kotlinx.serialization.Serializable

@Serializable
data class Route(
    val id: String,
    val name: String,
    val startPoint: LocationPoint,
    val endPoint: LocationPoint,
    val waypoints: List<LocationPoint>
)
