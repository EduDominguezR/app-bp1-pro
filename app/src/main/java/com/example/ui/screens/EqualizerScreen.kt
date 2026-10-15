package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.EQ_FREQUENCIES
import com.example.audio.SoftwarePreset
import com.example.protocol.HardwareEqPreset
import com.example.ui.BaseusPulseViewModel
import com.example.ui.components.EqualizerCurveCanvas
import com.example.ui.components.FrequencyBandSlider
import com.example.ui.theme.PulseBlue
import com.example.ui.theme.PulseCyan
import com.example.ui.theme.StatusGreen

@Composable
fun EqualizerScreen(
    viewModel: BaseusPulseViewModel,
    modifier: Modifier = Modifier
) {
    val currentPreset by viewModel.softwarePreset.collectAsState()
    val bandGains by viewModel.bandGains.collectAsState()
    val preampGain by viewModel.preampGainDb.collectAsState()
    val isLimiterEnabled by viewModel.isLimiterEnabled.collectAsState()
    val isAuditionPlaying by viewModel.isAuditionPlaying.collectAsState()
    val dspActive by viewModel.dspActive.collectAsState()
    val currentHwEq by viewModel.currentHwEq.collectAsState()
    val hwEqStatus by viewModel.hwEqStatus.collectAsState()

    // Automatically stop live DSP audition when navigating away from the Equalizer screen
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            viewModel.stopAudition()
        }
    }

    // Recalculate response curve when gains or preamp change
    val curvePoints = remember(bandGains, preampGain, dspActive) {
        viewModel.getResponseCurvePoints()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag("equalizer_screen")
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.GraphicEq,
                contentDescription = null,
                tint = PulseBlue,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Ecualizador y Procesamiento DSP",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Ajuste de hardware y ecualizador gráfico de 10 bandas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // SECTION 1: Hardware EQ Presets
        com.example.ui.components.glass.GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "1. Presets de Hardware (Chip BP1 Pro)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Procesamiento global interno en el DAC de los auriculares",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PulseBlue.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = hwEqStatus.label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PulseBlue,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HardwareEqPreset.entries.forEach { hwPreset ->
                        val isSelected = currentHwEq == hwPreset
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setHardwareEq(hwPreset) },
                            label = {
                                Text(
                                    text = hwPreset.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PulseBlue,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("hw_chip_${hwPreset.name.lowercase()}")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SECTION 2: 10-Band Software Graphic Equalizer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "2. Ecualizador Gráfico de 10 Bandas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Filtros biquad con respuesta en tiempo real",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Activo", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(6.dp))
                Switch(
                    checked = dspActive,
                    onCheckedChange = { viewModel.setDspActive(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PulseCyan,
                        checkedTrackColor = PulseBlue
                    ),
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Real Mathematical Curve Canvas
        EqualizerCurveCanvas(points = curvePoints)

        Spacer(modifier = Modifier.height(14.dp))

        // Presets Chips Carousel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SoftwarePreset.entries.forEach { preset ->
                val isSelected = currentPreset == preset
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setSoftwarePreset(preset) },
                    label = {
                        Text(
                            text = preset.displayName,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PulseCyan,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    modifier = Modifier.testTag("preset_chip_${preset.name.lowercase()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 10 Frequency Faders
        com.example.ui.components.glass.GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    EQ_FREQUENCIES.forEachIndexed { index, freq ->
                        val gain = if (index < bandGains.size) bandGains[index] else 0f
                        FrequencyBandSlider(
                            freqHz = freq,
                            gainDb = gain,
                            onGainChange = { newGain ->
                                viewModel.setBandGain(index, newGain)
                            },
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Preamp & Limiter Controls
        com.example.ui.components.glass.GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Preamp Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ganancia de Preamplificación",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "%+.1f dB".format(preampGain),
                        fontWeight = FontWeight.Bold,
                        color = PulseBlue,
                        fontSize = 13.sp
                    )
                }

                Slider(
                    value = preampGain,
                    onValueChange = { viewModel.setPreampGain(it) },
                    valueRange = -6f..6f,
                    colors = SliderDefaults.colors(
                        thumbColor = PulseBlue,
                        activeTrackColor = PulseCyan
                    ),
                    modifier = Modifier.testTag("preamp_slider")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Limiter toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = if (isLimiterEnabled) StatusGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Protección Anti-Clipping (Limitador)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Saturación suave tanh para prevenir picos >0 dBFS (distorsión armónica suave)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = isLimiterEnabled,
                        onCheckedChange = { viewModel.setLimiterEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = StatusGreen,
                            checkedTrackColor = PulseCyan.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.testTag("limiter_switch")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Audition and Reset Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { viewModel.toggleAudition() },
                modifier = Modifier
                    .weight(1.3f)
                    .testTag("audition_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAuditionPlaying) PulseCyan else PulseBlue
                )
            ) {
                Icon(
                    imageVector = if (isAuditionPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isAuditionPlaying) "Detener Audición" else "Audición DSP en Vivo")
            }

            OutlinedButton(
                onClick = { viewModel.resetEq() },
                modifier = Modifier
                    .weight(0.9f)
                    .testTag("reset_eq_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Plano")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Technical Notice Card
        com.example.ui.components.glass.GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = PulseCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Alcance del ecualizador: El ecualizador gráfico de 10 bandas por software procesa sesiones de audio compatibles y reproducción interna con cálculo de filtros biquad de alta precisión. Para aplicar un perfil a todo el audio del sistema sin depender de sesiones, utiliza los 'Presets de Hardware' integrados arriba.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
