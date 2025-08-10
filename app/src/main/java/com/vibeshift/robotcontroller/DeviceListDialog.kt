package com.vibeshift.robotcontroller

import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class DeviceListDialog(
    private val devices: List<BluetoothDevice>,
    private val onDeviceSelected: (BluetoothDevice) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val deviceNames = devices.map { d -> d.name ?: d.address }
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Select Device")
                .setItems(deviceNames.toTypedArray()) { _, which ->
                    onDeviceSelected(devices[which])
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
