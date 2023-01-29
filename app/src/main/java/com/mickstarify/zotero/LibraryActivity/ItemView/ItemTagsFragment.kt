package com.mickstarify.zotero.LibraryActivity.ItemView

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.chip.Chip
import com.mickstarify.zotero.LibraryActivity.ViewModels.LibraryListViewModel
import com.mickstarify.zotero.R
import com.mickstarify.zotero.databinding.FragmentItemTagsBinding

class ItemTagsFragment(libraryListViewModel: LibraryListViewModel) : Fragment() {
    private var viewModel: LibraryListViewModel = libraryListViewModel

    private lateinit var mBinding: FragmentItemTagsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentItemTagsBinding.inflate(inflater)
        viewModel.getOnItemClicked().observe(viewLifecycleOwner) { item ->
            populateTags(item.tags.map { it.tag })
        }
        return mBinding.root
    }

    /**
     * 显示item的标签
     */
    private fun populateTags(tags: List<String>) {
        if (tags.isNullOrEmpty()) {
            mBinding.txtNoneTags.visibility = View.VISIBLE
            return
        }

        for (tag in tags) {
            val chip = Chip(context)
            chip.text = tag

            chip.setOnClickListener {
                Toast.makeText(context, "代码待写！！！", Toast.LENGTH_SHORT).show()
            }

            mBinding.ChipsItemTags.addView(chip)
        }
    }


}