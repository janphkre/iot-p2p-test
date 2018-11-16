package de.zweidenker.iotp2ptest

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.os.Build
import android.support.annotation.RequiresApi

/**
 * This is a Activity to demo the behavior of Bluetooth Low Energy.
 * Bluetooth LE requires API level 18, some permissions and a bluetooth module in the phone.
 * See: https://developer.android.com/guide/topics/connectivity/bluetooth-le
 * The startLeScan and stopLeScan calls on the BluetoothAdapter have been deprecated in API 21 and replaced with android.bluetooth.le.BluetoothLeScanner
 */
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class BluetoothLEActivity: BaseBluetoothActivity(R.string.test_bluetooth_le), BluetoothAdapter.LeScanCallback  {

    private var isScanning = false

    override fun onBluetoothAdapterInitiated() {
        isScanning = true
        bluetoothAdapter?.startLeScan(this)
    }

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
        serviceAdapter.put(device.name, type, rssi, scanRecord.toString())
    }

    override fun clearEverything() {
        isScanning = false
        bluetoothAdapter?.stopLeScan(this)
        super.clearEverything()
    }

    override fun onDestroy() {
        isScanning = false
        bluetoothAdapter?.stopLeScan(this)
        super.onDestroy()
    }
}