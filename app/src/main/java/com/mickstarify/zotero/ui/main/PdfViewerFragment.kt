package com.mickstarify.zotero.ui.main

import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.github.barteksc.pdfviewer.PDFView
import com.mickstarify.zotero.R
import com.mickstarify.zotero.databinding.PdfViewerFragmentBinding

class PdfViewerFragment : Fragment() {

    private var pdfUri: Uri? = null

    fun setPdfByUri(uri: Uri) {
        this.pdfUri = uri
    }

    private lateinit var viewModel: PdfViewerModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

//        DataBindingUtil.inflate<PdfViewerFragmentBinding>(layoutInflater, container, false)

        val rootView = inflater.inflate(R.layout.pdf_viewer_fragment, container, false)

        val cPDFView: PDFView = rootView.findViewById(R.id.pdfView)
        cPDFView.enableAntialiasing(true)
        cPDFView.isScrollbarFadingEnabled = true
//        cPDFView.isSwipeEnabled = true

        if (pdfUri != null) {
            cPDFView.fromUri(pdfUri).load()
        }

        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(PdfViewerModel::class.java)

    }



}