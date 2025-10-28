package com.monjaro.dic.data

import com.monjaro.dic.data.model.VehicleReadings
import com.monjaro.dic.data.provider.VehicleDataProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class VehicleRepository(
    private val provider: VehicleDataProvider,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val _readings = MutableStateFlow(
        VehicleReadings(
            speedKph = 0f,
            rpm = 0f,
            fuelLevelPercent = 100f,
            engineTemperatureC = 90f,
            gear = "P",
            odometerKm = 0f,
            tripDistanceKm = 0f,
            outsideTemperatureC = 20f,
            batteryVoltage = 12.6f,
            cruiseControlActive = false,
            laneDepartureWarning = false,
            headlightsOn = false,
            highBeamsOn = false,
            leftIndicatorOn = false,
            rightIndicatorOn = false,
            absFault = false,
            airbagsFault = false
        )
    )

    private var subscriptionJob: Job? = null

    val readings: StateFlow<VehicleReadings> = _readings.asStateFlow()

    fun start() {
        if (subscriptionJob != null) return
        subscriptionJob = externalScope.launch {
            provider.connect()
            provider.readings.collectLatest { sample ->
                _readings.value = sample
            }
        }
    }

    suspend fun stop() {
        subscriptionJob?.cancel()
        subscriptionJob = null
        provider.disconnect()
    }
}
