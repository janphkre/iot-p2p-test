package de.zweidenker.iotp2ptest.util

import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v7.widget.LinearLayoutManager
import de.zweidenker.iotp2ptest.R
import kotlinx.android.synthetic.main.activity_services.*

abstract class ServicesActivity(@StringRes private val titleRes: Int): BaseActivity() {

    protected val serviceAdapter = ServicesAdapter(this)

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

    abstract fun startAsLocalService()
    abstract fun startAsDiscovery()
    abstract fun clearEverything()
}