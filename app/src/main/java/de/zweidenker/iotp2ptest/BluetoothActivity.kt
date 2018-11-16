package de.zweidenker.iotp2ptest

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build

class BluetoothActivity: BaseBluetoothActivity(R.string.test_bluetooth) {

    private var bluetoothReceiver: BroadcastReceiver? = null

    override fun onBluetoothManagerInstantiated() {
        bluetoothReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        // Discovery has found a device. Get the BluetoothDevice
                        // object and its info from the Intent.
                        val device: BluetoothDevice =
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                            device.type.toString()
                        } else {
                            "SDK-VER"
                        }
                        serviceAdapter.put(device.name, type, device.bondState)
                    }
                }
            }
        }
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(bluetoothReceiver, filter)
        bluetoothAdapter?.startDiscovery()
    }

    override fun onDestroy() {
        if(bluetoothReceiver != null) {
            unregisterReceiver(bluetoothReceiver)
        }
        bluetoothReceiver = null
        super.onDestroy()
    }
}