package com.mickstarify.zotero.AttachmentManager

import com.mickstarify.zotero.ZoteroStorage.Database.Item

interface AttachmentDownloadListener {
    fun onSuccess(item: Item)
    fun onError(item: Item, error: String)
    fun onProgress(progress: Long, total: Long)
}
