package com.mickstarify.zotero.ZoteroAPI

data class ZoteroAPIAuthorizationRequestResponse(
    val exists: Boolean,
    val url: String,
    val contentType: String,
    val prefix: String,
    val suffix: String,
    val uploadKey: String
)