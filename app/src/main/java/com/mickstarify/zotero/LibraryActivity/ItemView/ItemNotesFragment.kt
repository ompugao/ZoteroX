package com.mickstarify.zotero.LibraryActivity.ItemView

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mickstarify.zotero.LibraryActivity.ViewModels.LibraryListViewModel
import com.mickstarify.zotero.R
import com.mickstarify.zotero.ZoteroAPI.Model.Note
import com.mickstarify.zotero.databinding.FragmentItemNotesBinding
import com.mickstarify.zotero.databinding.FragmentItemTagsBinding

class ItemNotesFragment(libraryListViewModel: LibraryListViewModel) : Fragment() {
    private var libraryListViewModel: LibraryListViewModel = libraryListViewModel

    private lateinit var mBinding: FragmentItemNotesBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentItemNotesBinding.inflate(inflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        libraryListViewModel.getOnItemClicked().observe(viewLifecycleOwner) { item ->
            populateNotes(item.notes)
        }
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
}