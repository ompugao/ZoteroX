package com.mickstarify.zotero.LibraryActivity.ItemView

import android.app.AlertDialog
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mickstarify.zotero.LibraryActivity.Notes.NoteInteractionListener
import com.mickstarify.zotero.LibraryActivity.Notes.NoteView
import com.mickstarify.zotero.LibraryActivity.ViewModels.LibraryListViewModel
import com.mickstarify.zotero.R
import com.mickstarify.zotero.ZoteroAPI.Model.Note

val ARG_NOTE = "note"

class ItemNoteEntry() : Fragment() {
    private var note: Note? = null
    private var listener: NoteInteractionListener? = null
//    lateinit var libraryViewModel: LibraryListViewModel

//    val noteKey: String by lazy { arguments?.getString("noteKey") ?: "" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onAttachToParentFragment(parentFragment)
    }

    fun stripHtml(html: String): String {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString()
        } else {
            return Html.fromHtml(html).toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_item_note_entry, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showNoteItemView(note)
    }

    private fun showNoteItemView(note: Note?) {
        val noteText = requireView().findViewById<TextView>(R.id.textView_note)

        if (note == null) {
            return
        }


        val htmlText = stripHtml(note.note ?: "")
        noteText.text = htmlText
        val noteView = NoteView(requireActivity(), note, listener!!)

        requireView().setOnClickListener {
            noteView.show()
        }

        requireView().setOnLongClickListener(object : View.OnLongClickListener {
            override fun onLongClick(dialog: View?): Boolean {
                AlertDialog.Builder(this@ItemNoteEntry.context)
                    .setTitle(getString(R.string.note))
                    .setItems(
                        arrayOf(getString(R.string.view_note),
                            getString(R.string.edit_note),
                            getString(R.string.delete_note))
                    ) { _, item ->
                        when (item) {
                            0 -> noteView.show()
                            1 -> editNote()
                            2 -> deleteNote()
                        }
                    }.show()
                return true
            }

        })

    }

    private fun editNote() {
        Log.d("zotero", "edit note clicked")
        note?.let {
            listener?.editNote(it)
        }
    }

    private fun deleteNote() {
        Log.d("zotero", "delete note clicked")
        note?.let {
            listener?.deleteNote(it)
        }
    }

    fun onAttachToParentFragment(parentFragment: Fragment?) {
        if (parentFragment == null) {
            return
        }
        if (parentFragment is NoteInteractionListener) {
            listener = parentFragment
        } else {
            throw RuntimeException(parentFragment.toString() + " must implement OnNoteInteractionListener")
        }
    }

    companion object {

        @JvmStatic
        fun newInstance(note: Note): ItemNoteEntry {
            return ItemNoteEntry().apply {
                this.note = note
            }
        }
    }
}
