package com.monjaro.dic.navigation

data class NavigationEvent(
    val instruction: String,
    val distanceMeters: Int,
    val etaMinutes: Int,
    val isActive: Boolean
)
