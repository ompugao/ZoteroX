package com.mickstarify.zotero.ui.PdfViewer

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.barteksc.pdfviewer.PDFView
import com.mickstarify.zotero.adapters.PdfThumbnailAdapter
import com.mickstarify.zotero.databinding.FragmentPdfThumbnailsBinding
import com.shockwave.pdfium.PdfDocument
import com.shockwave.pdfium.PdfiumCore
import android.os.ParcelFileDescriptor
import androidx.recyclerview.widget.GridLayoutManager
import com.blankj.utilcode.util.EncryptUtils
import java.lang.Exception

class PdfThumbnailsFragment(val pdfView: PDFView): Fragment() {

    private lateinit var viewModel: PdfViewerModel
    private lateinit var mBinding: FragmentPdfThumbnailsBinding

    private var adapter: PdfThumbnailAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(PdfViewerModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentPdfThumbnailsBinding.inflate(inflater, container, false)

        loadData()
        return mBinding.root
    }

    /**
     * 加载数据
     */
    private fun loadData() {
        //获得pdf总页数
        val totalCount = viewModel.pageCount

        val md5String= EncryptUtils.encryptMD5ToString(viewModel.pdfUri.toString())
        //绑定列表数据
        adapter = PdfThumbnailAdapter(
            requireContext(),
            viewModel.pdfiumCore!!, viewModel.pdfDocument!!, md5String, totalCount
        )

//        adapter.setGridEvent(this)
        mBinding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 4)
        mBinding.recyclerView.adapter = adapter

        adapter?.setGridEvent(object : PdfThumbnailAdapter.GridEvent {
            override fun onGridItemClick(position: Int) {
                pdfView.jumpTo(position, true)
            }

        })
    }

}