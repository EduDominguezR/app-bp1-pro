package com.example.ui.screens

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
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
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetooth.BleConnectionState
import com.example.bluetooth.DiscoveredDevice
import com.example.ui.BaseusPulseViewModel
import com.example.ui.theme.PulseBlue
import com.example.ui.theme.PulseCyan
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed

@SuppressLint("MissingPermission")
@Composable
fun ConnectionScreen(
    viewModel: BaseusPulseViewModel,
    modifier: Modifier = Modifier
) {
    val connState by viewModel.connectionState.collectAsState()
    val connectedName by viewModel.connectedDeviceName.collectAsState()
    val connectedAddress by viewModel.connectedDeviceAddress.collectAsState()
    val discoveredList by viewModel.discoveredDevices.collectAsState()
    val gattInfo by viewModel.gattInfo.collectAsState()
    val currentRssi by viewModel.currentRssi.collectAsState()

    val isScanning = connState == BleConnectionState.BUSCANDO
    val isConnected = connState == BleConnectionState.CONECTADO

    // Query bonded devices
    val bondedDevices = remember(connState) {
        try {
            viewModel.bleManager.bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
        } catch (e: SecurityException) {
            emptyList()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag("connection_screen")
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Bluetooth,
                contentDescription = null,
                tint = PulseBlue,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Gestión de Conexión BLE",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Detección y enlace GATT con Baseus BP1 Pro",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Current Connection Status Card
        com.example.ui.components.glass.GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isConnected) StatusGreen else StatusRed)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = connState.displayName,
                            fontWeight = FontWeight.Bold,
                            color = if (isConnected) StatusGreen else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (isConnected) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SignalCellularAlt,
                                contentDescription = null,
                                tint = PulseCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$currentRssi dBm",
                                fontSize = 11.sp,
                                color = PulseCyan,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                if (isConnected) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = connectedName ?: "Baseus Bass BP1 Pro",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Dirección MAC: ${connectedAddress ?: "N/D"}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { viewModel.disconnectDevice() },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("disconnect_button")
                        ) {
                            Text("Desconectar GATT", color = StatusRed)
                        }

                        Button(
                            onClick = { viewModel.triggerInAppPopup(connectedName ?: "Baseus Bass BP1 Pro") },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PulseBlue),
                            modifier = Modifier.testTag("connection_popup_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Headphones,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pop-up AirPods")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Scan Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dispositivos Cercanos (BLE)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Button(
                onClick = {
                    if (isScanning) viewModel.stopBleScan() else viewModel.startBleScan()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isScanning) StatusRed else PulseBlue
                ),
                modifier = Modifier.testTag("scan_ble_button")
            ) {
                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Detener (10s)", fontSize = 12.sp)
                } else {
                    Icon(
                        imageVector = Icons.Default.BluetoothSearching,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Escanear", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Discovered Devices List
        if (discoveredList.isEmpty()) {
            com.example.ui.components.glass.GlassCard(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Headphones,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isScanning) "Buscando auriculares con publicidad BLE..." else "Pulsa 'Escanear' para buscar tus auriculares",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                discoveredList.forEach { dev ->
                    DeviceRow(
                        name = dev.name,
                        address = dev.address,
                        rssi = dev.rssi,
                        isBp1 = dev.isBp1Pro,
                        onConnect = { viewModel.connectDevice(dev.device) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Bonded Devices Section
        if (bondedDevices.isNotEmpty()) {
            Text(
                text = "Dispositivos Vinculados en el Sistema",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                bondedDevices.forEach { dev ->
                    val isBp1 = viewModel.bleManager.isLikelyBp1Pro(dev.name, dev.address)
                    DeviceRow(
                        name = dev.name ?: "Desconocido",
                        address = dev.address,
                        rssi = 0,
                        isBp1 = isBp1,
                        onConnect = { viewModel.connectDevice(dev) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // GATT Architecture Specs
        if (gattInfo.serviceUuid.isNotEmpty()) {
            com.example.ui.components.glass.GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Detalles Técnicos del Enlace GATT",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• Servicio: ${gattInfo.serviceUuid}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("• Característica WRITE: ${gattInfo.writeCharUuid}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("• Característica NOTIFY: ${gattInfo.notifyCharUuid}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("• MTU negociado: ${gattInfo.mtu} bytes", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun DeviceRow(
    name: String,
    address: String,
    rssi: Int,
    isBp1: Boolean,
    onConnect: () -> Unit
) {
    com.example.ui.components.glass.GlassCard(
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Headphones,
                    contentDescription = null,
                    tint = if (isBp1) PulseCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = name,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp
                        )
                        if (isBp1) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = PulseCyan.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "BP1 Pro",
                                    fontSize = 9.sp,
                                    color = PulseCyan,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "$address ${if (rssi != 0) "• $rssi dBm" else ""}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = onConnect,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isBp1) PulseBlue else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.testTag("connect_btn_${address.takeLast(4)}")
            ) {
                Text(
                    text = "Conectar",
                    fontSize = 11.sp,
                    color = if (isBp1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
