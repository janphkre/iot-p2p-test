package de.zweidenker.iotp2ptest

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.os.Build
import android.support.annotation.RequiresApi

/**
 * This is an Activity to demo the behavior of Bluetooth Low Energy.
 * Bluetooth LE requires API level 18, some permissions and a bluetooth module in the phone.
 * See: https://developer.android.com/guide/topics/connectivity/bluetooth-le
 * The startLeScan and stopLeScan calls on the BluetoothAdapter have been deprecated in API 21 and replaced with android.bluetooth.le.BluetoothLeScanner
 */
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class BluetoothLEActivity: BaseBluetoothActivity(R.string.test_bluetooth_le) {

    private var isScanning = false
    private var callbacks: Any? = null

    override fun onBluetoothAdapterStartDiscovery() {
        if(!isScanning) {
            isScanning = true
            callbacks = IoTScanCallback().also {
                bluetoothAdapter?.startLeScan(it)
            }
        }
    }

    @SuppressLint("NewApi")
    override fun onBluetoothAdapterStartLocalService() {
        withVersion(Build.VERSION_CODES.LOLLIPOP) {
            if (!isScanning) {
                isScanning = true
                val settings = AdvertiseSettings.Builder()
                    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                    .setConnectable(false)
                    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                    .build()
                val data = AdvertiseData.Builder()
                    .setIncludeDeviceName(true)
                    .setIncludeTxPowerLevel(true)
                    .build()
                bluetoothAdapter?.bluetoothLeAdvertiser?.apply {
                    callbacks = IoTAdvertiseCallback().also {
                        startAdvertising(settings, data, it)
                    }
                }
            }
        }
    }

    @SuppressLint("NewApi")
    override fun clearEverything() {
        isScanning = false
        (callbacks as? IoTScanCallback)?.let {
            bluetoothAdapter?.stopLeScan(it)
        }
        withVersion(Build.VERSION_CODES.LOLLIPOP) {
            (callbacks as? IoTAdvertiseCallback)?.let {
                bluetoothAdapter?.bluetoothLeAdvertiser?.apply {
                    stopAdvertising(it)
                }
            }
        }
        callbacks = null
        super.clearEverything()
    }

    private inner class IoTScanCallback: BluetoothAdapter.LeScanCallback {
        override fun onLeScan(device: BluetoothDevice, rssi: Int, scanRecord: ByteArray?) {
            val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                device.type.toString()
            } else {
                "SDK-VER"
            }
            val scanRecordString = if(scanRecord != null) {
                String(scanRecord)
            } else {
                ""
            }
            serviceAdapter.put(device.name, type, rssi, scanRecordString)
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private inner class IoTAdvertiseCallback: AdvertiseCallback() {
        override fun onStartFailure(errorCode: Int) {
            toast("Failed to create advertising! ($errorCode)")
        }

        @SuppressLint("HardwareIds")
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            toast("Successfully created advertising!")
            serviceAdapter.put(bluetoothAdapter?.name ?: bluetoothAdapter?.address ?: "", "", settingsInEffect.txPowerLevel, bluetoothAdapter?.address ?: "")
        }
    }
}