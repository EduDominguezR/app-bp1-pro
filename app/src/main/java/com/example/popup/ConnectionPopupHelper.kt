package com.example.popup

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.protocol.BatteryInfo

object ConnectionPopupHelper {

    const val CHANNEL_ID = "baseus_pulse_connection"
    const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Conexión Baseus Pulse"
            val descriptionText = "Notificaciones de conexión y batería de Baseus BP1 Pro"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showConnectionNotification(
        context: Context,
        deviceName: String,
        batteryInfo: BatteryInfo
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("SHOW_POPUP", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val batteryText = if (batteryInfo.isAvailable) {
            val l = if (batteryInfo.leftPercent >= 0) "L: ${batteryInfo.leftPercent}%" else "L: --"
            val r = if (batteryInfo.rightPercent >= 0) "R: ${batteryInfo.rightPercent}%" else "R: --"
            val c = if (batteryInfo.casePercent >= 0) "Estuche: ${batteryInfo.casePercent}%" else ""
            listOf(l, r, c).filter { it.isNotEmpty() }.joinToString(" • ")
        } else {
            "Auriculares conectados y listos"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Baseus Bass BP1 Pro Conectado")
            .setContentText(batteryText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$batteryText\nPulsa para abrir Baseus Pulse y gestionar ANC o el ecualizador.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Abrir Controles",
                pendingIntent
            )

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
        } catch (e: SecurityException) {
            // Android 13+ POST_NOTIFICATIONS may not be granted yet
        }
    }

    fun dismissNotification(context: Context) {
        try {
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
        } catch (e: Exception) {
            // Ignore
        }
    }
}
