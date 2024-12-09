package com.example.evanrobertson_comp304lab4_ex1.data

import kotlinx.serialization.Serializable

@Serializable
data class LocationPoint(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null
)
