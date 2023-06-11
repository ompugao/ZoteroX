package com.mickstarify.zotero.ui.PdfViewer

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseNodeAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.github.barteksc.pdfviewer.PDFView
import com.mickstarify.zotero.MyLog
import com.mickstarify.zotero.R
import com.mickstarify.zotero.databinding.FragmentPdfContentsBinding
import com.mickstarify.zotero.models.TreeNodeData

import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.mickstarify.zotero.adapters.PdfContentsAdapter

class PdfContentsFragment(val pdfView: PDFView): Fragment() {

    private lateinit var viewModel: PdfViewerModel

    private lateinit var mBinding: FragmentPdfContentsBinding

    private lateinit var adapter: PdfContentsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(PdfViewerModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentPdfContentsBinding.inflate(inflater, container, false)

        adapter = PdfContentsAdapter(requireContext(), null)
        adapter.bindPdfView(pdfView)

        mBinding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        mBinding.recyclerView.adapter = adapter

        adapter.setTreeEvent(object : PdfContentsAdapter.TreeEvent {
            override fun onSelectTreeNode(data: TreeNodeData?) {
                data?.let {
                    pdfView.jumpTo(it.pageNum, true)
                }
            }
        })

        viewModel.bookmarks.observe(requireActivity(),
            { list ->
                MyLog.e("ZoteroDebug", "接受到的目录数量：${list.size}")

                if (list.isNullOrEmpty()) {
                    showEmptyView()
                    return@observe
                } else {
                    hideEmptyView()
                }

                val catalogues = viewModel.convertToCatalogues(list)

                adapter.setData(catalogues)
                adapter.notifyDataSetChanged()
            })

        return mBinding.root
    }

    fun showEmptyView() {
        mBinding.txtEmptyView.visibility = View.VISIBLE
    }

    fun hideEmptyView() {
        mBinding.txtEmptyView.visibility = View.GONE
    }





}