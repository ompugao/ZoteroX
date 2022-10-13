package com.mickstarify.zotero.ZoteroAPI

interface DeleteItemListener {
    fun success()
    fun failedItemLocked()
    fun failedItemChangedSince()
    fun failed(code: Int)
}
