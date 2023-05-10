package com.mickstarify.zotero.models

import com.mickstarify.zotero.ZoteroStorage.Database.Item

data class AttachmentEntry(val name: String,
                           val itemKey: String,
                           val attachment: Item,
                           val attachmentType: String?) {

}