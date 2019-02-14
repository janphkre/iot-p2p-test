package de.zweidenker.iotp2ptest

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import de.zweidenker.iotp2ptest.util.ServicesActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
class WifiActivity: ServicesActivity(R.string.test_wifi) {
    companion object {
        private const val SERVICE_NAME = "ConfigurationService"
        private const val SERVICE_TYPE = "connectivity.pharo._tcp"
    }

    private var wifiManager: WifiP2pManager? = null
    private var wifiChannel: WifiP2pManager.Channel? = null
    private var broadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if(intent == null) return
            when (intent.action) {
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    val networkInfo: NetworkInfo? = intent
                        .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO) as NetworkInfo

                    if (networkInfo?.isConnected == true) {

                        // We are connected with the other device, request connection
                        // info to find group owner IP

                        wifiManager?.requestConnectionInfo(wifiChannel) {
                            if(!it.groupFormed || it.isGroupOwner) {
                                //TODO: RETRY IN A FEW?
                                toast("Got wifi info: ${it.groupFormed}, ${it.isGroupOwner}", false)
                                return@requestConnectionInfo
                            }
                            toast("Successfully got wifi info! ${it.groupOwnerAddress}")
                            Dispatchers.IO.dispatch(GlobalScope.coroutineContext, Runnable {
                                Socket().use { socket ->
                                    try {
                                        socket.bind(null)
                                        socket.connect(InetSocketAddress(it.groupOwnerAddress, 8889), 90000)
                                        if (socket.isConnected) {
                                            Dispatchers.Main.dispatch(GlobalScope.coroutineContext, Runnable {
                                                toast("Successfully created socket connection!")
                                            })
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            })
                        }
                    }

                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerReceiver(broadcastReceiver, IntentFilter(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION))
        serviceAdapter.setOnItemClickListener { item ->
            val config = WifiP2pConfig()
            config.deviceAddress = item.address
            config.groupOwnerIntent = 0
            toast("Trying to connect p2p!")
            wifiManager?.connect(wifiChannel, config, object: WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    toast("Successfully connected p2p!")
                }

                override fun onFailure(reason: Int) {
                    toast("Failed to connect! $reason")
                }

            })
        }
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
            wifiChannel = wifiManager?.initialize(this@WifiActivity, mainLooper) {
                toast("Channel Disconnected!")
                wifiManager = null
            }
    }

    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            wifiChannel?.close()
        }
        wifiChannel = null
        wifiManager = null
        clearEverything()
        unregisterReceiver(broadcastReceiver)
        super.onDestroy()
    }

    override fun startAsLocalService() {
        if(wifiChannel == null) {
            initiateWifiManagerWithPermissions()
            return
        }
        val serviceInfoMap = mapOf(Pair("identifier", "Android"), Pair("status","up"),Pair("port","8889"))
        val serviceInfo = WifiP2pDnsSdServiceInfo.newInstance(SERVICE_NAME, SERVICE_TYPE, serviceInfoMap)
        wifiManager?.addLocalService(wifiChannel, serviceInfo, object: WifiP2pManager.ActionListener {
            override fun onSuccess() {
                toast("Successfully created local service!")
                serviceAdapter.put(SERVICE_NAME, SERVICE_TYPE, -1, "")
                serviceAdapter.put(SERVICE_NAME, serviceInfoMap, -1, "")
            }

            override fun onFailure(p0: Int) {
                toast("Failed to create local service! $p0")
            }
        })

        wifiManager?.requestConnectionInfo(wifiChannel) {
            if(!it.groupFormed || !it.isGroupOwner) return@requestConnectionInfo
            toast("Successfully got wifi info!${it.groupFormed}")
            Dispatchers.IO.dispatch(GlobalScope.coroutineContext, Runnable {
                ServerSocket(8889).use { socket ->
                    socket.accept().use { clientSocket ->
                        if (clientSocket.isConnected) {
                            Dispatchers.Main.dispatch(GlobalScope.coroutineContext, Runnable {
                                toast("Successfully created socket connection!")
                            })
                        }
                    }
                }
            })
        }
        startAsDiscovery()
    }



    override fun startAsDiscovery() {
        if(wifiChannel == null) {
            initiateWifiManagerWithPermissions()
            return
        }
        val request = WifiP2pDnsSdServiceRequest.newInstance()
        wifiManager?.addServiceRequest(wifiChannel, request, object: WifiP2pManager.ActionListener {
            override fun onSuccess() {
                toast("Successfully created service request!", false)
            }

            override fun onFailure(p0: Int) {
                toast("Failed to create service request! $p0")
            }
        })
        wifiManager?.setDnsSdResponseListeners(wifiChannel, { fullDomainName, type, wifiP2pDevice ->
            serviceAdapter.put(fullDomainName, type, wifiP2pDevice.status, wifiP2pDevice.deviceAddress)
        }, { fullDomainName, txtRecordMap, wifiP2pDevice ->
            serviceAdapter.put(fullDomainName, txtRecordMap, wifiP2pDevice.status, wifiP2pDevice.deviceAddress)
        })
        wifiManager?.discoverServices(wifiChannel, object: WifiP2pManager.ActionListener {
            override fun onSuccess() {
                toast("Successfully initiated discovery of services!", false)
            }

            override fun onFailure(p0: Int) {
                toast("Failed to initiate discovery of services! $p0")
            }
        })
    }

    override fun clearEverything() {
        wifiChannel?.apply {
            wifiManager?.clearLocalServices(wifiChannel, null)
            wifiManager?.clearServiceRequests(wifiChannel, null)
            wifiManager?.cancelConnect(wifiChannel, null)
        }
        serviceAdapter.clearList()
    }
}