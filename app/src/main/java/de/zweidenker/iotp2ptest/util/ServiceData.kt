package de.zweidenker.iotp2ptest.util

import android.os.Parcel
import android.os.Parcelable

data class ServiceData(
    val position: Int,
    val name: String,
    var type: String,
    var deviceStatus: Int,
    var address: String,
    var txtRecordMap: Map<String, String>? = null) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString()!!,
        hashMapOf()) {
        parcel.readMap(txtRecordMap, Map::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(position)
        parcel.writeString(name)
        parcel.writeString(type)
        parcel.writeString(address)
        parcel.writeInt(deviceStatus)
        parcel.writeMap(txtRecordMap)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ServiceData> {
        override fun createFromParcel(parcel: Parcel): ServiceData {
            return ServiceData(parcel)
        }

        override fun newArray(size: Int): Array<ServiceData?> {
            return arrayOfNulls(size)
        }
    }

}