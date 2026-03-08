package com.example.espbattery

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.espbattery.ui.theme.EspBatteryTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels()
    private lateinit var bluetoothController: BluetoothController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        bluetoothController = BluetoothController(this, viewModel)
        BatteryMonitorService.bluetoothController = bluetoothController
        BatteryMonitorService.viewModel = viewModel

        setContent {
            EspBatteryTheme {
                MainScreen(
                    viewModel = viewModel,
                    onScanClick = {
                        bluetoothController.getPairedDevices()
                    },
                    onConnectDevice = { device ->
                        lifecycleScope.launch {
                            bluetoothController.connectToDevice(device.address)
                        }
                    },
                    onDisconnectClick = {
                        bluetoothController.closeConnection()
                    },
                    onStartClick = {
                        val serviceIntent = Intent(this, BatteryMonitorService::class.java)
                        if (viewModel.isServiceRunning.value) {
                            stopService(serviceIntent)
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                startForegroundService(serviceIntent)
                            } else {
                                startService(serviceIntent)
                            }
                        }
                    }
                )
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        bluetoothController.closeConnection()
        val serviceIntent = Intent(this, BatteryMonitorService::class.java)
        stopService(serviceIntent)
    }
}
