package com.mickstarify.zotero.ui.PdfViewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.mickstarify.zotero.MyLog
import com.mickstarify.zotero.R
import com.mickstarify.zotero.adapters.PdfAnnotationAdapter
import com.mickstarify.zotero.databinding.FragmentPdfAnnotationBinding
import com.mickstarify.zotero.models.PdfAnnotation
import com.moyear.pdfview.view.MyPDFView

class PdfAnnotationsFragment(private val pdfView: MyPDFView) : Fragment(), PdfAnnotationAdapter.OnAnnotationNavigateListener {

    private lateinit var viewModel: PdfViewerModel

    private lateinit var mBinding: FragmentPdfAnnotationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(PdfViewerModel::class.java)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentPdfAnnotationBinding.inflate(inflater, container, false)

        initLayout()

        return mBinding.root
    }

    private fun initLayout() {
        val adapter = PdfAnnotationAdapter(viewModel.pdfiumSDK, viewModel.getAttachmentFileName())
        adapter.onAnnotationNavigateListener = this
        adapter.setEmptyView(R.layout.layout_empty_list)

        mBinding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        mBinding.recyclerView.adapter = adapter

        viewModel.pdfAnnotations.observe(viewLifecycleOwner,
            { list ->
                if (list.isNullOrEmpty()) {
                    showEmptyView()
                } else {
                    hideEmptyView()
                }

                adapter.submitData(list)
            })
    }

    private fun hideEmptyView() {
        mBinding.layoutEmptyView.visibility = View.GONE
    }

    private fun showEmptyView() {
        mBinding.layoutEmptyView.visibility = View.VISIBLE
    }

    override fun onNavigate(annotation: PdfAnnotation) {
        viewModel.navigateToAnnotation(pdfView, annotation)

        MyLog.d("ZoteroDebug", "click annotation: $annotation")
    }

}