package de.zweidenker.iotp2ptest

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import de.zweidenker.iotp2ptest.util.ServicesActivity
import java.util.*

/**
 * This is an Activity to demo the behaviour of NFC.
 *
 * See: https://developer.android.com/guide/topics/connectivity/nfc/
 */
class NFCActivity: ServicesActivity(R.string.test_nfc) {

    private var nfcAdapter: NfcAdapter? = null
    private var isDiscovery = false

    private fun initiateNfcAdapterWithPermissions() {
        withPermissions(Manifest.permission.NFC, callback= ::initiateNfcAdapter)
    }

    private fun initiateNfcAdapter() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this).apply {
            if(this == null) {
                toast("Device does not support nfc!")
                return
            }
        }
        if(isDiscovery) {
            onNfcAdapterStartDiscovery()
        } else {
            onNfcAdapterStartLocalService()
        }
    }

    private fun onNfcAdapterStartDiscovery() {
        val intent = Intent(this, javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        val ndefIntentFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            try {
                addDataType("*/*")
            } catch (e: IntentFilter.MalformedMimeTypeException) {
                throw RuntimeException("fail", e)
            }
        }
        val intentFiltersArray = arrayOf(ndefIntentFilter)
        val techListsArray = arrayOf(arrayOf<String>(Ndef::class.java.name))
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray)
    }

    private fun onNfcAdapterStartLocalService() {
        val uuid = UUID.randomUUID()
        val payload = NdefRecord(NdefRecord.TNF_UNKNOWN, null, uuid.toString().toByteArray(), byteArrayOf(1))
        val message = NdefMessage(arrayOf(NdefRecord.createApplicationRecord("de.zweidenker.iotp2ptest"), payload))
        nfcAdapter?.setNdefPushMessage(message, this)
        nfcAdapter?.setOnNdefPushCompleteCallback(NfcAdapter.OnNdefPushCompleteCallback {
            toast("Successfully pushed message over nfc!")
        }, this)
        serviceAdapter.put("de.zweidenker.iotp2ptest",uuid.toString(),NdefRecord.TNF_UNKNOWN.toInt())
    }

    override fun onNewIntent(intent: Intent?) {
        val tagFromIntent: Tag? = intent?.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        if(tagFromIntent != null) {
            serviceAdapter.put("de.zweidenker.iotp2ptest",String(tagFromIntent.id), NdefRecord.TNF_UNKNOWN.toInt())
        }
    }

    override fun startAsLocalService() {
        isDiscovery = false
        initiateNfcAdapterWithPermissions()
    }

    override fun startAsDiscovery() {
        isDiscovery = true
        initiateNfcAdapterWithPermissions()
    }

    override fun clearEverything() {
        if(isDiscovery) {
            nfcAdapter?.disableForegroundDispatch(this)
        } else {
            nfcAdapter?.setNdefPushMessage(null, this)
            nfcAdapter?.setOnNdefPushCompleteCallback(null, this)
        }
        serviceAdapter.clearList()
    }

    override fun onDestroy() {
        clearEverything()
        nfcAdapter = null
        super.onDestroy()
    }
}