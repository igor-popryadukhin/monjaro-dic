package com.monjaro.dic

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ClusterScaffold(
    uiState: ClusterUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        NeonCluster(uiState)
    }
}

@Composable
private fun NeonCluster(state: ClusterUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NeonGauge(
            value = state.readings.rpm / 8000f,
            label = "x1000 r/min",
            formattedValue = "%.1f".format(state.readings.rpm / 1000f),
            steps = 9,
            colors = listOf(ClusterPalette.neon, ClusterPalette.warning)
        )
        NeonGauge(
            value = state.readings.speedKph / 240f,
            label = "km/h",
            formattedValue = "%.0f".format(state.readings.speedKph),
            steps = 13,
            colors = listOf(ClusterPalette.neon, ClusterPalette.secondary)
        )
    }
}

@Composable
private fun NeonGauge(
    value: Float,
    label: String,
    formattedValue: String,
    steps: Int,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    val animatedValue by animateFloatAsState(targetValue = value, label = "gaugeValue")

    Box(modifier = modifier.size(350.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 18.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val radius = diameter / 2f
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            val textPaintSize = 18.sp.toPx()

            drawGaugeArc(strokeWidth, topLeft, diameter, colors)
            drawTicksAndLabels(steps, radius, strokeWidth, textPaintSize)
            drawNeedle(animatedValue, radius, strokeWidth)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = formattedValue, style = MaterialTheme.typography.displayLarge)
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

private fun DrawScope.drawGaugeArc(strokeWidth: Float, topLeft: Offset, diameter: Float, colors: List<Color>) {
    drawArc(
        brush = Brush.sweepGradient(
            colors = colors,
            center = center
        ),
        startAngle = 135f,
        sweepAngle = 270f,
        useCenter = false,
        topLeft = topLeft,
        size = Size(diameter, diameter),
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawTicksAndLabels(steps: Int, radius: Float, strokeWidth: Float, textPaintSize: Float) {
    (0 until steps).forEach { i ->
        val angle = 135f + i * (270f / (steps - 1))
        val angleRad = Math.toRadians(angle.toDouble())
        val start = center + Offset(
            (radius - strokeWidth / 3) * cos(angleRad).toFloat(),
            (radius - strokeWidth / 3) * sin(angleRad).toFloat()
        )
        val end = center + Offset(
            (radius + strokeWidth / 3) * cos(angleRad).toFloat(),
            (radius + strokeWidth / 3) * sin(angleRad).toFloat()
        )
        drawLine(color = Color.White, start = start, end = end, strokeWidth = 2.dp.toPx())

        drawIntoCanvas { canvas ->
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = textPaintSize
                textAlign = android.graphics.Paint.Align.CENTER
                setShadowLayer(10f, 0f, 0f, ClusterPalette.neon.hashCode())
            }
            val text = (i * (if (steps == 9) 1 else 20)).toString()
            val textOffset = center + Offset(
                (radius - strokeWidth) * cos(angleRad).toFloat(),
                (radius - strokeWidth) * sin(angleRad).toFloat()
            )
            canvas.nativeCanvas.drawText(
                text,
                textOffset.x,
                textOffset.y + paint.fontMetrics.descent,
                paint
            )
        }
    }
}

private fun DrawScope.drawNeedle(value: Float, radius: Float, strokeWidth: Float) {
    val angle = 135f + (270f * value.coerceIn(0f, 1f))
    val angleRad = Math.toRadians(angle.toDouble())
    val start = center
    val end = center + Offset(
        (radius - strokeWidth) * cos(angleRad).toFloat(),
        (radius - strokeWidth) * sin(angleRad).toFloat()
    )
    drawLine(
        brush = Brush.linearGradient(listOf(Color.White, ClusterPalette.neon)),
        start = start, end = end, strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round
    )
    // Draw a shadow for the needle
    drawLine(
        color = ClusterPalette.neon.copy(alpha = 0.5f),
        start = start, end = end, strokeWidth = 12.dp.toPx(), cap = StrokeCap.Round
    )
}
