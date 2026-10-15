package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.NoiseAware
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SpatialAudio
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.audio.SoftwarePreset
import com.example.protocol.HardwareEqPreset
import com.example.R
import com.example.bluetooth.BleConnectionState
import com.example.protocol.AncMode
import com.example.ui.BaseusPulseViewModel
import com.example.ui.components.AncModeSelector
import com.example.ui.components.BatteryGauge
import com.example.ui.theme.PulseBlue
import com.example.ui.theme.PulseCyan
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed

@Composable
fun HomeScreen(
    viewModel: BaseusPulseViewModel,
    onNavigateToEq: () -> Unit,
    onNavigateToAnc: () -> Unit,
    onNavigateToConnection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val connState by viewModel.connectionState.collectAsState()
    val deviceName by viewModel.connectedDeviceName.collectAsState()
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    val ancMode by viewModel.currentAncMode.collectAsState()
    val ancStatus by viewModel.ancCommandStatus.collectAsState()
    val isGameMode by viewModel.isGameModeEnabled.collectAsState()
    val gameStatus by viewModel.gameModeStatus.collectAsState()
    val currentPreset by viewModel.softwarePreset.collectAsState()
    val currentHwEq by viewModel.currentHwEq.collectAsState()

    val isAuditionPlaying by viewModel.isAuditionPlaying.collectAsState()
    val isConnected = connState == BleConnectionState.CONECTADO

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("home_screen")
    ) {
        // App & Device Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Baseus Pulse",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = deviceName ?: "Baseus Bass BP1 Pro",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Connection indicator badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isConnected) StatusGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .testTag("connection_status_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isConnected) StatusGreen else StatusRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = connState.displayName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isConnected) StatusGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // AirPods-style Pop-up Quick Opener
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = PulseBlue.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, PulseCyan.copy(alpha = 0.40f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { viewModel.triggerInAppPopup(deviceName ?: "Baseus Bass BP1 Pro") }
                        .testTag("trigger_airpods_popup_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Launch,
                            contentDescription = "Ver Pop-up AirPods",
                            tint = PulseCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Pop-up",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PulseCyan
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Earbuds Render Card
        com.example.ui.components.glass.GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(185.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Ambient backlight halo behind earbuds
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        PulseBlue.copy(alpha = 0.38f),
                                        PulseCyan.copy(alpha = 0.12f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )

                    Image(
                        painter = painterResource(id = R.drawable.img_bp1_hero),
                        contentDescription = "Baseus Bass BP1 Pro Auriculares",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(18.dp)),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Battery Row with Refresh Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Estado de Batería",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(
                        onClick = { viewModel.refreshBattery() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar batería",
                            tint = PulseCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BatteryGauge(
                        label = "Izquierdo",
                        percent = batteryInfo.leftPercent,
                        isCharging = batteryInfo.isLeftCharging,
                        modifier = Modifier.weight(1f)
                    )
                    BatteryGauge(
                        label = "Estuche",
                        percent = batteryInfo.casePercent,
                        isCharging = batteryInfo.isCaseCharging,
                        isCase = true,
                        modifier = Modifier.weight(1f)
                    )
                    BatteryGauge(
                        label = "Derecho",
                        percent = batteryInfo.rightPercent,
                        isCharging = batteryInfo.isRightCharging,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Sound Effect & Mode Pills Row (JEBSPOD companion style)
        Text(
            text = "Efectos y Modos Rápidos",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val isBassActive = currentPreset == SoftwarePreset.BASS_BOOST || currentHwEq == HardwareEqPreset.BASS_BOOST
            val isSpatialActive = currentPreset == SoftwarePreset.POP
            val isVocalActive = currentPreset == SoftwarePreset.VOCALES

            SoundEffectChip(
                label = "Bass Boost+",
                icon = Icons.Default.MusicNote,
                isActive = isBassActive,
                onClick = {
                    viewModel.setSoftwarePreset(if (isBassActive) SoftwarePreset.ORIGINAL else SoftwarePreset.BASS_BOOST)
                }
            )

            SoundEffectChip(
                label = "Audio Espacial",
                icon = Icons.Default.SpatialAudio,
                isActive = isSpatialActive,
                onClick = {
                    viewModel.setSoftwarePreset(if (isSpatialActive) SoftwarePreset.ORIGINAL else SoftwarePreset.POP)
                }
            )

            SoundEffectChip(
                label = "Modo Juego",
                icon = Icons.Default.SportsEsports,
                isActive = isGameMode,
                onClick = {
                    viewModel.setGameMode(!isGameMode)
                }
            )

            SoundEffectChip(
                label = "Voz Clara",
                icon = Icons.Default.Hearing,
                isActive = isVocalActive,
                onClick = {
                    viewModel.setSoftwarePreset(if (isVocalActive) SoftwarePreset.ORIGINAL else SoftwarePreset.VOCALES)
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ANC Quick Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.NoiseAware,
                    contentDescription = null,
                    tint = PulseCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cancelación de Ruido (ANC)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            OutlinedButton(
                onClick = onNavigateToAnc,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Detalles", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        AncModeSelector(
            selectedMode = ancMode,
            commandStatus = ancStatus,
            onModeSelect = { viewModel.setAncMode(it) }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Low Latency / Game Mode Switch Card
        com.example.ui.components.glass.GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = if (isGameMode) PulseCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Modo Juego (Baja Latencia)",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isGameMode) "Latencia reducida a 0.038s (Opcode BA 24)" else "Modo audio estándar",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = isGameMode,
                    onCheckedChange = { viewModel.setGameMode(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PulseCyan,
                        checkedTrackColor = PulseBlue
                    ),
                    modifier = Modifier.testTag("game_mode_switch")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Equalizer Quick Summary Card with Live Audition Player
        com.example.ui.components.glass.GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.toggleAudition() },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isAuditionPlaying) PulseCyan else PulseBlue.copy(alpha = 0.22f))
                    ) {
                        Icon(
                            imageVector = if (isAuditionPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Probar sonido DSP",
                            tint = if (isAuditionPlaying) Color.Black else PulseCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Sonido y Ecualización",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (isAuditionPlaying) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• Sonando",
                                    fontSize = 11.sp,
                                    color = StatusGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "Preset: ${currentPreset.displayName} • HW: ${currentHwEq.label}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                OutlinedButton(
                    onClick = onNavigateToEq,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Abrir EQ", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bluetooth connection banner if not connected
        if (!isConnected) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = PulseBlue.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bluetooth,
                            contentDescription = null,
                            tint = PulseBlue
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Auriculares no enlazados por GATT",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    OutlinedButton(
                        onClick = onNavigateToConnection,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Buscar", fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SoundEffectChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeMode = com.example.ui.theme.LocalThemeMode.current
    val isGlass = themeMode == com.example.data.AppThemeMode.LIQUID_GLASS

    val chipBg = when {
        isActive -> PulseBlue.copy(alpha = 0.28f)
        isGlass -> Color.White.copy(alpha = 0.08f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    val chipBorder = if (isActive) {
        BorderStroke(1.5.dp, PulseCyan)
    } else if (isGlass) {
        BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = chipBg,
        border = chipBorder,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) PulseCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                color = if (isActive) PulseCyan else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
