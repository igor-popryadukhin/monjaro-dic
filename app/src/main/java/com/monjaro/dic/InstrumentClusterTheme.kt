package com.monjaro.dic

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val DarkColors = darkColorScheme(
    primary = ClusterPalette.primary,
    secondary = ClusterPalette.secondary,
    background = ClusterPalette.backgroundDark,
    onBackground = ClusterPalette.onBackground
)

private val LightColors = lightColorScheme(
    primary = ClusterPalette.primary,
    secondary = ClusterPalette.secondary,
    background = ClusterPalette.backgroundLight,
    onBackground = ClusterPalette.onBackground
)

@Composable
fun InstrumentClusterTheme(
    mode: ClusterMode,
    content: @Composable () -> Unit
) {
    val systemUiController = rememberSystemUiController()
    val darkTheme = when (mode) {
        ClusterMode.CLASSIC -> isSystemInDarkTheme()
        ClusterMode.MINIMAL -> true
        ClusterMode.NAVIGATION -> false
    }

    SideEffect {
        systemUiController.setSystemBarsColor(
            color = if (darkTheme) ClusterPalette.backgroundDark else ClusterPalette.backgroundLight,
            darkIcons = !darkTheme
        )
    }

    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = ClusterTypography,
        content = content
    )
}
