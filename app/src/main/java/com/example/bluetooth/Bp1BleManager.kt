package com.example.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.protocol.AckResult
import com.example.protocol.AncMode
import com.example.protocol.BatteryInfo
import com.example.protocol.Bp1Protocol
import com.example.protocol.CommandStatus
import com.example.protocol.HardwareEqPreset
import com.example.protocol.OP_ANC_CONTROL
import com.example.protocol.OP_BATTERY_QUERY
import com.example.protocol.OP_EQ_PRESET
import com.example.protocol.OP_GAME_MODE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

private const val TAG = "Bp1BleManager"

// Standard Client Characteristic Configuration Descriptor UUID
private val CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

enum class BleConnectionState(val displayName: String) {
    DESCONECTADO("Desconectado"),
    BUSCANDO("Buscando auriculares..."),
    CONECTANDO("Estableciendo enlace GATT..."),
    CONECTADO("Conectado"),
    RECONECTANDO("Reconectando..."),
    ERROR("Error de conexión")
}

data class DiscoveredDevice(
    val device: BluetoothDevice,
    val name: String,
    val address: String,
    val rssi: Int,
    val isBp1Pro: Boolean
)

data class BleGattInfo(
    val serviceUuid: String = "",
    val writeCharUuid: String = "",
    val notifyCharUuid: String = "",
    val mtu: Int = 23
)

class Bp1BleManager(private val context: Context) {

    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    val bluetoothAdapter: BluetoothAdapter? get() = bluetoothManager?.adapter
    private val scanner: BluetoothLeScanner? get() = bluetoothAdapter?.bluetoothLeScanner

    private val coroutineScope = CoroutineScope(Dispatchers.IO + Job())
    private val mainHandler = Handler(Looper.getMainLooper())

    private var currentGatt: BluetoothGatt? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null
    private var notifyCharacteristic: BluetoothGattCharacteristic? = null

    // State flows
    private val _connectionState = MutableStateFlow(BleConnectionState.DESCONECTADO)
    val connectionState: StateFlow<BleConnectionState> = _connectionState.asStateFlow()

    private val _connectedDeviceName = MutableStateFlow<String?>(null)
    val connectedDeviceName: StateFlow<String?> = _connectedDeviceName.asStateFlow()

    private val _connectedDeviceAddress = MutableStateFlow<String?>(null)
    val connectedDeviceAddress: StateFlow<String?> = _connectedDeviceAddress.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = _discoveredDevices.asStateFlow()

    private val _batteryInfo = MutableStateFlow(BatteryInfo())
    val batteryInfo: StateFlow<BatteryInfo> = _batteryInfo.asStateFlow()

    private val _currentAncMode = MutableStateFlow(AncMode.OFF)
    val currentAncMode: StateFlow<AncMode> = _currentAncMode.asStateFlow()

    private val _ancCommandStatus = MutableStateFlow(CommandStatus.IDLE)
    val ancCommandStatus: StateFlow<CommandStatus> = _ancCommandStatus.asStateFlow()

    private val _isGameModeEnabled = MutableStateFlow(false)
    val isGameModeEnabled: StateFlow<Boolean> = _isGameModeEnabled.asStateFlow()

    private val _gameModeStatus = MutableStateFlow(CommandStatus.IDLE)
    val gameModeStatus: StateFlow<CommandStatus> = _gameModeStatus.asStateFlow()

    private val _currentHwEq = MutableStateFlow(HardwareEqPreset.BALANCED)
    val currentHwEq: StateFlow<HardwareEqPreset> = _currentHwEq.asStateFlow()

    private val _hwEqStatus = MutableStateFlow(CommandStatus.IDLE)
    val hwEqStatus: StateFlow<CommandStatus> = _hwEqStatus.asStateFlow()

    private val _gattInfo = MutableStateFlow(BleGattInfo())
    val gattInfo: StateFlow<BleGattInfo> = _gattInfo.asStateFlow()

    private val _currentRssi = MutableStateFlow(-70)
    val currentRssi: StateFlow<Int> = _currentRssi.asStateFlow()

    private val _diagnosticLogs = MutableStateFlow<List<String>>(emptyList())
    val diagnosticLogs: StateFlow<List<String>> = _diagnosticLogs.asStateFlow()

    private val writeMutex = Mutex()
    private var scanTimeoutRunnable: Runnable? = null
    private var batteryPollingJob: Job? = null

