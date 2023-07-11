package com.mickstarify.zotero.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.mickstarify.zotero.LibraryActivity.ListEntry
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

class TagListAdapter(val context: Context) : RecyclerView.Adapter<ViewHolder>() {

    private val diffCallback = object: DiffUtil.ItemCallback<TagWrapper>() {

        override fun areItemsTheSame(oldItem: TagWrapper, newItem: TagWrapper): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: TagWrapper, newItem: TagWrapper): Boolean {
            return oldItem.equals(newItem)
        }
    }

    private val mDiffer = AsyncListDiffer(this, diffCallback)

    interface OnTagClickListener {
        fun onClick(tag: String)
        fun onLongClick(tag: String)
    }

    var clickListener: OnTagClickListener? = null

    fun updateData(newData: List<TagWrapper>?) {
        mDiffer.submitList(newData)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =  LayoutInflater.from(parent.context).inflate(R.layout.tag_chip_large, parent, false)
        return TagViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return mDiffer.currentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val viewHolder = holder as TagViewHolder

        val item = mDiffer.currentList[position]

        val tagContent = viewHolder.tagContent
        val txtTagName = viewHolder.txtTagName

        txtTagName.text = item.tag

        if (item.isFiltered) {
            tagContent.background = ColorDrawable(Color.parseColor("#576DD9"))
            txtTagName.setTextColor(Color.WHITE)
        } else {
            tagContent.background = AppCompatResources.getDrawable(context, R.drawable.bg_chip_round_rect)
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

    class TagViewHolder(itemView: View): ViewHolder(itemView) {
        val tagContent = itemView.findViewById<LinearLayout>(R.id.tag_view_container)

        val txtTagName = itemView.findViewById<TextView>(R.id.txt_tag_name)
    }
}