package com.monjaro.dic.data.model

data class VehicleReadings(
    val speedKph: Float,
    val rpm: Float,
    val fuelLevelPercent: Float,
    val engineTemperatureC: Float,
    val gear: String,
    val odometerKm: Float,
    val tripDistanceKm: Float,
    val outsideTemperatureC: Float,
    val batteryVoltage: Float,
    val cruiseControlActive: Boolean,
    val laneDepartureWarning: Boolean,
    val headlightsOn: Boolean,
    val highBeamsOn: Boolean,
    val leftIndicatorOn: Boolean,
    val rightIndicatorOn: Boolean,
    val absFault: Boolean,
    val airbagsFault: Boolean
)
