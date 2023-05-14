package com.mickstarify.zotero.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.imageview.ShapeableImageView
import com.mickstarify.zotero.AttachmentManager.AttachmentDownloadListener
import com.mickstarify.zotero.MyLog
import com.mickstarify.zotero.R
import com.mickstarify.zotero.ZoteroStorage.AttachmentStorageManager
import com.mickstarify.zotero.ZoteroStorage.Database.Item
import com.mickstarify.zotero.models.AttachmentEntry
import kotlinx.coroutines.*

class AttachmentListAdapter(val context: Context, private val attachmentManager: AttachmentStorageManager): Adapter<ViewHolder>() {

    private val attachments = mutableListOf<AttachmentEntry>()

    var listener: AttachInteractionListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rootView = LayoutInflater.from(parent.context).inflate(R.layout.item_attachment_info, parent, false)

        return AttachmentHolder(rootView)
    }

    override fun getItemCount(): Int {
        return attachments.size
    }

    override fun onBindViewHolder(p1: ViewHolder, position: Int) {
        val itemEntry = attachments[position]

        val item = itemEntry.attachment

        val holder = p1 as AttachmentHolder

        holder.txtTitle.text = item.getTitle()

        if (itemEntry.meta == null) {
            CoroutineScope(Dispatchers.Default).launch {
                val meta = async {
                    val isExist = attachmentManager.simpleCheckIfAttachmentExists(item)

                    var size = 0L
                    try {
                        size = attachmentManager.getFileSize(item)
                    } catch (e: Exception) {}
                    val itemType = attachmentManager.getAttachmentType(item)

                    return@async AttachmentEntry.AttachmentMeta("", itemType, isExist, size)
                }

                itemEntry.meta = meta.await()

                withContext(Dispatchers.Main) {
                    updateItemUI(holder, itemEntry.meta)
                }
            }
        } else {
            updateItemUI(holder, itemEntry.meta)
        }

        holder.rootView.setOnClickListener {
            listener?.onAttachmentOpen(item)
        }

        holder.btnDownload.setOnClickListener {

            holder.btnDownload.visibility = View.GONE
            holder.pgDownload.visibility = View.VISIBLE

            listener?.onAttachmentDownload(item, object : AttachmentDownloadListener {
                override fun onSuccess(item: Item) {
                    holder.btnDownload.visibility = View.GONE
                    holder.pgDownload.visibility = View.GONE
                }

                override fun onError(item: Item, error: String) {
                    holder.btnDownload.visibility = View.VISIBLE
                    holder.pgDownload.visibility = View.GONE
                }

                override fun onProgress(progress: Long, total: Long) {
                    MyLog.d("ZoteroDebug", "下载附件: ${item.getTitle()}， 进度：${progress}")
//                    holder.pgDownload.progress =
                }
            })
        }

    }

    private fun updateItemUI(holder:AttachmentHolder, meta: AttachmentEntry.AttachmentMeta?) {
        meta?.let {
            if (it.exists) {
                holder.btnDownload.visibility = View.GONE

                holder.txtDescription.text = "${it.size / 1024} KB"
            } else {
                holder.btnDownload.visibility = View.VISIBLE

                holder.txtDescription.text = ""
            }

            getDrawableResId(it.type).let {
                holder.imgIcon.setImageDrawable(context.getDrawable(it))
            }

        }
    }

    private fun getDrawableResId(itemType: String?): Int {
        return when (itemType) {
            "application/pdf" -> R.drawable.ic_pdf_outline
            "text/html" -> R.drawable.ic_webpage_outline
            else -> R.drawable.ic_file_unknown
        }
    }

    fun setData(data: List<AttachmentEntry>) {
        this.attachments.clear()
        this.attachments.addAll(data)
    }

    class AttachmentHolder(view: View) : ViewHolder(view) {
        val rootView = view
        val txtTitle =  view.findViewById<TextView>(R.id.txt_title)
        val txtDescription =  view.findViewById<TextView>(R.id.txt_descpription)
        val btnDownload =  view.findViewById<ImageButton>(R.id.img_download_state)

        val imgIcon =  view.findViewById<ShapeableImageView>(R.id.img_entry_icon)
        val pgDownload =  view.findViewById<ProgressBar>(R.id.progress_download)

//        val lvContainer =  view.findViewById<View>(R.id.container_item_library_entry)

    }

    interface AttachInteractionListener {
        fun onAttachmentDownload(item: Item, downloadListener: AttachmentDownloadListener): Unit
        fun onAttachmentOpen(item: Item): Unit
    }

}