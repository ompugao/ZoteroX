package com.mickstarify.zotero.ZoteroAPI.Model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CreatorPOJO(
    val creatorType: String,
    val firstName: String,
    val lastName: String
) : Parcelable {

    fun makeString(): String {
        return "${firstName} ${lastName}"
    }
}