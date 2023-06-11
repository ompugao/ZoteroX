package com.mickstarify.zotero.models

import com.chad.library.adapter.base.entity.node.BaseNode

/**
 * 树形控件数据类（会用于页面间传输，所以需实现Serializable 或 Parcelable）
 */
data class TreeNodeData(var name: String? = null,
                        var pageNum: Int = 0,
                        var isExpanded: Boolean = false, //是否已展开（用于控制树形节点图片显示，即箭头朝向图片）
                        var treeLevel: Int = 0   //展示级别(1级、2级...，用于控制树形节点缩进位置)
): BaseNode() {

    //子集（用于加载子节点，也用于判断是否显示箭头图片，如集合不为空，则显示）
    var subSet: MutableList<TreeNodeData>? = null

    companion object {

//        fun convert(bookmark: PdfDocument.Bookmark): TreeNodeData {
//            val nodeData = TreeNodeData()
//            nodeData.name = bookmark.title
//            nodeData.pageNum = bookmark.pageIdx.toInt()
//            nodeData.treeLevel = level
//            nodeData.isExpanded = false
//
//            return nodeData
//        }

    }

    override val childNode: MutableList<BaseNode>?
        get() = subSet as MutableList<BaseNode>?

}