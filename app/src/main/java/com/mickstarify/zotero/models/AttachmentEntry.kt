package com.mickstarify.zotero.models

import com.mickstarify.zotero.ZoteroStorage.Database.Item

data class AttachmentEntry(val name: String,
                           val fileName: String,
                           val itemKey: String,
                           val attachment: Item,
                           val attachmentType: String?) {

    var meta: AttachmentMeta? = null

    data class AttachmentMeta(val fileName: String,
                              val type: String?,
                              val exists: Boolean,
                              val size: Long)
}