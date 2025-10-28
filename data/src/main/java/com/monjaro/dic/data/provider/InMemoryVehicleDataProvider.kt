package com.monjaro.dic.data.provider

import com.monjaro.dic.data.model.VehicleReadings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

class InMemoryVehicleDataProvider : VehicleDataProvider {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val _readings = MutableSharedFlow<VehicleReadings>(replay = 1)
    override val readings: SharedFlow<VehicleReadings> = _readings

    private var running = false

    override suspend fun connect() {
        if (running) return
        running = true
        scope.launch {
            var time = 0f
            while (running) {
                val speed = 50 + 30 * sin(time)
                val rpm = 2000 + 800 * cos(time / 2)
                val fuel = (70 - time / 10).coerceAtLeast(5f)
                val engineTemp = 85 + 5 * sin(time / 3)
                val cruiseActive = time % 60f > 10f && time % 60f < 40f
                val laneWarning = time % 45f > 30f

                _readings.emit(
                    VehicleReadings(
                        speedKph = speed,
                        rpm = rpm,
                        fuelLevelPercent = fuel,
                        engineTemperatureC = engineTemp,
                        gear = when {
                            speed < 2 -> "P"
                            speed < 10 -> "1"
                            speed < 25 -> "2"
                            speed < 40 -> "3"
                            speed < 60 -> "4"
                            else -> "5"
                        },
                        odometerKm = time / 10f,
                        tripDistanceKm = time / 15f,
                        outsideTemperatureC = 18 + 4 * sin(time / 10),
                        batteryVoltage = 12.4f,
                        cruiseControlActive = cruiseActive,
                        laneDepartureWarning = laneWarning,
                        headlightsOn = true,
                        highBeamsOn = time % 90f < 10f,
                        leftIndicatorOn = time % 6f < 1.5f,
                        rightIndicatorOn = time % 6f in 3f..4.5f,
                        absFault = false,
                        airbagsFault = false
                    )
                )
                time += 0.5f
                delay(100L)
            }
        }
    }

    override suspend fun disconnect() {
        running = false
    }
}
