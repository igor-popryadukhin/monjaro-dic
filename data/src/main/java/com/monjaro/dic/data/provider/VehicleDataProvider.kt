package com.monjaro.dic.data.provider

import com.monjaro.dic.data.model.VehicleReadings
import kotlinx.coroutines.flow.Flow

interface VehicleDataProvider {
    val readings: Flow<VehicleReadings>
    suspend fun connect()
    suspend fun disconnect()
}
