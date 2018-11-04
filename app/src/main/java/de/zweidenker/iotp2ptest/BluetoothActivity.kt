package de.zweidenker.iotp2ptest

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import de.zweidenker.iotp2ptest.util.ServicesActivity

class BluetoothActivity: ServicesActivity(R.string.test_bluetooth) {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var wasEnabled: Boolean = false

    private var bluetoothReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initiateBluetoothManagerWithPermissions()
    }

    private fun initiateBluetoothManagerWithPermissions() {
        withPermissions(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_COARSE_LOCATION, callback= ::initiateBluetoothManager)
    }

    private fun initiateBluetoothManager() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter().apply {
            if(this == null) {
                toast("Device does not support bluetooth!")
                return
            }
            wasEnabled = isEnabled
            if(!isEnabled) {
                enable()
            }
            //TODO offloadedFiltering

            initiateBluetoothReceiver()
            startDiscovery()
            //TODO!
        }
    }


    private fun initiateBluetoothReceiver() {
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

    }
    override fun onDestroy() {
        super.onDestroy()
        if(!wasEnabled) {
            bluetoothAdapter?.disable()
        }
        if(bluetoothReceiver != null) {
            unregisterReceiver(bluetoothReceiver)
        }
        bluetoothAdapter = null
        bluetoothReceiver = null
    }

    override fun startAsDiscovery() {
        TODO("not implemented")
    }

    override fun startAsLocalService() {
        TODO("not implemented")
    }

    override fun clearEverything() {
        serviceAdapter.clearList()
    }
}