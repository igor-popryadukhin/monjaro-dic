package com.monjaro.dic.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NavigationRepository {
    private val _navigation = MutableStateFlow(
        NavigationEvent(
            instruction = "",
            distanceMeters = 0,
            etaMinutes = 0,
            isActive = false
        )
    )

    val navigation: StateFlow<NavigationEvent> = _navigation.asStateFlow()

    fun onNavigationUpdate(event: NavigationEvent) {
        _navigation.update { event }
    }

    fun clear() {
        _navigation.update {
            it.copy(
                instruction = "",
                distanceMeters = 0,
                etaMinutes = 0,
                isActive = false
            )
        }
    }
}
