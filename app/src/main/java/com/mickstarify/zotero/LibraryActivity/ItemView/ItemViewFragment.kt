package com.mickstarify.zotero.LibraryActivity.ItemView

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.mickstarify.zotero.LibraryActivity.Notes.EditNoteDialog
import com.mickstarify.zotero.LibraryActivity.Notes.NoteInteractionListener
import com.mickstarify.zotero.LibraryActivity.Notes.onEditNoteChangeListener
import com.mickstarify.zotero.LibraryActivity.ViewModels.LibraryListViewModel
import com.mickstarify.zotero.R
import com.mickstarify.zotero.ZoteroAPI.Model.Note
import com.mickstarify.zotero.ZoteroStorage.Database.Creator
import com.mickstarify.zotero.ZoteroStorage.Database.Item
import java.util.*

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [ItemViewFragment.OnListFragmentInteractionListener] interface.
 */
class ItemViewFragment : BottomSheetDialogFragment(),
    NoteInteractionListener,
    onShareItemListener,
    ItemTagEntry.OnTagEntryInteractionListener {

    lateinit var libraryViewModel: LibraryListViewModel

    lateinit var chipTags: ChipGroup

    private lateinit var attachments: List<Item>
    private lateinit var notes: List<Note>
    private var listener: OnItemFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private fun showShareItemDialog() {
        val item = libraryViewModel.getOnItemClicked().value
        if (item == null) {
            Toast.makeText(requireContext(), "Item not loaded yet.", Toast.LENGTH_SHORT).show()
        } else {
            ShareItemDialog(item).show(context, this)
        }
    }

    override fun editNote(note: Note) {
        EditNoteDialog()
            .show(context, note.note, object :
                onEditNoteChangeListener {
                override fun onCancel() {
                }

                override fun onSubmit(noteText: String) {
                    note.note = noteText
                    listener?.onNoteEdit(note)
                }
            })
    }

    override fun deleteNote(note: Note) {
        listener?.onNoteDelete(note)
    }

    /**
     * 显示创建笔记对话框
     */
    private fun showCreateNoteDialog() {
        EditNoteDialog()
            .show(context, "", object :
                onEditNoteChangeListener {
                override fun onCancel() {
                }

                override fun onSubmit(noteText: String) {
                    Log.d("zotero", "got note $noteText")
                    val item = libraryViewModel.getOnItemClicked().value
                    if (item == null) {
                        Toast.makeText(
                            requireContext(),
                            "Error, item unloaded from memory, please backup text and reopen app.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                    val note = Note(noteText, item.itemKey)
                    listener?.onNoteCreate(note)
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_item_main, container, false)
        return view
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        libraryViewModel =
            ViewModelProvider(requireActivity()).get(LibraryListViewModel::class.java)

        if (libraryViewModel.getOnItemClicked().value == null) {
            Log.e("zotero", "error item in viewmodel is null!")
            dismiss()
        }

        val txtItemType = requireView().findViewById<TextView>(R.id.txt_item_type)

        chipTags = requireView().findViewById<ChipGroup>(R.id.Chips_item_tags)

        val closeButton = requireView().findViewById<ImageButton>(R.id.btn_close)
        val moreButton = requireView().findViewById<ImageButton>(R.id.btn_more)

        val addNotesButton = requireView().findViewById<ImageButton>(R.id.btn_add_note)
        val textViewTitle = requireView().findViewById<TextView>(R.id.textView_item_toolbar_title)

        libraryViewModel.getOnItemClicked().observe(viewLifecycleOwner) { item ->
            attachments = item.attachments
            notes = item.notes

            txtItemType.text = item.itemType ?: "Unknown"

//            addTextEntry("Item Type", item.data["itemType"] ?: "Unknown")
//            addTextEntry("title", item.getTitle())

            if (item.creators.isNotEmpty()) {
                this.addCreators(item.getSortedCreators())
            } else {
                // empty creator.
                this.addCreators(listOf(Creator("null", "", "", "", -1)))
            }
            for ((key, value) in item.data) {
                if (value != "" && key != "itemType" && key != "title") {
                    addTextEntry(key, value)
                }
            }
            this.addAttachments(attachments)
            this.populateNotes(notes)
            this.populateTags(item.tags.map { it.tag })

            addNotesButton.setOnClickListener {
                showCreateNoteDialog()
            }

            closeButton.setOnClickListener {
                this.dismiss()
            }

            moreButton.setOnClickListener {
                val popupMenu = PopupMenu(context, moreButton)
                popupMenu.inflate(R.menu.fragment_library_item_actionbar)

                popupMenu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.add_note -> showCreateNoteDialog()
                        R.id.share_item -> showShareItemDialog()
                    }
                    false
                }
                popupMenu.show()
            }


            textViewTitle.text = item.getTitle()
        }
    }

    private fun populateTags(tags: List<String>) {
        val fmt = this.childFragmentManager.beginTransaction()
        for (tag in tags) {
            fmt.add(
                R.id.item_fragment_scrollview_ll_tags,
                ItemTagEntry.newInstance(tag)
            )
            val chip = Chip(context)
            chip.text = tag

            chip.setOnClickListener {

            }

            chipTags.addView(chip)
        }
        fmt.commit()
    }

    private fun populateNotes(notes: List<Note>) {
        if (notes.isEmpty()) {
            return
        }
        val fmt = this.childFragmentManager.beginTransaction()
        for (note in notes) {
            fmt.add(
                R.id.item_fragment_scrollview_ll_notes,
                ItemNoteEntry.newInstance(note.key)
            )
        }
        fmt.commit()
    }

    private fun addAttachments(attachments: List<Item>) {
        val fmt = this.childFragmentManager.beginTransaction()
        for (attachment in attachments) {
            Log.d("zotero", "adding ${attachment.getTitle()}")
            fmt.add(
                R.id.item_fragment_scrollview_ll_attachments,
                ItemAttachmentEntry.newInstance(attachment.itemKey)
            )
        }
        fmt.commit()
    }

    private fun addTextEntry(label: String, content: String) {
        val layout =
            requireView().findViewById<LinearLayout>(R.id.item_fragment_scrollview_ll_layout)

        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.fragment_item_text_entry, layout)
        val viewGroup = view as ViewGroup
        val textLayout = viewGroup.getChildAt(viewGroup.childCount - 1)

        val textLabel = textLayout.findViewById<TextView>(R.id.textView_label)
        textLabel.text = label

//        val editText = textLayout.findViewById<TextInputEditText>(R.id.editText_itemInfo)
//        editText.setText(content)
        val textViewInfo = textLayout.findViewById<TextView>(R.id.textView_item_info)
        textViewInfo.setText(content)
    }

    private fun addCreators(creators: List<Creator>) {
        val itemViewLayout =
            requireView().findViewById<LinearLayout>(R.id.item_fragment_scrollview_ll_layout)

        val inflator = LayoutInflater.from(requireContext())

        val creatorLayout = LinearLayout(requireContext())
        creatorLayout.orientation = LinearLayout.VERTICAL
        creatorLayout.layoutParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        itemViewLayout.addView(creatorLayout)

        creators.forEachIndexed { index, creator ->
            val parent = inflator.inflate(R.layout.fragment_item_authors_entry, creatorLayout)
            val view = (parent as ViewGroup).getChildAt(index)

            val creatorType = view.findViewById<TextView>(R.id.textView_creator_type)

            val txtAuthorName = view.findViewById<TextView>(R.id.txt_author)

//        0

            creatorType.text = creator.creatorType + ":"

            val lastName = creator.lastName ?: ""
            val firstName = creator.firstName ?: ""

            txtAuthorName.text = "$lastName'$firstName"

//            edtLastName.setText(creator.lastName ?: "")
//            edtFirstName.setText(creator.firstName ?: "")
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnItemFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnItemFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson
     * [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnItemFragmentInteractionListener {
        fun onListFragmentInteraction(item: Item?)
        fun onNoteCreate(note: Note)
        fun onNoteEdit(note: Note)
        fun onNoteDelete(note: Note)
        fun shareText(shareText: String)
        fun onTagOpenListener(tag: String)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ItemViewFragment()
    }

    override fun shareItem(shareText: String) {
        listener?.shareText(shareText)
    }

    override fun tagPressed(tag: String) {
        listener?.onTagOpenListener(tag)
        this.dismiss()
    }
}
