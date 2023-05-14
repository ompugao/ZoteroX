package com.mickstarify.zotero.adapters

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.mickstarify.zotero.LibraryActivity.ListEntry
import com.mickstarify.zotero.R
import com.mickstarify.zotero.ZoteroStorage.Database.Item

class AnimatedLibraryListRecyclerViewAdapter : BaseQuickAdapter<ListEntry, BaseViewHolder> {

    var items: MutableList<ListEntry>
    var listener: LibraryListInteractionListener

    constructor(items: MutableList<ListEntry>, listener: LibraryListInteractionListener) : super(R.layout.item_library_list) {
        this.items = items
        this.listener = listener
    }


//    class ListEntryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val imageView = view.findViewById<ImageView>(R.id.imageView_library_list_image)
//        val textView_title = view.findViewById<TextView>(R.id.TextView_library_list_title)
//        val textView_author = view.findViewById<TextView>(R.id.TextView_library_list_author)
//        val pdfImage = view.findViewById<ImageButton>(R.id.imageButton_library_list_attachment)
//        val layout = view.findViewById<ConstraintLayout>(R.id.constraintLayout_library_list_item)
//    }

//    override fun onCreateViewHolder(
//        parent: ViewGroup,
//        viewType: Int
//    ): ListEntryViewHolder {
//        // Create a new view, which defines the UI of the list item
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.library_screen_list_item, parent, false)
//
//        return ListEntryViewHolder(view)
//    }

//    override fun onBindViewHolder(
//        holder: ListEntryViewHolder,
//        position: Int
//    ) {
//
//        val entry = items[position]
//        if (entry.isItem()) {
//            val item = entry.getItem()
//            holder.textView_title.text = item.getTitle()
//            holder.textView_author.visibility = View.VISIBLE
//            holder.textView_author.text = item.getAuthor()
//            val pdfAttachment = item.getPdfAttachment()
//            if (pdfAttachment != null) {
//                holder.pdfImage.visibility = View.VISIBLE
//                holder.pdfImage.setOnClickListener {
//                    Log.d("zotero", "Open Attachment ${item.getTitle()}")
//                    listener.onItemAttachmentOpen(pdfAttachment)
//                }
//            } else {
//                holder.pdfImage.visibility = View.GONE
//            }
//            holder.layout.setOnClickListener {
//                Log.d("zotero", "Open Item")
//                listener.onItemOpen(item)
//            }
//
//            holder.imageView.setImageResource(getItemIcon(item))
//
////            holder.imageView.setImageResource(iconResource)
//        } else {
//            val collection = entry.getCollection()
//            holder.textView_title.text = collection.name
//            holder.textView_author.visibility = View.GONE
//            holder.pdfImage.visibility = View.GONE
//            holder.imageView.setImageResource(R.drawable.treesource_folder)
//            holder.layout.setOnClickListener {
//                Log.d("zotero", "Open Collection")
//                listener.onCollectionOpen(collection)
//            }
//        }
//    }

    private fun getItemIcon(item: Item): Int {
        val iconResource = when (item.itemType) {
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

//    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
//        DataBindingUtil.bind<LibraryScreenListItemBinding>(viewHolder.itemView)
//    }

    override fun convert(holder: BaseViewHolder, entry: ListEntry) {
//        val imageView = holder.getView<ImageView>(R.id.imageView_library_list_image)
//        val textView_title = holder.getView<TextView>(R.id.txtEntryTitle)
//        val textView_author = holder.getView<TextView>(R.id.txtEntryAuthor)
//        val pdfImage = holder.getView<ImageButton>(R.id.imgOpenAttachment)
//        val layout = holder.getView<ConstraintLayout>(R.id.constraintLayout_library_list_item)

//        val entry = items[position]

//        val binding = holder.getBinding<LibraryScreenListItemBinding>()
//        if (entry.isItem()) {
//            val item = entry.getItem()
//            binding!!.txtEntryTitle.text = item.getTitle()
//            binding.txtEntryAuthor.visibility = View.VISIBLE
//            binding.txtEntryAuthor.text = item.getAuthor()
//            val pdfAttachment = item.getPdfAttachment()
//            if (pdfAttachment != null) {
//                binding.imgOpenAttachment.visibility = View.VISIBLE
//                binding.imgOpenAttachment.setOnClickListener {
//                    Log.d("zotero", "Open Attachment ${item.getTitle()}")
//                    listener.onItemAttachmentOpen(pdfAttachment)
//                }
//            } else {
//                binding.imgOpenAttachment.visibility = View.GONE
//            }
//            binding.containerItemLibraryEntry.setOnClickListener {
//                Log.d("zotero", "Open Item")
//                listener.onItemOpen(item)
//            }
//
//            binding.imgEntryIcon.setImageResource(getItemIcon(item))
//
////            holder.imageView.setImageResource(iconResource)
//        } else {
//            val collection = entry.getCollection()
//            binding!!.txtEntryTitle.text = collection.name
//            binding.txtEntryAuthor.visibility = View.GONE
//            binding!!.imgOpenAttachment.visibility = View.GONE
//            binding.imgEntryIcon.setImageResource(R.drawable.treesource_folder)
//            binding.containerItemLibraryEntry.setOnClickListener {
//                Log.d("zotero", "Open Collection")
//                listener.onCollectionOpen(collection)
//            }
//        }
    }
}

//interface LibraryListInteractionListener {
//    fun onItemOpen(item: Item)
//    fun onCollectionOpen(collection: Collection)
//    fun onItemAttachmentOpen(item: Item)
//}