package com.example.strokepatientsvoicerecoveryapp

import android.icu.text.CaseMap.Title
import android.media.Image
import android.os.Parcel
import android.os.Parcelable

data class Dataclass(var dataImage: Int, var dataTitle: String):Parcelable {

    var onItemClick: ((String) -> Unit)? = null

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(dataImage)
        parcel.writeString(dataTitle)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Dataclass> {
        override fun createFromParcel(parcel: Parcel): Dataclass {
            return Dataclass(parcel)
        }

        override fun newArray(size: Int): Array<Dataclass?> {
            return arrayOfNulls(size)
        }
    }
}
