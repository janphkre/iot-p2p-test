package de.zweidenker.iotp2ptest

import de.zweidenker.iotp2ptest.util.ServicesActivity

/**
 * This is a Activity to demo the behavior of Bluetooth Low Energy.
 * Bluetooth LE requires API level 18, some permissions and a bluetooth module in the phone.
 * See: https://developer.android.com/guide/topics/connectivity/bluetooth-le
 */
class BluetoothLEActivity: ServicesActivity(R.string.test_bluetooth_le) {

    override fun startAsLocalService() {
        TODO("not implemented")
    }

    override fun startAsDiscovery() {
        TODO("not implemented")
    }

    override fun clearEverything() {
        TODO("not implemented")
    }

    override fun onDestroy() {
        clearEverything()
        super.onDestroy()
    }
}