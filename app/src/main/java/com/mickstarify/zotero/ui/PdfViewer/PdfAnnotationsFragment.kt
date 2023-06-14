package com.mickstarify.zotero.ui.PdfViewer

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
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
        val adapter = PdfAnnotationAdapter()
        adapter.onAnnotationNavigateListener = this
        adapter.setEmptyView(R.layout.layout_empty_list)

        mBinding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        mBinding.recyclerView.adapter = adapter

        viewModel.pdfAnnotations.observe(viewLifecycleOwner,
            { list ->
                list?.let {
                    adapter.data = list as MutableList<PdfAnnotation>
                    adapter.notifyDataSetChanged()
                }

                list?.forEach {
                    MyLog.e("ZoteroDebug", "sortIndex：${it.sortIndex}")
//                    MyLog.e("ZoteroDebug", "position：${it.position}")
                }
            })

        viewModel.loadAttachmentAnnotations()
    }

    override fun onNavigate(annotation: PdfAnnotation) {
        viewModel.navigateToAnnotation(pdfView, annotation)

    }


}