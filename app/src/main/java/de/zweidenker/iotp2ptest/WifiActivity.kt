package de.zweidenker.iotp2ptest

import android.Manifest
import android.content.Context
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
import android.os.Build
import android.os.Bundle
import android.os.HandlerThread
import de.zweidenker.iotp2ptest.util.ServicesActivity

class WifiActivity: ServicesActivity(R.string.test_wifi) {
    companion object {
        private const val SERVICE_NAME = "TestService"
        private const val SERVICE_TYPE = "pharo_connectivity"
    }

    private var backgroundThread: HandlerThread? = null
    private var wifiManager: WifiP2pManager? = null
    private var wifiChannel: WifiP2pManager.Channel? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initiateWifiManagerWithPermissions()
    }

    private fun initiateWifiManagerWithPermissions() {
        withPermissions(Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.INTERNET, callback= ::initiateWifiManager)
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
            wifiChannel = wifiManager?.initialize(this@WifiActivity, looper) {
                toast("Channel Disconnected!")
                wifiManager = null
            }
        }
    }

    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            wifiChannel?.close()
        }
        wifiChannel = null
        wifiManager = null
        backgroundThread?.quit()
        backgroundThread = null
        clearEverything()
        super.onDestroy()
    }

    override fun startAsLocalService() {
        if(wifiChannel == null) {
            initiateWifiManagerWithPermissions()
        }
        val serviceInfo = WifiP2pDnsSdServiceInfo.newInstance(SERVICE_NAME, SERVICE_TYPE, mapOf(Pair("mac","00:00:00:00:00:00:00:00")))
        wifiManager?.addLocalService(wifiChannel, serviceInfo, object: WifiP2pManager.ActionListener {
            override fun onSuccess() {
                toast("Successfully created local service!")
                serviceAdapter.put(SERVICE_NAME, SERVICE_TYPE, -1)
            }

            override fun onFailure(p0: Int) {
                toast("Failed to create local service!")
            }

        })
    }

    override fun startAsDiscovery() {
        if(wifiChannel == null) {
            initiateWifiManagerWithPermissions()
        }
        val request = WifiP2pDnsSdServiceRequest.newInstance(SERVICE_TYPE)
        wifiManager?.setDnsSdResponseListeners(wifiChannel, { fullDomainName, type, wifiP2pDevice ->
            serviceAdapter.put(fullDomainName, type, wifiP2pDevice.status)
        }, { fullDomainName, txtRecordMap, wifiP2pDevice ->
            serviceAdapter.put(fullDomainName, txtRecordMap, wifiP2pDevice.status)
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

    override fun clearEverything() {
        wifiChannel?.apply {
            wifiManager?.clearLocalServices(wifiChannel, null)
            wifiManager?.clearServiceRequests(wifiChannel, null)
        }
        serviceAdapter.clearList()
    }
}