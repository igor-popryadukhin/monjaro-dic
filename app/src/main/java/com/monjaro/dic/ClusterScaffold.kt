package com.monjaro.dic

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.monjaro.dic.navigation.NavigationEvent

@Composable
fun ClusterScaffold(
    uiState: ClusterUiState,
    onModeSelected: (ClusterMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        StatusHeader(uiState)
        when (uiState.mode) {
            ClusterMode.CLASSIC -> ClassicCluster(uiState)
            ClusterMode.MINIMAL -> MinimalCluster(uiState)
            ClusterMode.NAVIGATION -> NavigationCluster(uiState)
        }
        ModeSelector(current = uiState.mode, onModeSelected = onModeSelected)
    }
}

@Composable
private fun StatusHeader(uiState: ClusterUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = "Gear ${uiState.readings.gear}", style = MaterialTheme.typography.headlineMedium)
            Text(
                text = stringResource(id = R.string.odometer) + ": %.1f km".format(uiState.readings.odometerKm),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        WarningPanel(uiState.warnings)
    }
}

@Composable
private fun WarningPanel(warnings: List<ClusterWarning>) {
    AnimatedVisibility(visible = warnings.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
        Card(colors = CardDefaults.cardColors(containerColor = ClusterPalette.warning.copy(alpha = 0.15f))) {
            Column(modifier = Modifier.padding(16.dp)) {
                warnings.forEach { warning ->
                    Text(text = warning.label, color = ClusterPalette.warning)
                }
            }
        }
    }
}

private val ClusterWarning.label: String
    get() = when (this) {
        ClusterWarning.LowFuel -> "Low fuel"
        ClusterWarning.EngineHot -> "Engine overheating"
        ClusterWarning.AbsFault -> "ABS fault"
        ClusterWarning.AirbagFault -> "Airbag fault"
        ClusterWarning.LaneDeparture -> "Lane departure"
    }

@Composable
private fun ClassicCluster(state: ClusterUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Gauge(
            value = state.readings.rpm / 8000f,
            label = "RPM",
            formattedValue = "%.0f".format(state.readings.rpm),
            highlightColor = ClusterPalette.secondary
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "%.0f".format(state.readings.speedKph),
                style = MaterialTheme.typography.displayLarge
            )
            Text(text = "km/h", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(16.dp))
            NavigationTile(state.navigation)
        }
        Gauge(
            value = state.readings.fuelLevelPercent / 100f,
            label = stringResource(id = R.string.fuel_level),
            formattedValue = "%.0f%%".format(state.readings.fuelLevelPercent),
            highlightColor = ClusterPalette.primary
        )
    }
}

@Composable
private fun MinimalCluster(state: ClusterUiState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "%.0f".format(state.readings.speedKph),
            style = MaterialTheme.typography.displayLarge
        )
        LinearGauge(
            value = state.readings.speedKph / 220f,
            label = "Speed"
        )
        LinearGauge(
            value = state.readings.fuelLevelPercent / 100f,
            label = stringResource(id = R.string.fuel_level),
            color = ClusterPalette.primary
        )
        LinearGauge(
            value = state.readings.engineTemperatureC / 120f,
            label = stringResource(id = R.string.engine_temp),
            color = ClusterPalette.secondary
        )
    }
}

@Composable
private fun NavigationCluster(state: ClusterUiState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "%.0f km/h".format(state.readings.speedKph),
            style = MaterialTheme.typography.displayLarge
        )
        NavigationTile(state.navigation)
        LinearGauge(
            value = state.readings.fuelLevelPercent / 100f,
            label = stringResource(id = R.string.fuel_level)
        )
    }
}

@Composable
private fun NavigationTile(event: NavigationEvent) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (event.isActive) {
                Text(text = event.instruction, style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = "${event.distanceMeters / 1000f} km · ETA ${event.etaMinutes} min",
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                Text(text = stringResource(id = R.string.no_navigation))
            }
        }
    }
}

@Composable
private fun Gauge(
    value: Float,
    label: String,
    formattedValue: String,
    highlightColor: Color
) {
    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(220.dp)) {
            val strokeWidth = 24.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
            drawArc(
                color = Color.DarkGray,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                brush = Brush.linearGradient(listOf(highlightColor, highlightColor.copy(alpha = 0.4f))),
                startAngle = 135f,
                sweepAngle = (270f * value.coerceIn(0f, 1f)),
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = formattedValue, style = MaterialTheme.typography.headlineMedium)
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun LinearGauge(
    value: Float,
    label: String,
    modifier: Modifier = Modifier,
    color: Color = ClusterPalette.secondary
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(CircleShape)
                .background(Color.DarkGray.copy(alpha = 0.5f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(value.coerceIn(0f, 1f))
                    .height(16.dp)
                    .background(color)
            )
        }
    }
}

@Composable
private fun ModeSelector(current: ClusterMode, onModeSelected: (ClusterMode) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ClusterMode.values().forEach { mode ->
            val isSelected = current == mode
            Button(
                onClick = { onModeSelected(mode) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) ClusterPalette.primary else Color.DarkGray
                )
            ) {
                Text(text = mode.name.lowercase().replaceFirstChar { it.titlecase() })
            }
        }
    }
}
