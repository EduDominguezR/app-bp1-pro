package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PulseBlue
import com.example.ui.theme.PulseCyan
import kotlin.math.ln

@Composable
fun EqualizerCurveCanvas(
    points: List<Pair<Float, Float>>,
    modifier: Modifier = Modifier,
    minDb: Float = -16f,
    maxDb: Float = 16f
) {
    val themeMode = com.example.ui.theme.LocalThemeMode.current
    val isGlass = themeMode == com.example.data.AppThemeMode.LIQUID_GLASS

    val outlineColor = if (isGlass) Color.White.copy(alpha = 0.28f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    val gridColor = if (isGlass) Color.White.copy(alpha = 0.10f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isGlass) Color.White.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .then(
                if (isGlass) {
                    Modifier.border(
                        BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
                        RoundedCornerShape(16.dp)
                    )
                } else Modifier
            )
            .testTag("equalizer_curve_canvas")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            val width = size.width
            val height = size.height
            if (width <= 0f || height <= 0f || points.isEmpty()) return@Canvas

            val minFreq = 20.0
            val maxFreq = 20000.0
            val logMin = ln(minFreq)
            val logMax = ln(maxFreq)

            fun freqToX(freq: Double): Float {
                val fClamped = freq.coerceIn(minFreq, maxFreq)
                val fraction = (ln(fClamped) - logMin) / (logMax - logMin)
                return (fraction * width).toFloat()
            }

            fun dbToY(db: Float): Float {
                val fraction = (db - minDb) / (maxDb - minDb)
                // Invert because Y goes downward in Canvas
                return height - (fraction * height)
            }

            // Draw dB horizontal grid lines (-12, -6, 0, +6, +12)
            val dbSteps = listOf(-12f, -6f, 0f, 6f, 12f)
            for (db in dbSteps) {
                val y = dbToY(db)
                val lineStroke = if (db == 0f) 1.5f else 0.8f
                val strokeColor = if (db == 0f) outlineColor else gridColor
                drawLine(
                    color = strokeColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = lineStroke
                )
            }

            // Draw vertical frequency guidelines (100Hz, 1kHz, 10kHz)
            val freqGuidelines = listOf(100.0, 1000.0, 10000.0)
            for (fg in freqGuidelines) {
                val x = freqToX(fg)
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 0.8f
                )
            }

            // Build smooth curve path
            val curvePath = Path()
            val fillPath = Path()

            val zeroY = dbToY(0f)

            var firstPoint = true
            for (point in points) {
                val x = freqToX(point.first.toDouble())
                val y = dbToY(point.second)
                if (firstPoint) {
                    curvePath.moveTo(x, y)
                    fillPath.moveTo(x, zeroY)
                    fillPath.lineTo(x, y)
                    firstPoint = false
                } else {
                    curvePath.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
            }

            // Complete fill path down to zeroY
            if (points.isNotEmpty()) {
                val lastX = freqToX(points.last().first.toDouble())
                fillPath.lineTo(lastX, zeroY)
                fillPath.close()
            }

            // Draw gradient fill below curve
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PulseCyan.copy(alpha = 0.35f),
                        PulseBlue.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = height
                )
            )

            // Draw glowing curve stroke
            drawPath(
                path = curvePath,
                brush = Brush.horizontalGradient(
                    colors = listOf(PulseBlue, PulseCyan)
                ),
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )
        }

        // Frequency labels overlay
        Text(
            text = "100 Hz",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 68.dp, bottom = 4.dp)
        )
        Text(
            text = "1 kHz",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 4.dp)
        )
        Text(
            text = "10 kHz",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 56.dp, bottom = 4.dp)
        )
        Text(
            text = "0 dB",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 6.dp)
        )
    }
}
