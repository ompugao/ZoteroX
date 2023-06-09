package com.mickstarify.zotero.LibraryActivity.ItemView

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.chip.Chip
import com.mickstarify.zotero.MyLog
import com.mickstarify.zotero.TagStyler
import com.mickstarify.zotero.ZoteroStorage.Database.Creator
import com.mickstarify.zotero.ZoteroStorage.Database.Item
import com.mickstarify.zotero.ZoteroStorage.Database.ItemTag
import com.mickstarify.zotero.ZoteroStorage.ZoteroUtils
import com.mickstarify.zotero.adapters.TagWrapper
import com.mickstarify.zotero.databinding.FragmentItemBasicInfoBinding
import com.mickstarify.zotero.databinding.LayoutItemBasicInfoEtcBinding
import com.mickstarify.zotero.databinding.LayoutItemBasicInfoJournalPaperBinding

import com.devs.readmoreoption.ReadMoreOption
import com.mickstarify.zotero.R

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

        if (item == null) {
            return
        }

        when (item?.itemType) {
            "journalArticle" -> navigateToThesisInfoPage(item!!)
            else -> showBasicInfo(item!!)
        }

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
//        val itemViewLayout =
//            requireView().findViewById<LinearLayout>(R.id.item_fragment_scrollview_ll_layout)
//
//        val creatorLayout = LinearLayout(requireContext())
//        creatorLayout.orientation = LinearLayout.VERTICAL
//        creatorLayout.layoutParams =
//            LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT
//            )
//
//        itemViewLayout.addView(creatorLayout)

        var authorInfo = ""
        creators.forEachIndexed { index, creator ->

            val lastName = creator.lastName ?: ""
            val firstName = creator.firstName ?: ""

            var authorName = "$lastName$firstName"

            if (creator.creatorType == "author") authorName += "(作者)"

            authorInfo += "$authorName;"
        }

        addTextEntry("作者", authorInfo)
    }

    private fun addAttachments(attachments: List<Item>) {
        val fmt = this.childFragmentManager.beginTransaction()
        for (attachment in attachments) {
            Log.d("zotero", "adding ${attachment.getTitle()}")
            fmt.add(
                R.id.item_fragment_scrollview_ll_attachments,
                ItemAttachmentEntry.newInstance(attachment)
            )
        }
        fmt.commit()
    }

    private fun addTextEntry(label: String, content: String) {
        val layout =
            requireView().findViewById<LinearLayout>(R.id.item_fragment_scrollview_ll_layout)

        val view = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_item_text_entry, layout)
        val viewGroup = view as ViewGroup
        val textLayout = viewGroup.getChildAt(viewGroup.childCount - 1)

        val textLabel = textLayout.findViewById<TextView>(R.id.textView_label)
        textLabel.text = label

        val textViewInfo = textLayout.findViewById<TextView>(R.id.textView_item_info)
        textViewInfo.setText(content)
    }

    private fun showBasicInfo(item: Item) {
        val binding = LayoutItemBasicInfoEtcBinding.inflate(layoutInflater)
        navigateToView(binding.root)

        item?.let { showItemInfo(it) }

        var info = ""
        for (datum in item.data) {
            info += "\n ${datum.key} : ${datum.value}"
        }
        MyLog.d("ZoteroDebug", info)
    }

    private fun navigateToThesisInfoPage(item: Item) {
        val binding = LayoutItemBasicInfoJournalPaperBinding.inflate(layoutInflater)
        binding.txtThesisTitle.text = item.getTitle()

        // OR using options to customize
        val readMoreOption = ReadMoreOption.Builder(requireContext())
            .textLength(8, ReadMoreOption.TYPE_LINE) // OR
            //.textLength(300, ReadMoreOption.TYPE_CHARACTER)
            .moreLabel("展开")
            .lessLabel("折叠")
            .moreLabelColor(Color.RED)
            .lessLabelColor(Color.BLUE)
            .labelUnderLine(true)
            .expandAnimation(true)
            .build()

        readMoreOption.addReadMoreTo(binding.txtAbstract, item.data["abstractNote"])


        binding.txtDate.text = item.data["date"]
        binding.txtJournalName.text = item.data["publicationTitle"]

        // show creators of this paper
        var authorsInfo = ""
        item.creators.forEach {
            authorsInfo += it.lastName + it.firstName + ";"
        }
        binding.txtAuthors.text = authorsInfo.dropLast(1)

        var info = ""
        for (datum in item.data) {
            info += "\n ${datum.key} : ${datum.value}"
        }
//        MyLog.d("ZoteroDebug", info)

        // show extra info
        val extraInfo =  "修改日期：${item.data["dateModified"]} \n创建日期：${item.data["dateAdded"]}"
        binding.txtExtraInfo.text = extraInfo

        // show information about this journal paper
        val journalInfo = "卷次：${item.data["volume"]} \n期号：${item.data["issue"]} \n页码：${item.data["pages"]}" +
                "\n语言：${item.data["language"]} \nISSN：${item.data["ISSN"]}" +
                "\nDOI：${item.data["DOI"]} \nUrl：${item.data["url"]}"
        binding.txtPaperInfo.text = journalInfo

        val arrangedTags = sortTags(item.tags)
        arrangedTags.forEach {
            val chip: Chip = layoutInflater.inflate(R.layout.tag_chip, null, false).findViewById(R.id.chip) as Chip
            chip.text = it.tag

            if (!it.color.isNullOrEmpty()) {
                chip.setTextColor(Color.parseColor(it.color))
            }
            binding.tagsContainer.addView(chip)
        }

        navigateToView(binding.root)
    }

    private fun sortTags(tags: List<ItemTag>): List<TagWrapper> {
        val tagStyler = TagStyler.getInstance(requireContext())

        return tags.sortedWith { p0, p1 -> tagStyler.indexOf(p1!!.tag) - tagStyler.indexOf(p0!!.tag) }
            .map { TagWrapper(it.tag, tagStyler.getTagColor(it.tag), false) }
    }

    private fun navigateToView(view: View) {
        mBinding.content.removeAllViews()
        mBinding.content.addView(view)
    }

    companion object {
        @JvmStatic
        fun newInstance(item: Item) =
            ItemBasicInfoFragment().apply {
                this.item = item
            }
    }
}