package com.mickstarify.zotero.LibraryActivity.ItemView

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.chip.Chip
import com.mickstarify.zotero.LibraryActivity.ViewModels.LibraryListViewModel
import com.mickstarify.zotero.R
import com.mickstarify.zotero.ZoteroStorage.Database.Creator
import com.mickstarify.zotero.ZoteroStorage.Database.Item
import com.mickstarify.zotero.ZoteroStorage.ZoteroUtils
import com.mickstarify.zotero.databinding.FragmentItemBasicInfoBinding

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ItemBasicInfoFragment(val viewModel: LibraryListViewModel) : Fragment() {

    private lateinit var attachments: List<Item>

    private var param1: String? = null
    private var param2: String? = null

    private lateinit var mBinding: FragmentItemBasicInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mBinding = FragmentItemBasicInfoBinding.inflate(inflater)
        return mBinding.root

//        return inflater.inflate(R.layout.fragment_item_basic_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getOnItemClicked().observe(viewLifecycleOwner) { item ->
            attachments = item.attachments

            if (item.creators.isNotEmpty()) {
                this.addCreators(item.getSortedCreators())
            } else {
                // empty creator.
                this.addCreators(listOf(Creator("null", "", "", "", -1)))
            }
            for ((key, value) in item.data) {
                if (value != "" && key != "itemType" && key != "title") {

                    // 将key转换为实际所代表的真实信息
                    var keyString = ZoteroUtils.getItemKeyNameHumanReadableString(key)

                    addTextEntry(keyString, value)
                }
            }
            this.addAttachments(attachments)
        }

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

            creatorType.text = creator.creatorType + ":"

            val lastName = creator.lastName ?: ""
            val firstName = creator.firstName ?: ""

            txtAuthorName.text = "$lastName'$firstName"

//            edtLastName.setText(creator.lastName ?: "")
//            edtFirstName.setText(creator.firstName ?: "")
        }
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

        val textViewInfo = textLayout.findViewById<TextView>(R.id.textView_item_info)
        textViewInfo.setText(content)
    }



    companion object {
        @JvmStatic
        fun newInstance(viewModel: LibraryListViewModel, param1: String, param2: String) =
            ItemBasicInfoFragment(viewModel).apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}