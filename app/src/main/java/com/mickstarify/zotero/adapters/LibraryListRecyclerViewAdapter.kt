package com.mickstarify.zotero.adapters

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.mickstarify.zotero.LibraryActivity.LibraryActivity
import com.mickstarify.zotero.LibraryActivity.LibraryActivityModel
import com.mickstarify.zotero.LibraryActivity.ListEntry
import com.mickstarify.zotero.MyLog
import com.mickstarify.zotero.R
import com.mickstarify.zotero.ZoteroStorage.Database.Collection
import com.mickstarify.zotero.ZoteroStorage.Database.GroupInfo
import com.mickstarify.zotero.ZoteroStorage.Database.Item
import com.mickstarify.zotero.ZoteroStorage.ZoteroUtils
import kotlinx.coroutines.*

class LibraryListRecyclerViewAdapter(val context: Context,
    val listener: LibraryListInteractionListener
) : RecyclerView.Adapter<LibraryListRecyclerViewAdapter.ListEntryViewHolder>() {

//    val items = ArrayList<ListEntry>()

    val diffCallback = object: ItemCallback<ListEntry>() {

        override fun areItemsTheSame(oldItem: ListEntry, newItem: ListEntry): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ListEntry, newItem: ListEntry): Boolean {
            return oldItem.equals(newItem)
        }
    }

    val mDiffer = AsyncListDiffer<ListEntry>(this, diffCallback)

    var model: LibraryActivityModel
    init {
        model = ViewModelProvider(context as LibraryActivity).get(LibraryActivityModel::class.java)
    }

    class ListEntryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView = view.findViewById<ImageView>(R.id.imgEntryIcon)
        val textView_title = view.findViewById<TextView>(R.id.txtEntryTitle)
        val txtDescription = view.findViewById<TextView>(R.id.txtEntryAuthor)
        val pdfImage = view.findViewById<ImageButton>(R.id.imgOpenAttachment)
        val layout = view.findViewById<ConstraintLayout>(R.id.container_item_library_entry)

        val tagContainer = view.findViewById<ChipGroup>(R.id.tagsContainer)

    }

    // ItemView long click Listener
    var onItemLongClickListener: LibraryItemLongClickListener? = null

    lateinit var layoutInflater: LayoutInflater

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ListEntryViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_library_list, parent, false)

        layoutInflater = LayoutInflater.from(parent.context)

        return ListEntryViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ListEntryViewHolder,
        position: Int
    ) {

        val entry = mDiffer.currentList[position]
        if (entry.isItem()) {
            val item = entry.getItem()
            holder.textView_title.text = item.getTitle()
            holder.txtDescription.visibility = View.VISIBLE
            holder.txtDescription.text = item.getAuthor().trimEnd()
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
                this.onItemLongClickListener?.onItemLongClick(item)
                false
            }

            // Use itemType to get the target icon resource.
            val iconResource = requireItemIconRes(item.itemType)
            holder.imageView.setImageResource(iconResource)

            // 在recyclerview中使用addView添加控件需要先移除所用，不然会产生布局错乱
            holder.tagContainer.removeAllViews()
            // add tags
            item.tags.forEach {
                if (ZoteroUtils.isImportantTag(it.tag)) {
                    val chip: Chip = layoutInflater.inflate(R.layout.tag_chip, null, false).findViewById(R.id.chip) as Chip
                    chip.text = it.tag
                    chip.setTextColor(Color.parseColor(ZoteroUtils.getTagColor(it.tag)))

                    holder.tagContainer.addView(chip)
                }
            }

        } else {
            val collection = entry.getCollection()
            holder.textView_title.text = collection.name

            val childCollectionCount = collection.getSubCollections().size
            val items = model.getItemsFromCollection(collection.key)

            holder.txtDescription.text = "${childCollectionCount + items.size} 条子项"

            holder.pdfImage.visibility = View.GONE
            holder.imageView.setImageResource(R.drawable.treesource_folder)
            holder.layout.setOnClickListener {
                Log.d("zotero", "Open Collection")
                listener.onCollectionOpen(collection)
            }

            // 实测需要移除所用，不然会因为复用问题产生布局错乱
            holder.tagContainer.removeAllViews()
            holder.tagContainer.visibility = View.GONE

            holder.layout.setOnLongClickListener {
                this.onItemLongClickListener?.onCollectionLongClick(collection)
                false
            }
        }
    }

    fun setItemLongClick(onLongClick:LibraryItemLongClickListener) {
        this.onItemLongClickListener = onLongClick
    }

    fun setData(entries: List<ListEntry>) {
        mDiffer.submitList(entries)
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
        return mDiffer.currentList.size
    }


}

interface LibraryListInteractionListener {
    fun onItemOpen(item: Item)
    fun onCollectionOpen(collection: Collection)
    fun onItemAttachmentOpen(item: Item)
}

interface LibraryItemLongClickListener {
    fun onItemLongClick(item: Item)
    fun onCollectionLongClick(collection: Collection)
}

