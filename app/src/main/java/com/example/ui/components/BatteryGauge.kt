package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PulseBlue
import com.example.ui.theme.PulseCyan
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed

@Composable
fun BatteryGauge(
    label: String,
    percent: Int,
    isCharging: Boolean,
    isCase: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isKnown = percent in 0..100
    val progress = if (isKnown) percent / 100f else 0f
    val color = when {
        !isKnown -> Color.Gray
        percent >= 50 -> StatusGreen
        percent >= 20 -> StatusAmber
        else -> StatusRed
    }

    val themeMode = com.example.ui.theme.LocalThemeMode.current
    val isGlass = themeMode == com.example.data.AppThemeMode.LIQUID_GLASS

    val surfaceColor = if (isGlass) {
        Color.White.copy(alpha = 0.08f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
    }

    val borderStroke = if (isGlass) {
        androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
    } else null

    Surface(
        modifier = modifier
            .testTag("battery_gauge_${label.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        color = surfaceColor,
        border = borderStroke,
        tonalElevation = if (isGlass) 0.dp else 2.dp
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(54.dp)
            ) {
                // Background track
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(54.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                    strokeWidth = 4.dp
                )
                // Active indicator
                if (isKnown) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(54.dp),
                        color = color,
                        strokeWidth = 4.dp
                    )
                }
                // Center icon / glyph
                if (isCharging) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Cargando",
                        tint = StatusAmber,
                        modifier = Modifier.size(24.dp)
                    )
                } else if (isCase) {
                    Icon(
                        imageVector = Icons.Default.Inbox,
                        contentDescription = "Estuche",
                        tint = PulseCyan,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Headphones,
                        contentDescription = "Auricular",
                        tint = PulseBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isKnown) "$percent%" else "--%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (isCharging) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(StatusAmber)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Cargando",
                        fontSize = 10.sp,
                        color = StatusAmber,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
