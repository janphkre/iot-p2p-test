package de.zweidenker.iotp2ptest

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setTitle(R.string.app_name)

        button_wifi.setOnClickListener {
            startActivity(Intent(this, WifiActivity::class.java))
        }

        button_bluetooth.setOnClickListener {
            startActivity(Intent(this, BluetoothActivity::class.java))
        }
    }
}
