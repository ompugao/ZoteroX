package com.mickstarify.zotero.adapters

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.barteksc.pdfviewer.PDFView
import com.mickstarify.zotero.R
import com.mickstarify.zotero.models.TreeNodeData

import android.view.LayoutInflater

import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil


class PdfContentsAdapter(val context: Context, data: List<TreeNodeData>? = null) : RecyclerView.Adapter<PdfContentsAdapter.TreeNodeViewHolder>() {

    private var pdfView: PDFView? = null

    fun bindPdfView(pdfView: PDFView) {
        this.pdfView = pdfView
    }

    // 原始数据，注意该数据并不直接用于展示
    private var data: List<TreeNodeData>? = null

    //子标题间隔(dp)
    private var marginLeft = 50

    //委托对象
    private var delegate: TreeEvent? = null

    private val diffCallback = object: DiffUtil.ItemCallback<TreeNodeData>() {

        override fun areItemsTheSame(oldItem: TreeNodeData, newItem: TreeNodeData): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: TreeNodeData, newItem: TreeNodeData): Boolean {
            return oldItem == newItem
        }
    }

    private val mDiffer = AsyncListDiffer(this, diffCallback)

    init {
        this.data = data
        //数据转为展示数据
        dataToDisplayData(data)
    }

    /**
     * 数据转为展示数据
     *
     * @param data 数据
     */
    private fun dataToDisplayData(data: List<TreeNodeData>?) {
        if (data == null) return

        val newList = mutableListOf<TreeNodeData>()
        rearrangeData(newList, data)
        mDiffer.submitList(newList)
    }


    private fun rearrangeData(newList : MutableList<TreeNodeData>, data: List<TreeNodeData>?) {
        if (data == null) return

        for (nodeData in data) {
            newList.add(nodeData)
            if (nodeData.isExpanded && nodeData.subSet != null) {
                rearrangeData(newList, nodeData.subSet!!)
            }
        }

    }

    /**
     * 数据集合转为可显示的集合
     */
    private fun redisplayData() {
        if (data.isNullOrEmpty()) {
            return
        }
        dataToDisplayData(data)
    }

    fun setData(data: List<TreeNodeData>?) {
        this.data = data
        //数据转为展示数据
        dataToDisplayData(data)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TreeNodeViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.item_pdf_content_bookmark, parent, false)
        return TreeNodeViewHolder(view)

    }

    override fun onBindViewHolder(holder: TreeNodeViewHolder, position: Int) {
        val data = mDiffer.currentList[position]

        //设置图片
        if (data.subSet != null) {
            holder.btnExpand.visibility = View.VISIBLE
            if (data.isExpanded) {
                holder.btnExpand.setImageResource(R.drawable.ic_arrow_up)
            } else {
                holder.btnExpand.setImageResource(R.drawable.ic_arrow_down)
            }
        } else {
            holder.btnExpand.visibility = View.INVISIBLE
        }

        //设置标题偏移位置
        val ratio = if (data.treeLevel <= 0) 0 else data.treeLevel - 1
        holder.title.setPadding(marginLeft * ratio, 0, 0, 0)

        //显示文本
        holder.title.text = data.name

        holder.pageNum.text = data.pageNum.toString()

        //图片点击事件
        holder.btnExpand.setOnClickListener { //控制树节点展开、折叠
            data.isExpanded = !data.isExpanded
            //刷新数据源
            redisplayData()
        }
        holder.itemView.setOnClickListener { //回调结果
            delegate?.onSelectTreeNode(data)
        }
    }

    override fun getItemCount(): Int {
        return mDiffer.currentList.size
    }

    /**
     * 定义RecyclerView的ViewHolder对象
     */
    class TreeNodeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var btnExpand: ImageButton = view.findViewById(R.id.btn_expand)
        var title: TextView = view.findViewById(R.id.txt_bookmark_title)
        var pageNum: TextView = view.findViewById(R.id.txt_page_count)

    }

    /**
     * 接口：Tree事件
     */
    interface TreeEvent {
        /**
         * 当选择了某tree节点
         * @param data tree节点数据
         */
        fun onSelectTreeNode(data: TreeNodeData?)
    }

    /**
     * 设置Tree的事件
     * @param treeEvent Tree的事件对象
     */
    fun setTreeEvent(treeEvent: TreeEvent?) {
        delegate = treeEvent
    }
}