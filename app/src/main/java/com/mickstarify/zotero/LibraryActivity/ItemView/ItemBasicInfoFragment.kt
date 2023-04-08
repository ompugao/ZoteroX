package com.mickstarify.zotero.LibraryActivity.ItemView

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.mickstarify.zotero.R
import com.mickstarify.zotero.ZoteroApplication
import com.mickstarify.zotero.ZoteroStorage.Database.Creator
import com.mickstarify.zotero.ZoteroStorage.Database.Item
import com.mickstarify.zotero.ZoteroStorage.ZoteroDB.ZoteroDB
import com.mickstarify.zotero.ZoteroStorage.ZoteroUtils
import com.mickstarify.zotero.databinding.FragmentItemBasicInfoBinding

class ItemBasicInfoFragment : Fragment() {

    private lateinit var attachments: List<Item>

    private lateinit var mBinding: FragmentItemBasicInfoBinding

    private var item: Item? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mBinding = FragmentItemBasicInfoBinding.inflate(inflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        item?.let { showItemInfo(it) }
    }

    private fun showItemInfo(item: Item) {

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
        fun newInstance(item: Item) =
            ItemBasicInfoFragment().apply {
                this.item = item
            }
    }
}