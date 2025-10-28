package com.monjaro.dic

import android.app.Application
import com.monjaro.dic.data.VehicleRepository
import com.monjaro.dic.data.provider.InMemoryVehicleDataProvider
import com.monjaro.dic.navigation.NavigationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class InstrumentClusterApplication : Application() {
    private val applicationScope = CoroutineScope(Dispatchers.Default)

    val vehicleRepository: VehicleRepository by lazy {
        VehicleRepository(InMemoryVehicleDataProvider(), applicationScope)
    }

    val navigationRepository: NavigationRepository by lazy {
        NavigationRepository()
    }
}
