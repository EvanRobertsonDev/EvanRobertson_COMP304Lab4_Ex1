package com.example.evanrobertson_comp304lab4_ex1.navigation

sealed interface ContentType {
    data object List : ContentType
    data object ListAndDetail : ContentType
}