package com.mickstarify.zotero.ui.WebViewer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mickstarify.zotero.databinding.FragmentWebviewerBinding

private const val ARG_URI = "param1"
private const val ARG_TYPE = "param2"

class WebViewerFragment : Fragment() {
    private var attachment_uri: String? = null
    private var attachment_type: String? = null

    private lateinit var mBinding: FragmentWebviewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            attachment_uri = it.getString(ARG_URI)
            attachment_type = it.getString(ARG_TYPE)
        }
        mBinding = FragmentWebviewerBinding.inflate(layoutInflater)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.btnBack.setOnClickListener { requireActivity().finish() }

        attachment_uri?.let {
            mBinding.webViewer.loadUrl(it)
        }

    }

    companion object {
        @JvmStatic
        fun newInstance(uri: String, type: String) =
            WebViewerFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_URI, uri)
                    putString(ARG_TYPE, type)
                }
            }
    }
}