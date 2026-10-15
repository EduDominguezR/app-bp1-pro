package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BaseusPulseApp
import com.example.audio.DspEngine
import com.example.audio.EQ_FREQUENCIES
import com.example.audio.SoftwarePreset
import com.example.bluetooth.BleConnectionState
import com.example.bluetooth.Bp1BleManager
import com.example.bluetooth.DiscoveredDevice
import com.example.data.PreferencesManager
import com.example.protocol.AncMode
import com.example.protocol.BatteryInfo
import com.example.protocol.CommandStatus
import com.example.protocol.HardwareEqPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BaseusPulseViewModel : ViewModel() {

    private val app = BaseusPulseApp.instance
    val bleManager: Bp1BleManager = app.bleManager
    val dspEngine: DspEngine = app.dspEngine
    private val prefs: PreferencesManager = app.preferencesManager

    // BLE state
    val connectionState: StateFlow<BleConnectionState> = bleManager.connectionState
    val connectedDeviceName: StateFlow<String?> = bleManager.connectedDeviceName
    val connectedDeviceAddress: StateFlow<String?> = bleManager.connectedDeviceAddress
    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = bleManager.discoveredDevices
    val batteryInfo: StateFlow<BatteryInfo> = bleManager.batteryInfo
    val currentAncMode: StateFlow<AncMode> = bleManager.currentAncMode
    val ancCommandStatus: StateFlow<CommandStatus> = bleManager.ancCommandStatus
    val isGameModeEnabled: StateFlow<Boolean> = bleManager.isGameModeEnabled
    val gameModeStatus: StateFlow<CommandStatus> = bleManager.gameModeStatus
    val currentHwEq: StateFlow<HardwareEqPreset> = bleManager.currentHwEq
    val hwEqStatus: StateFlow<CommandStatus> = bleManager.hwEqStatus
    val diagnosticLogs: StateFlow<List<String>> = bleManager.diagnosticLogs
    val currentRssi: StateFlow<Int> = bleManager.currentRssi
    val gattInfo = bleManager.gattInfo

    // DSP State
    val softwarePreset: StateFlow<SoftwarePreset> = dspEngine.currentPreset
    val bandGains: StateFlow<List<Float>> = dspEngine.bandGains
    val preampGainDb: StateFlow<Float> = dspEngine.preampGainDb
    val isLimiterEnabled: StateFlow<Boolean> = dspEngine.isLimiterEnabled
    val isAuditionPlaying: StateFlow<Boolean> = dspEngine.isAuditionPlaying
    val dspActive: StateFlow<Boolean> = dspEngine.dspActive

    // Experimental ANC Level (0x10 to 0xFF)
    private val _ancIntensity = MutableStateFlow(128)
    val ancIntensity: StateFlow<Int> = _ancIntensity.asStateFlow()

    // Settings preferences
    val isPopupEnabled: StateFlow<Boolean> = prefs.isPopupEnabled
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val themeMode: StateFlow<com.example.data.AppThemeMode> = prefs.themeMode
        .stateIn(viewModelScope, SharingStarted.Lazily, com.example.data.AppThemeMode.DARK)

    val isDarkThemePref: StateFlow<Boolean> = prefs.isDarkTheme
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    // Popup modal in-app visibility
    val showInAppPopup: StateFlow<Boolean> = app.showInAppPopup
    val popupDeviceName: StateFlow<String> = app.popupDeviceName

    init {
        // Load saved preferences
        viewModelScope.launch {
            val savedPreset = prefs.activePresetName.first()
            val savedGains = prefs.bandGains.first()
            val savedPreamp = prefs.preampDb.first()
            val savedLimiter = prefs.isLimiterEnabled.first()

            val preset = SoftwarePreset.fromName(savedPreset)
            dspEngine.applyPreset(preset)
            if (preset == SoftwarePreset.PERSONALIZADO && savedGains.size == 10) {
                dspEngine.setBandGains(savedGains)
            }
            dspEngine.setPreampGain(savedPreamp)
            dspEngine.setLimiterEnabled(savedLimiter)
        }
    }

    fun startBleScan() = bleManager.startScan()
    fun stopBleScan() = bleManager.stopScan()
    fun connectDevice(device: android.bluetooth.BluetoothDevice) = bleManager.connect(device)
    fun disconnectDevice() = bleManager.disconnect()
    fun refreshBattery() = bleManager.sendBatteryQuery()

    fun setAncMode(mode: AncMode) {
        bleManager.setAncMode(mode, _ancIntensity.value)
        viewModelScope.launch {
            prefs.saveAncMode(mode.code.toInt())
        }
    }

    fun setAncIntensity(level: Int) {
        val clamped = level.coerceIn(16, 255)
        _ancIntensity.value = clamped
        if (currentAncMode.value != AncMode.OFF) {
            bleManager.setAncMode(currentAncMode.value, clamped)
        }
    }

    fun setGameMode(enabled: Boolean) {
        bleManager.setGameMode(enabled)
        viewModelScope.launch {
            prefs.saveGameMode(enabled)
        }
    }

    fun setHardwareEq(preset: HardwareEqPreset) {
        bleManager.setHardwareEq(preset)
    }

    fun setSoftwarePreset(preset: SoftwarePreset) {
        dspEngine.applyPreset(preset)
        viewModelScope.launch {
            prefs.savePreset(preset.name, preset.gains)
        }
    }

    private var saveGainsJob: kotlinx.coroutines.Job? = null
    private var savePreampJob: kotlinx.coroutines.Job? = null

    fun setBandGain(index: Int, gainDb: Float) {
        dspEngine.setBandGain(index, gainDb)
        // Actualización inmediata en memoria y filtros DSP; escritura en DataStore con debounce para evitar I/O repetitivo
        saveGainsJob?.cancel()
        saveGainsJob = viewModelScope.launch {
            kotlinx.coroutines.delay(350)
            prefs.savePreset(SoftwarePreset.PERSONALIZADO.name, dspEngine.bandGains.value)
        }
    }

    fun setPreampGain(gainDb: Float) {
        dspEngine.setPreampGain(gainDb)
        // Debounce de persistencia en disco
        savePreampJob?.cancel()
        savePreampJob = viewModelScope.launch {
            kotlinx.coroutines.delay(350)
            prefs.savePreamp(gainDb)
        }
    }

    fun setLimiterEnabled(enabled: Boolean) {
        dspEngine.setLimiterEnabled(enabled)
        viewModelScope.launch {
            prefs.saveLimiterEnabled(enabled)
        }
    }

    fun setDspActive(active: Boolean) {
        dspEngine.setDspActive(active)
    }

    fun resetEq() {
        dspEngine.resetToFlat()
        viewModelScope.launch {
            prefs.savePreset(SoftwarePreset.ORIGINAL.name, List(10) { 0f })
            prefs.savePreamp(0f)
        }
    }

    fun toggleAudition() = dspEngine.toggleAudition()

    fun getResponseCurvePoints(): List<Pair<Float, Float>> {
        return dspEngine.computeResponseCurve(80)
    }

    fun setPopupEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefs.savePopupEnabled(enabled)
        }
    }

    fun setThemeMode(mode: com.example.data.AppThemeMode) {
        viewModelScope.launch {
            prefs.saveThemeMode(mode)
        }
    }

    fun setDarkTheme(isDark: Boolean) {
        viewModelScope.launch {
            prefs.saveDarkTheme(isDark)
        }
    }

    fun setAppForeground(isForeground: Boolean) {
        bleManager.setAppForeground(isForeground)
    }

    fun stopAudition() {
        dspEngine.stopAudition()
    }

    fun triggerInAppPopup(name: String = "Baseus Bass BP1 Pro") {
        app.triggerPopup(name)
    }

    fun dismissPopup() {
        app.dismissPopup()
    }

    override fun onCleared() {
        super.onCleared()
        dspEngine.stopAudition()
    }
}
