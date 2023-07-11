package com.mickstarify.zotero.ui.PdfViewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.barteksc.pdfviewer.PDFView
import com.mickstarify.zotero.MyLog
import com.mickstarify.zotero.databinding.FragmentPdfContentsBinding
import com.mickstarify.zotero.models.TreeNodeData

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
                if (list.isNullOrEmpty()) {
                    showEmptyView()
                    adapter.submitList(emptyList())
                    return@observe
                } else {
                    hideEmptyView()
                }

                val catalogues = viewModel.convertToCatalogues(list)
                adapter.submitList(catalogues)
            })

        return mBinding.root
    }

    private fun showEmptyView() {
        mBinding.layoutEmptyView.visibility = View.VISIBLE
    }

    private fun hideEmptyView() {
        mBinding.layoutEmptyView.visibility = View.GONE
    }

}