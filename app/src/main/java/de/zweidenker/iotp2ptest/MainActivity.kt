package de.zweidenker.iotp2ptest

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import de.zweidenker.iotp2ptest.util.BaseActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setTitle(R.string.app_name)

        button_wifi.setOnClickListener {
            withVersion(Build.VERSION_CODES.JELLY_BEAN) {
                if(packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT)) {
                    startActivity(Intent(this, WifiActivity::class.java))
                } else {
                    toast("This phone does not support Wifi Direct.")
                }
            }
        }

        button_bluetooth.setOnClickListener {
            if(packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
                startActivity(Intent(this, BluetoothActivity::class.java))
            } else {
                toast("This phone does not support Bluetooth.")
            }
        }

        button_bluetooth_le.setOnClickListener {
            withVersion(Build.VERSION_CODES.JELLY_BEAN_MR2) {
                if(packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                    startActivity(Intent(this, BluetoothLEActivity::class.java))
                } else {
                    toast("This phone does not support Bluetooth Low Energy.")
                }
            }
        }
    }
}
