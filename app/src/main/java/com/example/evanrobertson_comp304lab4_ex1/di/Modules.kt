package com.example.evanrobertson_comp304lab4_ex1.di

import com.example.evanrobertson_comp304lab4_ex1.data.LocationRepository
import com.example.evanrobertson_comp304lab4_ex1.data.RoutesRepository
import com.example.evanrobertson_comp304lab4_ex1.viewModels.LocationViewModel
import com.example.evanrobertson_comp304lab4_ex1.viewModels.RoutesViewModel
import com.example.evanrobertson_comp304lab4_ex1.workers.BackgroundLocationWorker
import com.example.evanrobertson_comp304lab4_ex1.workers.GeofenceWorker
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.workmanager.dsl.worker
import kotlinx.serialization.json.Json
import org.koin.dsl.module

private val json = Json
val appModules = module {
    single { RoutesRepository() }
    single { LocationRepository(get()) }
    single { RoutesViewModel(get()) }
    single { Dispatchers.IO }

    //workers
    worker { BackgroundLocationWorker(get(), get(), get()) }
    worker { GeofenceWorker(get(), get()) }

    //viewmodels
    viewModel { RoutesViewModel(routesRepository = get()) }
    viewModel { LocationViewModel(locationRepository = get()) }
}