package de.zweidenker.iotp2ptest.util

import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import permissions.dispatcher.PermissionUtils

abstract class BaseActivity: AppCompatActivity() {

    private var permissionCallback: (() -> Unit)? = null

    protected fun toast(string: String) {
        Toast.makeText(this, string, Toast.LENGTH_LONG).show()
    }

    protected fun withVersion(versionCode: Int, callback: () -> Unit) {
        if(Build.VERSION.SDK_INT >= versionCode) {
            callback.invoke()
        } else {
            toast("This phone is too old to use this feature (API ${Build.VERSION.SDK_INT})")
        }
    }

    protected fun withPermissions(vararg permissions: String, callback: () -> Unit) {
        if(PermissionUtils.hasSelfPermissions(this, *permissions)) {
            callback.invoke()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                permissionCallback = callback
                requestPermissions(permissions, ServicesActivity.RequestCode.PERMISSIONS.value)
            } else {
                toast("Required permissions were not granted!")
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == ServicesActivity.RequestCode.PERMISSIONS.value) {
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
}