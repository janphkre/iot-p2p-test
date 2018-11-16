package de.zweidenker.iotp2ptest

import android.os.Build
import android.support.annotation.RequiresApi

/**
 * This is a Activity to demo the behavior of Bluetooth Low Energy.
 * Bluetooth LE requires API level 18, some permissions and a bluetooth module in the phone.
 * See: https://developer.android.com/guide/topics/connectivity/bluetooth-le
 */
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class BluetoothLEActivity: BaseBluetoothActivity(R.string.test_bluetooth_le) {

    override fun onBluetoothManagerInstantiated() {
        TODO("not implemented")
    }

    override fun onDestroy() {
        //TODO!
        super.onDestroy()
    }
}