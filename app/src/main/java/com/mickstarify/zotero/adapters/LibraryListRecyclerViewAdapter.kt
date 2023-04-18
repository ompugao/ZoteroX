package com.mickstarify.zotero.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mickstarify.zotero.LibraryActivity.ListEntry
import com.mickstarify.zotero.R
import com.mickstarify.zotero.ZoteroStorage.Database.Collection
import com.mickstarify.zotero.ZoteroStorage.Database.Item

class LibraryListRecyclerViewAdapter(val context: Context,
    var items: List<ListEntry>,
    val listener: LibraryListInteractionListener
) : RecyclerView.Adapter<LibraryListRecyclerViewAdapter.ListEntryViewHolder>() {
    class ListEntryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView = view.findViewById<ImageView>(R.id.imgEntryIcon)
        val textView_title = view.findViewById<TextView>(R.id.txtEntryTitle)
        val textView_author = view.findViewById<TextView>(R.id.txtEntryAuthor)
        val pdfImage = view.findViewById<ImageButton>(R.id.imgOpenAttachment)
        val layout = view.findViewById<ConstraintLayout>(R.id.container_item_library_entry)
    }

    // ItemView long click Listener
    var onItemLongClickListener: LibraryItemLongClickListener? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ListEntryViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.library_screen_list_item, parent, false)

        return ListEntryViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ListEntryViewHolder,
        position: Int
    ) {

        val entry = items[position]
        if (entry.isItem()) {
            val item = entry.getItem()
            holder.textView_title.text = item.getTitle()
            holder.textView_author.visibility = View.VISIBLE
            holder.textView_author.text = item.getAuthor()
            val pdfAttachment = item.getPdfAttachment()
            if (pdfAttachment != null) {
                holder.pdfImage.visibility = View.VISIBLE
                holder.pdfImage.setOnClickListener {
                    Log.d("zotero", "Open Attachment ${item.getTitle()}")
                    listener.onItemAttachmentOpen(pdfAttachment)
                }
            } else {
                holder.pdfImage.visibility = View.GONE
            }
            holder.layout.setOnClickListener {
                Log.d("zotero", "Open Item")
//                listener.onItemOpen(item)

                //原来是点击打开item信息，修改后变成打开pdf附件
                val pdfFile = item.getPdfAttachment()
                if (pdfFile != null) listener.onItemAttachmentOpen(pdfFile)
            }

            holder.layout.setOnLongClickListener {
                this.onItemLongClickListener?.onItemClick(item)
                false
            }

            // Use itemType to get the target icon resource.
            val iconResource = requireItemIconRes(item.itemType)
            holder.imageView.setImageResource(iconResource)

//            Glide.with(context)
//                .load(iconResource)
////                .placeholder(iconResource)
////                .error(R.drawable.ic_item_known)
//                .crossFade()//或者使用 dontAnimate() 关闭动画
//                .into(holder.imageView)

        } else {
            val collection = entry.getCollection()
            holder.textView_title.text = collection.name
            holder.textView_author.visibility = View.GONE
            holder.pdfImage.visibility = View.GONE
            holder.imageView.setImageResource(R.drawable.treesource_folder)
            holder.layout.setOnClickListener {
                Log.d("zotero", "Open Collection")
                listener.onCollectionOpen(collection)
            }

//            Glide.with(context)
//                .load(R.drawable.treesource_folder)
//                .crossFade()//或者使用 dontAnimate() 关闭动画
//                .into(holder.imageView);
        }
    }

    fun setItemLongClick(onLongClick:LibraryItemLongClickListener) {
        this.onItemLongClickListener = onLongClick
    }

    /**
     * Use itemType to get target icon res, which is built-in
     */
    private fun requireItemIconRes(itemType: String): Int {
        val iconResource = when (itemType) {
            "note" -> {
                R.drawable.ic_item_note
            }
            "book" -> {
                R.drawable.ic_book
            }
            "bookSection" -> {
                R.drawable.ic_book_section
            }
            "journalArticle" -> {
                R.drawable.journal_article
            }
            "magazineArticle" -> {
                R.drawable.magazine_article_24dp
            }
            "newspaperArticle" -> {
                R.drawable.newspaper_article_24dp
            }
            "thesis" -> {
                R.drawable.ic_thesis
            }
            "letter" -> {
                R.drawable.letter_24dp
            }
            "manuscript" -> {
                R.drawable.manuscript_24dp
            }
            "interview" -> {
                R.drawable.interview_24dp
            }
            "film" -> {
                R.drawable.film_24dp
            }
            "artwork" -> {
                R.drawable.artwork_24dp
            }
            "webpage" -> {
                R.drawable.ic_web_page
            }
            "attachment" -> {
                R.drawable.ic_treeitem_attachment
            }
            "report" -> {
                R.drawable.report_24dp
            }
            "bill" -> {
                R.drawable.bill_24dp
            }
            "case" -> {
                R.drawable.case_24dp
            }
            "hearing" -> {
                R.drawable.hearing_24dp
            }
            "patent" -> {
                R.drawable.patent_24dp
            }
            "statute" -> {
                R.drawable.statute_24dp
            }
            "email" -> {
                R.drawable.email_24dp
            }
            "map" -> {
                R.drawable.map_24dp
            }
            "blogPost" -> {
                R.drawable.blog_post_24dp
            }
            "instantMessage" -> {
                R.drawable.instant_message_24dp
            }
            "forumPost" -> {
                R.drawable.forum_post_24dp
            }
            "audioRecording" -> {
                R.drawable.audio_recording_24dp
            }
            "presentation" -> {
                R.drawable.presentation_24dp
            }
            "videoRecording" -> {
                R.drawable.video_recording_24dp
            }
            "tvBroadcast" -> {
                R.drawable.tv_broadcast_24dp
            }
            "radioBroadcast" -> {
                R.drawable.radio_broadcast_24dp
            }
            "podcast" -> {
                R.drawable.podcast_24dp
            }
            "computerProgram" -> {
                R.drawable.computer_program_24dp
            }
            "conferencePaper" -> {
                R.drawable.ic_conference_paper
            }
            "document" -> {
                R.drawable.ic_document
            }
            "encyclopediaArticle" -> {
                R.drawable.encyclopedia_article_24dp
            }
            "dictionaryEntry" -> {
                R.drawable.dictionary_entry_24dp
            }
            else -> {
                R.drawable.ic_item_known
            }
        }

        return iconResource
    }

    override fun getItemCount(): Int {
        return items.size
    }
}

interface LibraryListInteractionListener {
    fun onItemOpen(item: Item)
    fun onCollectionOpen(collection: Collection)
    fun onItemAttachmentOpen(item: Item)
}

interface LibraryItemLongClickListener {
    fun onItemClick(item: Item)
}