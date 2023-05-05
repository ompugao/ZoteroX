package com.mickstarify.zotero.LibraryActivity.ItemView

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mickstarify.zotero.LibraryActivity.Notes.NoteInteractionListener
import com.mickstarify.zotero.LibraryActivity.ViewModels.LibraryListViewModel
import com.mickstarify.zotero.R
import com.mickstarify.zotero.ZoteroAPI.Model.Note
import com.mickstarify.zotero.ZoteroStorage.Database.Item
import com.mickstarify.zotero.ZoteroStorage.ZoteroDB.ZoteroDB
import com.mickstarify.zotero.databinding.FragmentItemNotesBinding
import com.mickstarify.zotero.databinding.FragmentItemTagsBinding

class ItemNotesFragment : Fragment(), NoteInteractionListener {

    private lateinit var mBinding: FragmentItemNotesBinding

    private var item: Item? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentItemNotesBinding.inflate(inflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        item?.let {  populateNotes(it.notes) }
    }

    private fun populateNotes(notes: List<Note>) {
        if (notes.isEmpty()) {
            mBinding.txtNoneNotes.visibility = View.VISIBLE
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

    companion object {
        @JvmStatic
        fun newInstance(item: Item) =
            ItemNotesFragment().apply {
                this.item = item
            }
    }

    override fun deleteNote(note: Note) {
    }

    override fun editNote(note: Note) {
    }


}