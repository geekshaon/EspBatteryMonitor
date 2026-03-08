package com.example.espbattery

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class EspDevice(val name: String, val address: String)

class AppViewModel : ViewModel() {
    private val _targetBattery = MutableStateFlow(80f)
    val targetBattery: StateFlow<Float> = _targetBattery.asStateFlow()

    private val _currentBattery = MutableStateFlow(0)
    val currentBattery: StateFlow<Int> = _currentBattery.asStateFlow()

    private val _btStatusMessage = MutableStateFlow("Disconnected")
    val btStatusMessage: StateFlow<String> = _btStatusMessage.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<EspDevice>>(emptyList())
    val pairedDevices: StateFlow<List<EspDevice>> = _pairedDevices.asStateFlow()

    private val _selectedDevice = MutableStateFlow<EspDevice?>(null)
    val selectedDevice: StateFlow<EspDevice?> = _selectedDevice.asStateFlow()

    fun setTargetBattery(level: Float) {
        _targetBattery.value = level
    }

    fun updateCurrentBattery(level: Int) {
        _currentBattery.value = level
    }

    fun updateBtStatus(message: String, connected: Boolean) {
        _btStatusMessage.value = message
        _isConnected.value = connected
    }

    fun setServiceRunning(running: Boolean) {
        _isServiceRunning.value = running
    }

    fun updatePairedDevices(devices: List<EspDevice>) {
        _pairedDevices.value = devices
    }

    fun selectDevice(device: EspDevice) {
        _selectedDevice.value = device
    }
}
