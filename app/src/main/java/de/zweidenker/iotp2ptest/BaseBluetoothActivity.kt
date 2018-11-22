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
    protected var isDiscovery: Boolean = false
    private set

    private fun initiateBluetoothAdapterWithPermissions() {
        withPermissions(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_COARSE_LOCATION, callback= ::initiateBluetoothAdapter)
    }

    private fun initiateBluetoothAdapter() {
        if(bluetoothAdapter == null) {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter().apply {
                if (this == null) {
                    toast("Device does not support bluetooth!")
                    finish()
                    return
                }
                wasEnabled = isEnabled
                if (!isEnabled) {
                    enable()
                }
            }
        }
        if(isDiscovery) {
            onBluetoothAdapterStartDiscovery()
        } else {
            onBluetoothAdapterStartLocalService()
        }
    }

    abstract fun onBluetoothAdapterStartDiscovery()
    abstract fun onBluetoothAdapterStartLocalService()


    override fun onDestroy() {
        clearEverything()
        super.onDestroy()
    }

    override fun startAsDiscovery() {
        isDiscovery = true
        if(bluetoothAdapter == null) {
            initiateBluetoothAdapterWithPermissions()
        } else {
            onBluetoothAdapterStartDiscovery()
        }
    }

    override fun startAsLocalService() {
        isDiscovery = false
        if(bluetoothAdapter == null) {
            initiateBluetoothAdapterWithPermissions()
        } else {
            onBluetoothAdapterStartLocalService()
        }
    }

    override fun clearEverything() {
        isDiscovery = false
        if(!wasEnabled) {
            bluetoothAdapter?.disable()
        }
        bluetoothAdapter = null
        serviceAdapter.clearList()
    }
}