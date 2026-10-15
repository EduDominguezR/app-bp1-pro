package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.BaseusPulseViewModel
import com.example.ui.theme.PulseBlue
import com.example.ui.theme.PulseCyan
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed

@Composable
fun SettingsScreen(
    viewModel: BaseusPulseViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isPopupEnabled by viewModel.isPopupEnabled.collectAsState()
    val isDarkTheme by viewModel.isDarkThemePref.collectAsState()
    val diagnosticLogs by viewModel.diagnosticLogs.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag("settings_screen")
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = PulseCyan,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Ajustes y Diagnóstico",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Configuración de pop-up, permisos y consola técnica",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Connection Pop-up Settings
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
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
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = PulseBlue,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Ventana Emergente de Conexión",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Estilo AirPods con telemetría de batería",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = isPopupEnabled,
                        onCheckedChange = { viewModel.setPopupEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PulseCyan,
                            checkedTrackColor = PulseBlue
                        ),
                        modifier = Modifier.testTag("popup_toggle_switch")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { viewModel.triggerInAppPopup() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("test_popup_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Probar Ventana Emergente en Vivo")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Appearance & Theme Selector Card (3-Way: Oscuro, Claro, Liquid Glass)
        val currentThemeMode by viewModel.themeMode.collectAsState()

        com.example.ui.components.glass.GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DarkMode,
                        contentDescription = null,
                        tint = PulseCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Estilo Visual y Apariencia",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Selecciona entre tema Oscuro, Claro o Liquid Glass translúcido",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ThemePreviewCard(
                        title = "Oscuro",
                        subtitle = "#10141D",
                        isSelected = currentThemeMode == com.example.data.AppThemeMode.DARK,
                        previewBg = androidx.compose.ui.graphics.Color(0xFF10141D),
                        previewCard = androidx.compose.ui.graphics.Color(0xFF191F2B),
                        accentColor = PulseBlue,
                        onClick = { viewModel.setThemeMode(com.example.data.AppThemeMode.DARK) },
                        modifier = Modifier.weight(1f)
                    )

                    ThemePreviewCard(
                        title = "Claro",
                        subtitle = "#F4F6FB",
                        isSelected = currentThemeMode == com.example.data.AppThemeMode.LIGHT,
                        previewBg = androidx.compose.ui.graphics.Color(0xFFF4F6FB),
                        previewCard = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
                        accentColor = com.example.ui.theme.LightPulseBlue,
                        isLightText = false,
                        onClick = { viewModel.setThemeMode(com.example.data.AppThemeMode.LIGHT) },
                        modifier = Modifier.weight(1f)
                    )

                    ThemePreviewCard(
                        title = "Liquid Glass",
                        subtitle = "Vidrio Translúcido",
                        isSelected = currentThemeMode == com.example.data.AppThemeMode.LIQUID_GLASS,
                        previewBg = androidx.compose.ui.graphics.Color(0xFF0B1020),
                        previewCard = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.15f),
                        accentColor = PulseCyan,
                        isGlass = true,
                        onClick = { viewModel.setThemeMode(com.example.data.AppThemeMode.LIQUID_GLASS) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Permissions Checklist Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = PulseBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Auditoría de Permisos de Android",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                PermissionItem(
                    name = "Bluetooth Connect (BLE)",
                    isGranted = checkPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                )
                PermissionItem(
                    name = "Bluetooth Scan",
                    isGranted = checkPermission(context, Manifest.permission.BLUETOOTH_SCAN)
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    PermissionItem(
                        name = "Notificaciones (Pop-up en segundo plano)",
                        isGranted = checkPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Diagnostic Hex Log Console
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            )
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = null,
                            tint = PulseCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Consola de Diagnóstico BLE",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = "${diagnosticLogs.size} eventos",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (diagnosticLogs.isEmpty()) {
                            Text(
                                text = "Sin eventos registrados todavía.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontFamily = FontFamily.Monospace
                            )
                        } else {
                            diagnosticLogs.reversed().forEach { log ->
                                Text(
                                    text = log,
                                    fontSize = 10.sp,
                                    color = PulseCyan,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // About & Spec Disclaimer
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = PulseBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Información del Modelo",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "• Modelo: Baseus Bass BP1 Pro (EP10 Pro / BP1 Ultra)\n" +
                            "• Chipset: Bluetrum BLE + JieLi OTA\n" +
                            "• Identificador de sesión provisto: 53527aa4-29f7-ae11-4e74-997334782568 (catalogado como hash de referencia interna; no se inyecta como UUID de Bluetooth)\n" +
                            "• Firmware OTA: Bloqueado por seguridad para evitar bricking.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun PermissionItem(name: String, isGranted: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isGranted) StatusGreen else StatusRed)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isGranted) "Concedido" else "No otorgado",
                fontSize = 11.sp,
                color = if (isGranted) StatusGreen else StatusRed,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun checkPermission(context: Context, permission: String): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S &&
        (permission == Manifest.permission.BLUETOOTH_SCAN || permission == Manifest.permission.BLUETOOTH_CONNECT)) {
        return true
    }
    return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

@Composable
private fun ThemePreviewCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    previewBg: Color,
    previewCard: Color,
    accentColor: Color,
    isLightText: Boolean = true,
    isGlass: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) PulseCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mini Preview Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(previewBg)
                .border(
                    BorderStroke(borderWidth, borderColor),
                    RoundedCornerShape(12.dp)
                )
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            // Miniature floating card
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(34.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(previewCard)
                    .then(
                        if (isGlass) {
                            Modifier.border(
                                BorderStroke(
                                    1.dp,
                                    Color.White.copy(alpha = 0.35f)
                                ),
                                RoundedCornerShape(6.dp)
                            )
                        } else Modifier
                    )
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(3.dp)
                            .clip(CircleShape)
                            .background(if (isLightText) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.4f))
                    )
                }
            }

            // Radio check badge
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(PulseCyan),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Seleccionado",
                        tint = Color.Black,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) PulseCyan else MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
        Text(
            text = subtitle,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}
