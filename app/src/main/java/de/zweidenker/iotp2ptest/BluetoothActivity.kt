package de.zweidenker.iotp2ptest

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build

class BluetoothActivity: BaseBluetoothActivity(R.string.test_bluetooth) {

    private var bluetoothReceiver: BroadcastReceiver? = null

    override fun onBluetoothAdapterStartDiscovery() {
        bluetoothAdapter?.startDiscovery()
        if(bluetoothReceiver == null) {
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
                            serviceAdapter.put(device.name, type, device.bondState, device.address)
                        }
                        BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                            toast("Completed Discovery")
                        }
                        BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                            toast("Started Discovery")
                        }
                    }
                }
            }
            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND).apply {
                addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            }
            registerReceiver(bluetoothReceiver, filter)
        }
        bluetoothAdapter?.startDiscovery()
    }

    override fun onBluetoothAdapterStartLocalService() {
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120)
        startActivityForResult(intent, RequestCode.DISCOVERABLE_BT.value)
    }

    override fun clearEverything() {
        if(bluetoothReceiver != null) {
            unregisterReceiver(bluetoothReceiver)
        }
        bluetoothReceiver = null
        super.clearEverything()
    }
}