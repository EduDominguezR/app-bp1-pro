package com.example

import android.app.Application
import com.example.audio.DspEngine
import com.example.bluetooth.Bp1BleManager
import com.example.data.PreferencesManager
import com.example.popup.ConnectionPopupHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BaseusPulseApp : Application() {

    lateinit var bleManager: Bp1BleManager
        private set

    lateinit var dspEngine: DspEngine
        private set

    lateinit var preferencesManager: PreferencesManager
        private set

    private val _showInAppPopup = MutableStateFlow(false)
    val showInAppPopup: StateFlow<Boolean> = _showInAppPopup.asStateFlow()

    private val _popupDeviceName = MutableStateFlow("Baseus Bass BP1 Pro")
    val popupDeviceName: StateFlow<String> = _popupDeviceName.asStateFlow()

    override fun onCreate() {
        super.onCreate()
        instance = this

        ConnectionPopupHelper.createNotificationChannel(this)
        bleManager = Bp1BleManager(this)
        dspEngine = DspEngine()
        preferencesManager = PreferencesManager(this)
    }

    fun triggerPopup(deviceName: String = "Baseus Bass BP1 Pro") {
        _popupDeviceName.value = deviceName
        _showInAppPopup.value = true
    }

    fun dismissPopup() {
        _showInAppPopup.value = false
    }

    override fun onTerminate() {
        super.onTerminate()
        dspEngine.release()
        bleManager.disconnect()
    }

    companion object {
        lateinit var instance: BaseusPulseApp
            private set
    }
}
