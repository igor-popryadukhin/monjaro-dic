package com.monjaro.dic

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val DarkColors = darkColorScheme(
    primary = ClusterPalette.primary,
    secondary = ClusterPalette.secondary,
    background = ClusterPalette.backgroundDark,
    onBackground = ClusterPalette.onBackground
)

@Composable
fun InstrumentClusterTheme(
    content: @Composable () -> Unit
) {
    val systemUiController = rememberSystemUiController()

    SideEffect {
        systemUiController.setSystemBarsColor(
            color = ClusterPalette.backgroundDark,
            darkIcons = false
        )
    }

    MaterialTheme(
        colorScheme = DarkColors,
        typography = ClusterTypography,
        content = content
    )
}
