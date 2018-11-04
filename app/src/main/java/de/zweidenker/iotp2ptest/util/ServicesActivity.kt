package de.zweidenker.iotp2ptest.util

import android.os.Build
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import de.zweidenker.iotp2ptest.R
import kotlinx.android.synthetic.main.activity_services.*
import permissions.dispatcher.PermissionUtils

abstract class ServicesActivity(@StringRes private val titleRes: Int): AppCompatActivity() {

    private var permissionCallback: (() -> Unit)? = null
    protected val serviceAdapter = ServicesAdapter()

    enum class RequestCode(val value: Int) {
        PERMISSIONS(5001), ENABLE_BT(5002)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_services)
        setTitle(titleRes)

        service_list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        service_list.adapter = serviceAdapter

        button_local_service.setOnClickListener {
            clearEverything()
            startAsLocalService()
        }
        button_discover_services.setOnClickListener {
            clearEverything()
            startAsDiscovery()
        }
    }

    protected fun toast(string: String) {
        Toast.makeText(this, string, Toast.LENGTH_LONG).show()
    }

    protected fun withPermissions(vararg permissions: String, callback: () -> Unit) {
        if(PermissionUtils.hasSelfPermissions(this, *permissions)) {
            callback.invoke()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                permissionCallback = callback
                requestPermissions(permissions, RequestCode.PERMISSIONS.value)
            } else {
                toast("Required permissions were not granted!")
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == RequestCode.PERMISSIONS.value) {
            if(PermissionUtils.verifyPermissions(*grantResults)) {
                permissionCallback?.invoke()
                permissionCallback = null
            } else {
                toast("Required permissions were not granted!")
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    abstract fun startAsLocalService()
    abstract fun startAsDiscovery()
    abstract fun clearEverything()
}