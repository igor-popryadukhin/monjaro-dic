package com.monjaro.dic.service

import android.car.cluster.renderer.InstrumentClusterRenderingService
import android.graphics.Bitmap
import android.view.Surface
import com.monjaro.dic.data.VehicleRepository
import com.monjaro.dic.data.provider.VehicleDataProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Thin service wrapper that bridges Android Automotive's InstrumentClusterRenderingService
 * with the UI process. In this sample we render the UI inside the application process and
 * simply keep the provider connected while the cluster surface is alive.
 */
class ClusterRendererEntryPoint : InstrumentClusterRenderingService() {
    private var job: Job? = null

    // These dependencies would typically be provided via DI container.
    private val scope = CoroutineScope(Dispatchers.Default)
    private val repository by lazy {
        val provider = Class.forName("com.monjaro.dic.data.provider.InMemoryVehicleDataProvider")
            .getDeclaredConstructor()
            .newInstance() as VehicleDataProvider
        VehicleRepository(provider, scope)
    }

    override fun onSurfaceAvailable(surface: Surface, width: Int, height: Int) {
        super.onSurfaceAvailable(surface, width, height)
        repository.start()
        job = scope.launch {
            repository.readings.collectLatest {
                // In a production build this surface would be forwarded to a renderer.
                // This sample focuses on architecture so rendering is handled by the Activity.
            }
        }
    }

    override fun onSurfaceDestroyed(surface: Surface) {
        super.onSurfaceDestroyed(surface)
        job?.cancel()
        job = null
    }

    override fun getBitmap(width: Int, height: Int): Bitmap? = null
}
