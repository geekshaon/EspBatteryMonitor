package com.example.espbattery

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class BatteryMonitorService : Service() {

    companion object {
        const val CHANNEL_ID = "BatteryMonitorChannel"
        const val NOTIFICATION_ID = 1
        
        // Static references for simplicity in this specific project layout
        var bluetoothController: BluetoothController? = null
        var viewModel: AppViewModel? = null
    }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                
                if (level != -1 && scale != -1) {
                    val batteryPct = (level * 100) / scale
                    viewModel?.updateCurrentBattery(batteryPct)

                    val targetPct = viewModel?.targetBattery?.value?.toInt() ?: 100

                    if (batteryPct >= targetPct) {
                        // Send termination signal
                        bluetoothController?.sendData("999\n") 
                        stopSelf()
                    } else {
                        // Send normal battery level
                        bluetoothController?.sendData("$batteryPct\n")
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ESP Battery Monitor")
            .setContentText("Monitoring battery and sending over Bluetooth")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
        viewModel?.setServiceRunning(true)

        // Register receiver for battery changes
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryReceiver, filter)

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryReceiver)
        bluetoothController?.closeConnection()
        viewModel?.setServiceRunning(false)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Battery Monitor Subagent",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}
