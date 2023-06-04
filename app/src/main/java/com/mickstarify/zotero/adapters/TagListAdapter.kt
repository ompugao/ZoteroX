package com.mickstarify.zotero.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.mickstarify.zotero.R
import com.mickstarify.zotero.TagStyler

data class TagWrapper(var tag: String, var color: String, var isFiltered: Boolean) {

    companion object {
        @JvmStatic
        fun convert(tag: TagStyler.StyledTagEntry): TagWrapper {
            return TagWrapper(tag.tag, tag.color, false)
        }
    }

}

class TagListAdapter(context: Context) : BaseQuickAdapter<TagWrapper, BaseViewHolder>(R.layout.tag_chip_large, null) {

    interface OnTagClickListener {
        fun onClick(tag: String)
        fun onLongClick(tag: String)
    }

    var clickListener: OnTagClickListener? = null

    fun updateData(newData: List<TagWrapper>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }

    override fun convert(holder: BaseViewHolder, item: TagWrapper) {
        val tagContent = holder.getView<LinearLayout>(R.id.tag_view_container)

        val txtTagName = holder.getView<TextView>(R.id.txt_tag_name)
        txtTagName.text = item.tag

        if (item.isFiltered) {
            tagContent.background = ColorDrawable(Color.parseColor("#576DD9"))
            txtTagName.setTextColor(Color.WHITE)
        } else {
            tagContent.background = context.getDrawable(R.drawable.bg_chip_round_rect)
            if (!item.color.isNullOrEmpty()) {
                txtTagName.setTextColor(Color.parseColor(item.color))
            } else {
                txtTagName.setTextColor(Color.parseColor("#4B5162"))
            }
        }

        tagContent.setOnClickListener {
            clickListener?.onClick(item.tag)
        }

        tagContent.setOnLongClickListener {
            clickListener?.onLongClick(item.tag)
            false
        }
    }
}