package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.NoiseAware
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.protocol.AncMode
import com.example.protocol.CommandStatus
import com.example.ui.theme.PulseBlue
import com.example.ui.theme.PulseCyan
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed

@Composable
fun AncModeSelector(
    selectedMode: AncMode,
    commandStatus: CommandStatus,
    onModeSelect: (AncMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AncModeCard(
                mode = AncMode.OFF,
                label = "Normal",
                subtext = "Sin ANC",
                icon = Icons.Default.VolumeOff,
                isSelected = selectedMode == AncMode.OFF,
                onClick = { onModeSelect(AncMode.OFF) },
                modifier = Modifier.weight(1f)
            )

            AncModeCard(
                mode = AncMode.ANC_ON,
                label = "ANC Activo",
                subtext = "-50 dB",
                icon = Icons.Default.NoiseAware,
                isSelected = selectedMode == AncMode.ANC_ON,
                onClick = { onModeSelect(AncMode.ANC_ON) },
                modifier = Modifier.weight(1f)
            )

            AncModeCard(
                mode = AncMode.TRANSPARENCY,
                label = "Transparente",
                subtext = "Voz ambiental",
                icon = Icons.Default.Hearing,
                isSelected = selectedMode == AncMode.TRANSPARENCY,
                onClick = { onModeSelect(AncMode.TRANSPARENCY) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        val themeMode = com.example.ui.theme.LocalThemeMode.current
        val isGlass = themeMode == com.example.data.AppThemeMode.LIQUID_GLASS

        // Command verification banner
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isGlass) androidx.compose.ui.graphics.Color.White.copy(alpha = 0.07f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = if (isGlass) androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color.White.copy(alpha = 0.15f)) else null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val statusDotColor = when (commandStatus) {
                    CommandStatus.CONFIRMADO -> StatusGreen
                    CommandStatus.ENVIADO_SIN_CONFIRMACION -> StatusAmber
                    CommandStatus.ERROR -> StatusRed
                    CommandStatus.ENVIANDO -> PulseBlue
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = statusDotColor.copy(alpha = 0.2f),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = commandStatus.label,
                        color = statusDotColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = if (commandStatus == CommandStatus.ENVIADO_SIN_CONFIRMACION) {
                        "El comando BA 34 fue transmitido. El firmware del auricular respondió con ACK genérico."
                    } else if (commandStatus == CommandStatus.CONFIRMADO) {
                        "Modo verificado por telemetría del auricular."
                    } else {
                        "Control directo sobre el chip ANC de los BP1 Pro."
                    },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
private fun AncModeCard(
    mode: AncMode,
    label: String,
    subtext: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeMode = com.example.ui.theme.LocalThemeMode.current
    val isGlass = themeMode == com.example.data.AppThemeMode.LIQUID_GLASS

    val borderColor by animateColorAsState(
        targetValue = when {
            isSelected -> PulseCyan
            isGlass -> androidx.compose.ui.graphics.Color.White.copy(alpha = 0.16f)
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        },
        label = "anc_border"
    )

    val cardColor by animateColorAsState(
        targetValue = when {
            isSelected && isGlass -> androidx.compose.ui.graphics.Color.White.copy(alpha = 0.16f)
            !isSelected && isGlass -> androidx.compose.ui.graphics.Color.White.copy(alpha = 0.06f)
            isSelected -> MaterialTheme.colorScheme.surfaceVariant
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        },
        label = "anc_bg"
    )

    Card(
        modifier = modifier
            .testTag("anc_card_${mode.name.lowercase()}")
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) PulseCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = subtext,
                fontSize = 10.sp,
                color = if (isSelected) PulseBlue else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
