package com.vibeshift.robotcontroller

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.*
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

@SuppressLint("MissingPermission")
class BluetoothService(
    private val bluetoothAdapter: BluetoothAdapter,
    private val connectionCallback: (Boolean) -> Unit
) {
    companion object {
        private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    private var bluetoothSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    private var isConnected = false
    private var connectionJob: Job? = null
    private var readJob: Job? = null

    fun getPairedDevices(): Set<BluetoothDevice> {
        return bluetoothAdapter.bondedDevices ?: emptySet()
    }

    fun connect(device: BluetoothDevice) {
        disconnect()

        connectionJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                bluetoothAdapter.cancelDiscovery()
                bluetoothSocket?.connect()

                inputStream = bluetoothSocket?.inputStream
                outputStream = bluetoothSocket?.outputStream

                isConnected = true
                connectionCallback(true)

                startReading()
            } catch (e: IOException) {
                disconnect()
                connectionCallback(false)
            }
        }
    }

    private fun startReading() {
        readJob = CoroutineScope(Dispatchers.IO).launch {
            val buffer = ByteArray(1024)
            while (isConnected && currentCoroutineContext().isActive) {
                try {
                    val bytesRead = inputStream?.read(buffer) ?: 0
                    if (bytesRead > 0) {
                        val receivedData = String(buffer, 0, bytesRead)
                        // Process received data here (sensor readings, status, etc.)
                    }
                } catch (e: IOException) {
                    break
                }
            }
        }
    }

    fun sendCommand(command: String): Boolean {
        return try {
            if (isConnected && outputStream != null) {
                outputStream?.write("$command\n".toByteArray())
                outputStream?.flush()
                true
            } else {
                false
            }
        } catch (e: IOException) {
            false
        }
    }

    fun isConnected(): Boolean = isConnected

    fun disconnect() {
        connectionJob?.cancel()
        readJob?.cancel()

        isConnected = false

        try {
            inputStream?.close()
            outputStream?.close()
            bluetoothSocket?.close()
        } catch (e: IOException) {
            // Ignore
        }

        inputStream = null
        outputStream = null
        bluetoothSocket = null

        connectionCallback(false)
    }
}
