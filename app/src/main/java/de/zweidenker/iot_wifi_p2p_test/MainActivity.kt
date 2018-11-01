package de.zweidenker.iot_wifi_p2p_test

import android.content.Context
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
import android.os.Build
import android.os.Bundle
import android.os.HandlerThread
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import permissions.dispatcher.PermissionUtils

class MainActivity : AppCompatActivity() {

    companion object {
        private val SERVICE_NAME = "TestService"
        private val SERVICE_TYPE = "pharo_connectivity"
    }

    private var backgroundThread: HandlerThread? = null
    private var wifiManager: WifiP2pManager? = null
    private var wifiChannel: WifiP2pManager.Channel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button_local_service.setOnClickListener {
            startAsLocalService()
        }
        button_discover_services.setOnClickListener {
            startAsDiscovery()
        }
        initiateWifiManagerWithPermissions()
    }

    private fun initiateWifiManagerWithPermissions() {
        withPermissions("Manifest.permission.ACCESS_WIFI_STATE", "Manifest.permission.CHANGE_WIFI_STATE", callback= ::initiateWifiManager)
    }

    private fun initiateWifiManager() {
        wifiManager = getSystemService(Context.WIFI_P2P_SERVICE) as? WifiP2pManager
        if(wifiManager == null) {
            toast("Failed to load WifiP2pManager")
        }
        backgroundThread = HandlerThread("WifiHandlerThread").apply {
            if (!isAlive) {
                start()
            }
            wifiChannel = wifiManager?.initialize(this@MainActivity, looper) {
                toast("Channel Disconnected!")
                wifiManager = null
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == 5001) {
            if(PermissionUtils.verifyPermissions(*grantResults)) {
                initiateWifiManager()
            } else {
                toast("Required permissions were not granted!")
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onDestroy() {
        wifiChannel?.apply {
            wifiManager?.clearLocalServices(wifiChannel, null)
            wifiManager?.clearServiceRequests(wifiChannel, null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                close()
            }
        }
        wifiChannel = null
        wifiManager = null
        backgroundThread?.quit()
        backgroundThread = null
        super.onDestroy()
    }

    private fun withPermissions(vararg permissions: String, callback: () -> Unit) {
        if(PermissionUtils.hasSelfPermissions(this, *permissions)) {
            callback.invoke()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissions, 5001)
            } else {
               toast("Required permissions were not granted!")
            }
        }
    }

    private fun startAsLocalService() {
        if(wifiChannel == null) {
            initiateWifiManager()
        }
        val serviceInfo = WifiP2pDnsSdServiceInfo.newInstance(SERVICE_NAME, SERVICE_TYPE, mapOf(Pair("mac","00:00:00:00:00:00:00:00")))
        wifiManager?.addLocalService(wifiChannel, serviceInfo, object: WifiP2pManager.ActionListener {
            override fun onSuccess() {
                toast("Successfully created local service!")
                runOnUiThread {
                    service_list.text = "Local Service $SERVICE_NAME"
                }
            }

            override fun onFailure(p0: Int) {
                toast("Failed to create local service!")
            }

        })
    }

    private fun startAsDiscovery() {
        if(wifiChannel == null) {
            initiateWifiManager()
        }
        val request = WifiP2pDnsSdServiceRequest.newInstance(SERVICE_TYPE)
        wifiManager?.setDnsSdResponseListeners(wifiChannel, { name, type, wifiP2pDevice ->
            runOnUiThread { service_list.text = "${service_list.text}\n$name: $type" }
        }, { s, mutableMap, wifiP2pDevice ->
            //ignore for now
        })
        wifiManager?.addServiceRequest(wifiChannel, request, object: WifiP2pManager.ActionListener {
            override fun onSuccess() {
                toast("Successfully created service request!")
            }

            override fun onFailure(p0: Int) {
                toast("Failed to create service request!")
            }

        })
    }

    private fun toast(string: String) {
        Toast.makeText(this, string, Toast.LENGTH_LONG).show()
    }
}
