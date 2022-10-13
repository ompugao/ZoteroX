package com.mickstarify.zotero.ZoteroAPI.Model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TagPOJO(
    @SerializedName("tag")
    val tag: String
) : Parcelable
