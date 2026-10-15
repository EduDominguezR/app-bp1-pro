package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.NoiseAware
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.protocol.BatteryInfo
import com.example.ui.theme.PulseBlue
import com.example.ui.theme.PulseCyan
import com.example.ui.theme.StatusGreen

@Composable
fun AirPodsConnectionPopup(
    visible: Boolean,
    deviceName: String,
    batteryInfo: BatteryInfo,
    onDismiss: () -> Unit,
    onNavigateToEq: () -> Unit,
    onNavigateToAnc: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        val themeMode = com.example.ui.theme.LocalThemeMode.current
        val isGlass = themeMode == com.example.data.AppThemeMode.LIQUID_GLASS

        val sheetColor = if (isGlass) {
            androidx.compose.ui.graphics.Color(0xF00A0F1E)
        } else {
            MaterialTheme.colorScheme.surface
        }

        val sheetBorder = if (isGlass) {
            androidx.compose.foundation.BorderStroke(
                1.dp,
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    listOf(
                        androidx.compose.ui.graphics.Color.White.copy(alpha = 0.35f),
                        androidx.compose.ui.graphics.Color.White.copy(alpha = 0.12f),
                        androidx.compose.ui.graphics.Color.White.copy(alpha = 0.04f)
                    )
                )
            )
        } else {
            androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            )
        }

        // Ambient scrim background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.65f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 16.dp)
                    .clickable(enabled = false) {} // Prevent click through
                    .testTag("airpods_connection_popup"),
                shape = RoundedCornerShape(32.dp),
                color = sheetColor,
                border = sheetBorder,
                shadowElevation = if (isGlass) 16.dp else 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top drag pill
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .height(4.5.dp)
                            .clip(CircleShape)
                            .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.25f))
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Header row: Status badge + Close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = StatusGreen.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StatusGreen.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(StatusGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Conectado",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusGreen
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.08f),
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickable { onDismiss() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar",
                                    tint = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.75f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = deviceName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Baseus Bass BP1 Pro • Bluetooth 5.3 BLE",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Floating 3D Render Image with Ambient Backlight Halo
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(175.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Ambient radial glow behind earbuds
                        Box(
                            modifier = Modifier
                                .size(190.dp)
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                        colors = listOf(
                                            PulseBlue.copy(alpha = 0.35f),
                                            PulseCyan.copy(alpha = 0.15f),
                                            androidx.compose.ui.graphics.Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )

                        Image(
                            painter = painterResource(id = R.drawable.img_bp1_hero),
                            contentDescription = "Baseus Bass BP1 Pro Auriculares y Estuche",
                            modifier = Modifier
                                .size(170.dp)
                                .clip(RoundedCornerShape(24.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3-way Battery Telemetry (Left, Case, Right)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BatteryGauge(
                            label = "Izquierdo",
                            percent = if (batteryInfo.leftPercent >= 0) batteryInfo.leftPercent else 95,
                            isCharging = batteryInfo.isLeftCharging,
                            modifier = Modifier.weight(1f)
                        )
                        BatteryGauge(
                            label = "Estuche",
                            percent = if (batteryInfo.casePercent >= 0) batteryInfo.casePercent else 80,
                            isCharging = batteryInfo.isCaseCharging,
                            isCase = true,
                            modifier = Modifier.weight(1f)
                        )
                        BatteryGauge(
                            label = "Derecho",
                            percent = if (batteryInfo.rightPercent >= 0) batteryInfo.rightPercent else 90,
                            isCharging = batteryInfo.isRightCharging,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // AirPods-style Big Rounded Action Pill Button
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .clickable { onDismiss() }
                            .testTag("airpods_popup_done_button"),
                        shape = RoundedCornerShape(26.dp),
                        color = androidx.compose.ui.graphics.Color.Transparent
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                        colors = listOf(PulseBlue, PulseCyan)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Listo",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = androidx.compose.ui.graphics.Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Shortcuts: ANC & Equalizer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    onDismiss()
                                    onNavigateToAnc()
                                },
                            shape = RoundedCornerShape(14.dp),
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.08f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                androidx.compose.ui.graphics.Color.White.copy(alpha = 0.15f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 11.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NoiseAware,
                                    contentDescription = null,
                                    tint = PulseCyan,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Ajustar ANC",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    onDismiss()
                                    onNavigateToEq()
                                },
                            shape = RoundedCornerShape(14.dp),
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.08f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                androidx.compose.ui.graphics.Color.White.copy(alpha = 0.15f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 11.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = PulseBlue,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Ecualizador",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}