    fun addLog(entry: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.US).format(java.util.Date())
        val formatted = "[$timestamp] $entry"
        Log.d(TAG, formatted)
        _diagnosticLogs.value = (_diagnosticLogs.value + formatted).takeLast(100)
    }

    /**
     * Start user-initiated BLE scan with strict 10s auto-stop to protect battery.
     */
    @SuppressLint("MissingPermission")
    fun startScan() {
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            addLog("Error: Adaptador Bluetooth desactivado")
            _connectionState.value = BleConnectionState.ERROR
            return
        }

        val bleScanner = scanner
        if (bleScanner == null) {
            addLog("Error: Escáner BLE no disponible")
            _connectionState.value = BleConnectionState.ERROR
            return
        }

        stopScan()
        _discoveredDevices.value = emptyList()
        _connectionState.value = BleConnectionState.BUSCANDO
        addLog("Iniciando escaneo BLE (Límite: 10s)...")

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        // Filter can include Baseus name matches
        val filters = listOf<ScanFilter>()

        try {
            bleScanner.startScan(filters, settings, scanCallback)

            // Auto-stop after 10 seconds to conserve battery
            scanTimeoutRunnable = Runnable {
                addLog("Tiempo de escaneo completado (10s)")
                stopScan()
            }
            mainHandler.postDelayed(scanTimeoutRunnable!!, 10000L)
        } catch (e: Exception) {
            addLog("Excepción al iniciar escaneo: ${e.message}")
            _connectionState.value = BleConnectionState.ERROR
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        scanTimeoutRunnable?.let { mainHandler.removeCallbacks(it) }
        scanTimeoutRunnable = null
        try {
            scanner?.stopScan(scanCallback)
        } catch (e: Exception) {
            // Ignore if already stopped
        }
        if (_connectionState.value == BleConnectionState.BUSCANDO) {
            _connectionState.value = if (currentGatt != null) BleConnectionState.CONECTADO else BleConnectionState.DESCONECTADO
        }
    }

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val dev = result.device ?: return
            val name = dev.name ?: result.scanRecord?.deviceName ?: "Desconocido"
            val addr = dev.address ?: ""
            val isBp1 = isLikelyBp1Pro(name, addr)

            val existing = _discoveredDevices.value
            val matchIndex = existing.indexOfFirst { it.address == addr }
            val item = DiscoveredDevice(dev, name, addr, result.rssi, isBp1)

            if (matchIndex >= 0) {
                val updated = existing.toMutableList()
                updated[matchIndex] = item
                _discoveredDevices.value = updated
            } else {
                _discoveredDevices.value = existing + item
            }
        }

        override fun onScanFailed(errorCode: Int) {
            addLog("Fallo en escaneo BLE código: $errorCode")
            _connectionState.value = BleConnectionState.ERROR
        }
    }

    fun isLikelyBp1Pro(name: String?, address: String): Boolean {
        if (name == null) return false
        val n = name.lowercase()
        return n.contains("baseus") || n.contains("bp1") || n.contains("ep10") || n.contains("pulse")
    }

    /**
     * Connects to target BluetoothDevice via GATT.
     */
    @SuppressLint("MissingPermission")
    fun connect(device: BluetoothDevice) {
        stopScan()
        // Reutilizar conexión activa si ya estamos conectados a este dispositivo
        if (currentGatt != null &&
            _connectedDeviceAddress.value == device.address &&
            _connectionState.value == BleConnectionState.CONECTADO
        ) {
            addLog("Ya conectado a ${device.address}. Reutilizando enlace GATT sin reconexión innecesaria.")
            return
        }
        disconnect()

        val devName = device.name ?: "Baseus BP1 Pro"
        addLog("Conectando con GATT a $devName (${device.address})...")
        _connectionState.value = BleConnectionState.CONECTANDO
        _connectedDeviceName.value = devName
        _connectedDeviceAddress.value = device.address

        try {
            currentGatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        } catch (e: Exception) {
            addLog("Error al invocar connectGatt: ${e.message}")
            _connectionState.value = BleConnectionState.ERROR
        }
    }

    /**
     * Disconnects GATT session and releases all hardware resources.
     */
    @SuppressLint("MissingPermission")
    fun disconnect() {
        batteryPollingJob?.cancel()
        batteryPollingJob = null

        currentGatt?.let { gatt ->
            try {
                addLog("Cerrando sesión GATT y liberando recursos")
                gatt.disconnect()
                gatt.close()
            } catch (e: Exception) {
                addLog("Excepción cerrando GATT: ${e.message}")
            }
        }
        currentGatt = null
        writeCharacteristic = null
        notifyCharacteristic = null
        _connectionState.value = BleConnectionState.DESCONECTADO
        _batteryInfo.value = BatteryInfo()
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            addLog("onConnectionStateChange: status=$status, newState=$newState")
            if (status != BluetoothGatt.GATT_SUCCESS) {
                addLog("Error en enlace GATT (status $status). Cerrando.")
                disconnect()
                _connectionState.value = BleConnectionState.ERROR
                return
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                _connectionState.value = BleConnectionState.CONECTANDO
                addLog("GATT Conectado. Negociando enlace...")
                try {
                    // Solicitar 256 bytes de manera no bloqueante (paquetes BP1 son de 2 a 4 bytes)
                    val requested = gatt.requestMtu(256)
                    if (!requested) {
                        gatt.discoverServices()
                    }
                } catch (e: Exception) {
                    gatt.discoverServices()
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                addLog("GATT Desconectado.")
                disconnect()
            }
        }

        @SuppressLint("MissingPermission")
        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            addLog("MTU negociado: $mtu (status: $status). Descubriendo servicios...")
            _gattInfo.value = _gattInfo.value.copy(mtu = mtu)
            gatt.discoverServices()
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                addLog("Fallo al descubrir servicios: $status")
                _connectionState.value = BleConnectionState.ERROR
                return
            }

            addLog("Servicios descubiertos con éxito (${gatt.services.size} servicios)")
            inspectAndAssignCharacteristics(gatt)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            handleIncomingPacket(value)
        }

        @Deprecated("Deprecated in Java for API 33")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            @Suppress("DEPRECATION")
            val data = characteristic.value ?: return
            handleIncomingPacket(data)
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                _currentRssi.value = rssi
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun inspectAndAssignCharacteristics(gatt: BluetoothGatt) {
        var foundWrite: BluetoothGattCharacteristic? = null
        var foundNotify: BluetoothGattCharacteristic? = null
        var activeServiceUuid = ""

        for (service in gatt.services) {
            for (char in service.characteristics) {
                val props = char.properties
                val canWrite = (props and (BluetoothGattCharacteristic.PROPERTY_WRITE or
                        BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) != 0
                val canNotify = (props and (BluetoothGattCharacteristic.PROPERTY_NOTIFY or
                        BluetoothGattCharacteristic.PROPERTY_INDICATE)) != 0

                if (canWrite && foundWrite == null) {
                    foundWrite = char
                    activeServiceUuid = service.uuid.toString()
                }
                if (canNotify && foundNotify == null) {
                    foundNotify = char
                }
            }
        }

        writeCharacteristic = foundWrite
        notifyCharacteristic = foundNotify

        _gattInfo.value = _gattInfo.value.copy(
            serviceUuid = activeServiceUuid,
            writeCharUuid = foundWrite?.uuid?.toString() ?: "No encontrado",
            notifyCharUuid = foundNotify?.uuid?.toString() ?: "No encontrado"
        )

        addLog("GATT Asignado -> Write: ${foundWrite?.uuid}, Notify: ${foundNotify?.uuid}")

        // Enable notifications on notifyCharacteristic
        foundNotify?.let { notifyChar ->
            gatt.setCharacteristicNotification(notifyChar, true)
            val desc = notifyChar.getDescriptor(CCCD_UUID)
            if (desc != null) {
                desc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gatt.writeDescriptor(desc)
                addLog("CCCD suscrito para notificaciones")
            }
        }

        _connectionState.value = BleConnectionState.CONECTADO
        addLog("Conexión con auriculares completada y operativa.")

        // Send initial battery query
        sendBatteryQuery()
        startBatteryPolling()
    }

    private fun handleIncomingPacket(data: ByteArray) {
        val hex = Bp1Protocol.toHexString(data)
        addLog("RX <- $hex")

        // 1. Check if battery response
        val battery = Bp1Protocol.parseBatteryResponse(data)
        if (battery != null && battery.isAvailable) {
            _batteryInfo.value = battery
            addLog("Batería parseada -> L: ${battery.leftPercent}%, R: ${battery.rightPercent}%, Estuche: ${battery.casePercent}%")
            return
        }

        // 2. Check ACK response
        val ackAnc = Bp1Protocol.parseAck(data, OP_ANC_CONTROL)
        when (ackAnc) {
            is AckResult.SpecificAck -> {
                _ancCommandStatus.value = CommandStatus.CONFIRMADO
                addLog("ACK específico de ANC recibido -> Modo confirmado")
            }
            is AckResult.GenericAck -> {
                _ancCommandStatus.value = CommandStatus.ENVIADO_SIN_CONFIRMACION
                addLog("ACK genérico recibido para ANC (el firmware no discrimina el modo)")
            }
            else -> {}
        }

        val ackGame = Bp1Protocol.parseAck(data, OP_GAME_MODE)
        when (ackGame) {
            is AckResult.SpecificAck -> {
                _gameModeStatus.value = CommandStatus.CONFIRMADO
                addLog("ACK específico de Modo Juego recibido")
            }
            is AckResult.GenericAck -> {
                _gameModeStatus.value = CommandStatus.ENVIADO_SIN_CONFIRMACION
            }
            else -> {}
        }

        val ackEq = Bp1Protocol.parseAck(data, OP_EQ_PRESET)
        when (ackEq) {
            is AckResult.SpecificAck -> {
                _hwEqStatus.value = CommandStatus.CONFIRMADO
            }
            is AckResult.GenericAck -> {
                _hwEqStatus.value = CommandStatus.ENVIADO_SIN_CONFIRMACION
            }
            else -> {}
        }
    }

    /**
     * Sends packet safely with write mutex to prevent concurrent GATT collisions.
     */
    @SuppressLint("MissingPermission")
    private suspend fun sendPacket(packet: ByteArray): Boolean {
        val gatt = currentGatt ?: run {
            addLog("No se puede enviar paquete: GATT no conectado")
            return false
        }
        val char = writeCharacteristic ?: run {
            addLog("No se puede enviar paquete: Característica WRITE no disponible")
            return false
        }

        return writeMutex.withLock {
            try {
                addLog("TX -> ${Bp1Protocol.toHexString(packet)}")
                char.value = packet
                val success = gatt.writeCharacteristic(char)
                delay(80) // Small spacing between BLE writes
                success
            } catch (e: Exception) {
                addLog("Excepción escribiendo característica: ${e.message}")
                false
            }
        }
    }

    fun setAncMode(mode: AncMode, level: Int = 0x80) {
        _currentAncMode.value = mode
        _ancCommandStatus.value = CommandStatus.ENVIANDO
        coroutineScope.launch {
            val packet = Bp1Protocol.encodeAncCommand(mode, level)
            val sent = sendPacket(packet)
            if (!sent) {
                _ancCommandStatus.value = CommandStatus.ERROR
            } else {
                // If within 1.5s no specific ACK arrives, update to ENVIADO_SIN_CONFIRMACION
                delay(1500)
                if (_ancCommandStatus.value == CommandStatus.ENVIANDO) {
                    _ancCommandStatus.value = CommandStatus.ENVIADO_SIN_CONFIRMACION
                }
            }
        }
    }

    fun setGameMode(enabled: Boolean) {
        _isGameModeEnabled.value = enabled
        _gameModeStatus.value = CommandStatus.ENVIANDO
        coroutineScope.launch {
            val packet = Bp1Protocol.encodeGameModeCommand(enabled)
            val sent = sendPacket(packet)
            if (!sent) {
                _gameModeStatus.value = CommandStatus.ERROR
            } else {
                delay(1200)
                if (_gameModeStatus.value == CommandStatus.ENVIANDO) {
                    _gameModeStatus.value = CommandStatus.ENVIADO_SIN_CONFIRMACION
                }
            }
        }
    }

    fun setHardwareEq(preset: HardwareEqPreset) {
        _currentHwEq.value = preset
        _hwEqStatus.value = CommandStatus.ENVIANDO
        coroutineScope.launch {
            val packet = Bp1Protocol.encodeHardwareEqPreset(preset)
            val sent = sendPacket(packet)
            if (!sent) {
                _hwEqStatus.value = CommandStatus.ERROR
            } else {
                delay(1200)
                if (_hwEqStatus.value == CommandStatus.ENVIANDO) {
                    _hwEqStatus.value = CommandStatus.ENVIADO_SIN_CONFIRMACION
                }
            }
        }
    }

    fun sendBatteryQuery() {
        coroutineScope.launch {
            val packet = Bp1Protocol.encodeBatteryQuery()
            sendPacket(packet)
            currentGatt?.readRemoteRssi()
        }
    }

    private val _isAppInForeground = MutableStateFlow(true)

    fun setAppForeground(isForeground: Boolean) {
        _isAppInForeground.value = isForeground
        addLog("Ciclo de vida app: foreground=$isForeground")
    }

    /**
     * Consulta adaptativa de batería:
     * - 45 segundos mientras la aplicación está en primer plano.
     * - 3 minutos (180s) cuando la aplicación pasa a segundo plano para ahorrar energía.
     */
    private fun startBatteryPolling() {
        batteryPollingJob?.cancel()
        batteryPollingJob = coroutineScope.launch {
            while (isActive && _connectionState.value == BleConnectionState.CONECTADO) {
                val delayMs = if (_isAppInForeground.value) 45000L else 180000L
                delay(delayMs)
                if (_connectionState.value == BleConnectionState.CONECTADO) {
                    sendBatteryQuery()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun readRssi() {
        currentGatt?.readRemoteRssi()
    }
}
