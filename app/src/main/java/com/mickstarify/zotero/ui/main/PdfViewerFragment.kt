package com.mickstarify.zotero.ui.main

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mickstarify.zotero.R

class PdfViewerFragment : Fragment() {

    companion object {
        fun newInstance() = PdfViewerFragment()
    }

    private lateinit var viewModel: PdfViewerModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.pdf_viewer_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(PdfViewerModel::class.java)
        // TODO: Use the ViewModel
    }

}