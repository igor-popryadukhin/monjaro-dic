package com.monjaro.dic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monjaro.dic.data.VehicleRepository
import com.monjaro.dic.data.model.VehicleReadings
import com.monjaro.dic.navigation.NavigationEvent
import com.monjaro.dic.navigation.NavigationRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ClusterViewModel(
    private val vehicleRepository: VehicleRepository,
    private val navigationRepository: NavigationRepository
) : ViewModel() {

    init {
        vehicleRepository.start()
    }

    val readings: StateFlow<VehicleReadings> = vehicleRepository
        .readings
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Eagerly, vehicleRepository.readings.value)

    val navigation: StateFlow<NavigationEvent> = navigationRepository.navigation
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Eagerly, navigationRepository.navigation.value)

    val uiState: StateFlow<ClusterUiState> = combine(
        readings,
        navigation,
    ) { readings, navigation ->
        ClusterUiState(
            readings = readings,
            navigation = navigation,
            warnings = buildWarnings(readings)
        )
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.Eagerly,
        initialValue = ClusterUiState(
            readings = readings.value,
            navigation = navigation.value,
            warnings = buildWarnings(readings.value)
        )
    )

    fun updateNavigation(event: NavigationEvent) {
        viewModelScope.launch {
            navigationRepository.onNavigationUpdate(event)
        }
    }

    private fun buildWarnings(readings: VehicleReadings): List<ClusterWarning> {
        val warnings = mutableListOf<ClusterWarning>()
        if (readings.fuelLevelPercent < 15f) {
            warnings += ClusterWarning.LowFuel
        }
        if (readings.engineTemperatureC > 105f) {
            warnings += ClusterWarning.EngineHot
        }
        if (readings.absFault) {
            warnings += ClusterWarning.AbsFault
        }
        if (readings.airbagsFault) {
            warnings += ClusterWarning.AirbagFault
        }
        if (readings.laneDepartureWarning) {
            warnings += ClusterWarning.LaneDeparture
        }
        return warnings
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            vehicleRepository.stop()
        }
    }
}

data class ClusterUiState(
    val readings: VehicleReadings,
    val navigation: NavigationEvent,
    val warnings: List<ClusterWarning>
)

sealed interface ClusterWarning {
    data object LowFuel : ClusterWarning
    data object EngineHot : ClusterWarning
    data object AbsFault : ClusterWarning
    data object AirbagFault : ClusterWarning
    data object LaneDeparture : ClusterWarning
}
