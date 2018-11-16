package de.zweidenker.iotp2ptest

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import android.support.annotation.StringRes
import de.zweidenker.iotp2ptest.util.ServicesActivity

abstract class BaseBluetoothActivity(@StringRes private val titleRes: Int): ServicesActivity(titleRes) {
    protected var bluetoothAdapter: BluetoothAdapter? = null
    private set
    private var wasEnabled: Boolean = false

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
        }
    }

    abstract fun onBluetoothManagerInstantiated()

    override fun onDestroy() {
        if(!wasEnabled) {
            bluetoothAdapter?.disable()
        }
        bluetoothAdapter = null
        super.onDestroy()
    }

    override fun startAsDiscovery() {
        if(bluetoothAdapter == null) {
            initiateBluetoothManagerWithPermissions()
        }
    }

    override fun startAsLocalService() {
        if(bluetoothAdapter == null) {
            initiateBluetoothManagerWithPermissions()
        }
    }

    override fun clearEverything() {
        serviceAdapter.clearList()
    }
}