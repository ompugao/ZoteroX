package com.mickstarify.zotero.ui.PdfViewer

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.mickstarify.zotero.ZoteroApplication
import com.mickstarify.zotero.ZoteroStorage.AttachmentStorageManager
import com.mickstarify.zotero.ZoteroStorage.Database.Item
import com.mickstarify.zotero.ZoteroStorage.ZoteroDB.ZoteroDB
import com.mickstarify.zotero.models.TreeNodeData
import com.shockwave.pdfium.PdfDocument.Bookmark
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


class PdfViewerModel(application: Application) : AndroidViewModel(application) {

    var pageCount: Int = -1

    var currentPage: Int = -1

    var attachmentKey: String = ""

    var attachmentItem: Item? = null

    var pdfUri: Uri? = null

    var showTools = MutableLiveData<Boolean>(true)

    var bookmarks = MutableLiveData<List<Bookmark>>()

    val nightMode = MutableLiveData<Boolean>(false)

    @Inject
    lateinit var attachmentStorageManager: AttachmentStorageManager

    private var zoteroDB: ZoteroDB? = null

    init {
        (application as ZoteroApplication).component.inject(this)

        zoteroDB = (application as ZoteroApplication).zoteroDB

    }

    /**
     * 将bookmark转为目录数据集合（递归）
     *
     * @param catalogues 目录数据集合
     * @param bookmarks  书签数据
     * @param level      目录树级别（用于控制树节点位置偏移）
     */
    private fun bookmarkToCatalogues(
        catalogues: MutableList<TreeNodeData>,
        bookmarks: List<Bookmark>,
        level: Int
    ) {
        for (bookmark in bookmarks) {
            val node = TreeNodeData()
            node.name = bookmark.title
            node.pageNum = bookmark.pageIdx.toInt()
            node.treeLevel = level
            node.isExpanded = false
            catalogues.add(node)

            if (bookmark.children != null && bookmark.children.size > 0) {
                val treeNodeData: MutableList<TreeNodeData> = ArrayList()
                node.subSet = treeNodeData
                bookmarkToCatalogues(treeNodeData, bookmark.children, level + 1)
            }
        }
    }
    fun convertToCatalogues(bookmarks: List<Bookmark>): List<TreeNodeData> {
        val catalogues = mutableListOf<TreeNodeData>()
        bookmarkToCatalogues(catalogues, bookmarks, 1)
        return catalogues
    }

    fun getAttachmentName(): String {
        return attachmentItem?.getTitle() ?: ""
    }

    fun getAttachmentFileName(): String {
        var fileName = ""
        attachmentItem?.let {
            fileName = attachmentStorageManager.getFilenameForItem(it)
        }
        return fileName
    }

    fun getItemWithKey(itemKey: String): Item? {
        if (zoteroDB == null) return null
        return zoteroDB!!.getItemWithKey(itemKey)
    }

    fun initParams(attachmentKey: String?, pdfUri: Uri?) {
        // 初始化pdf附件的基本参数（uri和key）
        this.pdfUri = pdfUri
        this.attachmentKey = attachmentKey ?:""

        CoroutineScope(Dispatchers.IO).launch {
            attachmentKey?.let {
                attachmentItem = getItemWithKey(it)
            }
        }
    }

    fun fetchAnnotations() {
//        zoteroDB?.items?.filter {  }

    }

}