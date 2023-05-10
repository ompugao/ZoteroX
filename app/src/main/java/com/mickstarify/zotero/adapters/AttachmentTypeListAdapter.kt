package com.mickstarify.zotero.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.mickstarify.zotero.AttachmentManager.AttachmentManagerModel
import com.mickstarify.zotero.MyLog
import com.mickstarify.zotero.R
import com.mickstarify.zotero.ZoteroStorage.AttachmentStorageManager
import com.mickstarify.zotero.ZoteroStorage.Database.Item
import com.mickstarify.zotero.models.AttachmentEntry

class AttachmentTypeListAdapter(context: Context, private val attachmentManager: AttachmentStorageManager) :
    BaseQuickAdapter<AttachmentEntry, AttachmentTypeListAdapter.AttachmentHolder>(R.layout.item_attachment_info) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentHolder {
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun convert(holder: AttachmentHolder, item: AttachmentEntry) {
        holder.txtTitle.text = item.name

//        CoroutineScope(Dispatchers.Default).launch {
//            getAttachmentFileMeta(item.attachmentItem)?.let {
//
//                withContext(Dispatchers.Main) {
//                    if (it.exists) {
////                        FileUtils
//                        holder.txtDescription.text = "${it.size / 1048576} MB"
//                    }
//                    holder.setVisible(R.id.img_download_state, !it.exists)
//                }
//            }
//        }
    }

    override fun onBindViewHolder(holder: AttachmentHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val item = data[position]

        holder.rootView.setOnClickListener {
            MyLog.d("ZoteroDebug", "Open attachment: ${item.name}")

            if (attachmentManager.checkIfAttachmentExists(item.attachment, false)) {
                val intent = attachmentManager.openLinkedAttachment(item.attachment)
                context.startActivity(intent)

            }
        }

    }

    fun getAttachmentFileMeta(it: Item): AttachmentManagerModel.FilesystemMetadataObject? {
        if (it.itemType != "attachment") {
            MyLog.e("zotero", "${it.itemKey} is not an attachment")
            return null
        }

        val fileName = attachmentManager.getFilenameForItem(it)

//        var uri: Uri? = null
//         try {
//             uri = attachmentStorageManager.getAttachmentUri(it)
//         } catch (e: Exception) {
//
//         }

        if (!it.isDownloadable() || it.data["linkMode"] == "linked_file") {
            return AttachmentManagerModel.FilesystemMetadataObject("", null, false, -1)
        } else {
            val exists = attachmentManager.checkIfAttachmentExists(it, false)
            val size = if (exists) {
                attachmentManager.getFileSize(it)
            } else {
                -1
            }
            return AttachmentManagerModel.FilesystemMetadataObject(fileName, null, exists, size.toLong())
        }
    }

    class AttachmentHolder(view: View) : BaseViewHolder(view) {
        val rootView = view
        val txtTitle =  view.findViewById<TextView>(R.id.txt_title)
        val txtDescription =  view.findViewById<TextView>(R.id.txt_descpription)
        val btnDownload =  view.findViewById<ImageButton>(R.id.img_download_state)

    }




}