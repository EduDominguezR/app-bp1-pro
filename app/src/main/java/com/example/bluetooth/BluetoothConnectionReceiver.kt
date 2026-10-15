package com.example.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.BaseusPulseApp
import com.example.popup.ConnectionPopupHelper
import com.example.protocol.BatteryInfo

private const val TAG = "BtConnectionReceiver"

class BluetoothConnectionReceiver : BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val device: BluetoothDevice? = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        }

        val deviceName = try {
            device?.name ?: "Dispositivo Bluetooth"
        } catch (e: SecurityException) {
            "Dispositivo Bluetooth"
        }

        Log.d(TAG, "Bluetooth event received: action=$action, device=$deviceName (${device?.address})")

        val app = context.applicationContext as? BaseusPulseApp
        val isTargetDevice = deviceName.lowercase().let {
            it.contains("baseus") || it.contains("bp1") || it.contains("ep10") || it.contains("pulse")
        }

        when (action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                if (isTargetDevice && device != null) {
                    Log.i(TAG, "Baseus BP1 Pro conectado por ACL")
                    // Show notification connection popup without background service
                    ConnectionPopupHelper.showConnectionNotification(
                        context,
                        deviceName,
                        app?.bleManager?.batteryInfo?.value ?: BatteryInfo()
                    )
                    app?.bleManager?.connect(device)
                    app?.triggerPopup(deviceName)
                }
            }
            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                if (isTargetDevice) {
                    Log.i(TAG, "Baseus BP1 Pro desconectado por ACL")
                    ConnectionPopupHelper.dismissNotification(context)
                    app?.bleManager?.disconnect()
                }
            }
            BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED -> {
                val state = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, BluetoothA2dp.STATE_DISCONNECTED)
                if (state == BluetoothA2dp.STATE_CONNECTED && isTargetDevice && device != null) {
                    Log.i(TAG, "Baseus BP1 Pro conectado por A2DP")
                    ConnectionPopupHelper.showConnectionNotification(
                        context,
                        deviceName,
                        app?.bleManager?.batteryInfo?.value ?: BatteryInfo()
                    )
                    app?.triggerPopup(deviceName)
                }
            }
        }
    }
}
