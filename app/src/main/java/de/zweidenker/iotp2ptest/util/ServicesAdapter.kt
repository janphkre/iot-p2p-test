package de.zweidenker.iotp2ptest.util

import android.app.Activity
import android.support.v4.util.ArrayMap
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import de.zweidenker.iotp2ptest.R
import java.lang.ref.WeakReference

class ServicesAdapter(activity: Activity): RecyclerView.Adapter<ServicesAdapter.ServiceViewHolder>() {

    private val attachedActivity: WeakReference<Activity> = WeakReference(activity)
    private val itemMap =  ArrayMap<String, ServiceData>()

    private fun runOnUiThread(action: () -> Unit) {
        attachedActivity.get()?.runOnUiThread(action)
    }

    fun put(name: String, type: String, status: Int) {
        val currentData = itemMap[name]
        if(currentData != null) {
            currentData.apply {
                this.type = type
                this.deviceStatus = status
            }
            runOnUiThread {
                notifyItemChanged(currentData.position)
            }
        } else {
            synchronized(itemMap) {
                val position = itemMap.size
                itemMap[name] = ServiceData(
                    position,
                    name,
                    type,
                    status
                )
                runOnUiThread {
                    notifyItemInserted(position)
                }
            }

        }
    }

    fun put(name: String, type: String, rssi: Int, scanRecord: String) {
        val currentData = itemMap[name]
        if(currentData != null) {
            currentData.apply {
                this.type = type
                this.deviceStatus = rssi
                this.txtRecordMap = mapOf(Pair("", scanRecord))
            }
            runOnUiThread {
                notifyItemChanged(currentData.position)
            }
        } else {
            synchronized(itemMap) {
                val position = itemMap.size
                itemMap[name] = ServiceData(
                    position,
                    name,
                    type,
                    rssi,
                    mapOf(Pair("", scanRecord))
                )
                runOnUiThread {
                    notifyItemInserted(position)
                }
            }

        }
    }

    fun put(name: String, txtRecordMap: Map<String, String>, status: Int) {
        val currentData = itemMap[name]
        if(currentData != null) {
            currentData.apply {
                this.deviceStatus = status
                this.txtRecordMap = txtRecordMap
            }
            runOnUiThread {
                notifyItemChanged(currentData.position)
            }
        } else {
            synchronized(itemMap) {
                val position = itemMap.size
                itemMap[name] = ServiceData(
                    position,
                    name,
                    "",
                    status,
                    txtRecordMap
                )
                runOnUiThread {
                    notifyItemInserted(position)
                }
            }

        }
    }

    fun clearList() {
        synchronized(itemMap) {
            itemMap.clear()
            runOnUiThread {
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        return ServiceViewHolder(parent)
    }

    override fun getItemCount(): Int {
        return itemMap.size
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val item = itemMap.valueAt(position)
        holder.bindData(item)
    }

    data class ServiceData(val position: Int, val name: String, var type: String, var deviceStatus: Int, var txtRecordMap: Map<String, String>? = null)

    class ServiceViewHolder(parent: ViewGroup): RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_service, parent, false)) {
        private val nameView = itemView.findViewById<TextView>(R.id.text_name)
        private val typeView = itemView.findViewById<TextView>(R.id.text_type)
        private val deviceView = itemView.findViewById<TextView>(R.id.text_device)
        private val recordView = itemView.findViewById<ListView>(R.id.text_record)

        fun bindData(data: ServiceData) {
            nameView.text = data.name
            typeView.text = data.type
            deviceView.text = data.deviceStatus.toString()
            data.txtRecordMap?.apply {
                this.entries
                recordView.adapter = object: ArrayAdapter<Map.Entry<String, String>>(recordView.context, 0, this.entries.toList()) {
                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val currentView = convertView ?: LayoutInflater.from(parent.context).inflate(R.layout.item_record, parent, false)
                        val item = getItem(position)
                        (currentView as TextView).text = "${item?.key} = ${item?.value}"
                        return currentView
                    }
                }
            }
        }
    }
}