package com.mickstarify.zotero.LibraryActivity.ItemView

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.view.ContextThemeWrapper
import com.google.android.material.chip.Chip
import com.mickstarify.zotero.LibraryActivity.Notes.NoteInteractionListener
import com.mickstarify.zotero.LibraryActivity.ViewModels.LibraryListViewModel
import com.mickstarify.zotero.R
import com.mickstarify.zotero.ZoteroAPI.Model.Note
import com.mickstarify.zotero.ZoteroStorage.Database.Item
import com.mickstarify.zotero.ZoteroStorage.Database.ItemTag
import com.mickstarify.zotero.ZoteroStorage.ZoteroDB.ZoteroDB
import com.mickstarify.zotero.ZoteroStorage.ZoteroUtils
import com.mickstarify.zotero.databinding.FragmentItemTagsBinding
import java.text.Collator
import java.util.*
import kotlin.Comparator

class ItemTagsFragment : Fragment() {

    private var item: Item? = null

    private lateinit var mBinding: FragmentItemTagsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentItemTagsBinding.inflate(inflater)

        item?.apply {
            populateTags(this.tags)
        }
        return mBinding.root
    }

    /**
     * 显示item的标签
     */
    private fun populateTags(tags: List<ItemTag>) {
        if (tags.isNullOrEmpty()) {
            mBinding.txtNoneTags.visibility = View.VISIBLE
            return
        }

        // 对标签列表先进行排序，然后显示
        val regulatedTags = ZoteroUtils.getImportantTag().reversed()

        val arrangedTags = tags
            .sortedWith { o1, o2 ->
                Collator.getInstance(Locale.CHINA).compare(o1?.tag, o2?.tag)
            }
            .sortedWith(object : Comparator<ItemTag> {
                override fun compare(p0: ItemTag?, p1: ItemTag?): Int {
                    return regulatedTags.indexOf(p1!!.tag) - regulatedTags.indexOf(p0!!.tag)
                }
            })

        for (tag in arrangedTags) {
            val chip = Chip(ContextThemeWrapper(requireContext(), R.style.TagAppearance))
            chip.chipBackgroundColor = ColorStateList.valueOf(Color.parseColor("#F1F2FA"))
            chip.text = tag.tag

            chip.setTextColor(Color.parseColor(ZoteroUtils.getTagColor(tag.tag)))

            chip.setOnClickListener {
                Toast.makeText(context, "代码待写！！！", Toast.LENGTH_SHORT).show()
            }

            mBinding.ChipsItemTags.addView(chip)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(item: Item) =
            ItemTagsFragment().apply {
                this.item = item
            }
    }

}