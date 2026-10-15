package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PulseBlue
import com.example.ui.theme.PulseCyan

@Composable
fun FrequencyBandSlider(
    freqHz: Int,
    gainDb: Float,
    onGainChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    minDb: Float = -12f,
    maxDb: Float = 12f
) {
    val freqLabel = when {
        freqHz >= 1000 -> "${freqHz / 1000}k"
        else -> "$freqHz"
    }

    val gainLabel = when {
        gainDb > 0.1f -> "+%.1f".format(gainDb)
        gainDb < -0.1f -> "%.1f".format(gainDb)
        else -> "0.0"
    }

    var trackHeightPx by remember { mutableFloatStateOf(1f) }

    Column(
        modifier = modifier
            .width(36.dp)
            .testTag("freq_band_$freqLabel"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Gain display in dB
        Text(
            text = gainLabel,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (gainDb != 0f) PulseCyan else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Vertical Fader Track
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(130.dp)
                .onSizeChanged { trackHeightPx = it.height.toFloat() }
                .pointerInput(minDb, maxDb) {
                    detectVerticalDragGestures { change, dragAmount ->
                        change.consume()
                        if (trackHeightPx > 0f) {
                            val dbRange = maxDb - minDb
                            val deltaDb = -(dragAmount / trackHeightPx) * dbRange
                            val newGain = (gainDb + deltaDb).coerceIn(minDb, maxDb)
                            // Round to 0.5 dB steps for responsive feel
                            val stepped = (kotlin.math.round(newGain * 2) / 2.0).toFloat()
                            onGainChange(stepped)
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            val themeMode = com.example.ui.theme.LocalThemeMode.current
            val isGlass = themeMode == com.example.data.AppThemeMode.LIQUID_GLASS

            // Track background groove
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (isGlass) Color.White.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant)
            )

            // Zero dB center tick
            Box(
                modifier = Modifier
                    .width(14.dp)
                    .height(2.dp)
                    .background(if (isGlass) Color.White.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            )

            // Calculate thumb offset (fraction from bottom)
            val fraction = ((gainDb - minDb) / (maxDb - minDb)).coerceIn(0f, 1f)
            val thumbOffsetY = if (trackHeightPx > 0f) {
                // Inverted: fraction 1 (maxDb) is at Y=0, fraction 0 (minDb) is at bottom
                ((1f - fraction) * (trackHeightPx - 28f)).dp / androidx.compose.ui.platform.LocalDensity.current.density
            } else {
                50.dp
            }

            // Fader Handle / Thumb
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = thumbOffsetY.coerceAtLeast(0.dp))
                    .width(26.dp)
                    .height(20.dp)
                    .shadow(4.dp, RoundedCornerShape(6.dp))
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                PulseBlue,
                                PulseCyan
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Indicator line inside handle
                Box(
                    modifier = Modifier
                        .width(12.dp)
                        .height(2.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Frequency Label (31, 63, 125, 250, 500, 1k, 2k, 4k, 8k, 16k)
        Text(
            text = freqLabel,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
