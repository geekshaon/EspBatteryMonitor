package com.example.espbattery

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

class BluetoothController(private val context: Context, private val viewModel: AppViewModel) {

    private val bluetoothManager: BluetoothManager = context.getSystemService(BluetoothManager::class.java)
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    // Standard SPP UUID for Bluetooth Classic
    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    @SuppressLint("MissingPermission")
    fun getPairedDevices() {
        if (bluetoothAdapter?.isEnabled == true) {
            val paired = bluetoothAdapter.bondedDevices
            val deviceList = paired.map { EspDevice(it.name ?: "Unknown", it.address) }
            viewModel.updatePairedDevices(deviceList)
        } else {
            viewModel.updateBtStatus("Bluetooth is disabled", false)
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun connectToDevice(macAddress: String): Boolean = withContext(Dispatchers.IO) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            viewModel.updateBtStatus("Bluetooth not available", false)
            return@withContext false
        }

        try {
            viewModel.updateBtStatus("Connecting...", false)
            val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(macAddress)
            bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            bluetoothAdapter.cancelDiscovery()
            bluetoothSocket?.connect()
            outputStream = bluetoothSocket?.outputStream

            viewModel.updateBtStatus("Connected to ${device.name}", true)
            return@withContext true
        } catch (e: IOException) {
            e.printStackTrace()
            closeConnection()
            viewModel.updateBtStatus("Connection Failed", false)
            return@withContext false
        }
    }

    fun sendData(data: String) {
        try {
            outputStream?.write(data.toByteArray())
            outputStream?.flush()
        } catch (e: IOException) {
            e.printStackTrace()
            closeConnection()
            viewModel.updateBtStatus("Disconnected (Send Error)", false)
            viewModel.setServiceRunning(false)
        }
    }

    fun closeConnection() {
        try {
            outputStream?.close()
            bluetoothSocket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            outputStream = null
            bluetoothSocket = null
            viewModel.updateBtStatus("Disconnected", false)
        }
    }
}
