package com.mickstarify.zotero

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.mickstarify.zotero.databinding.FragmentWebviewerBinding
import com.mickstarify.zotero.ui.PdfViewer.PdfViewerFragment
import com.mickstarify.zotero.ui.WebViewer.WebViewerFragment

class AttachmentViewerActivity : AppCompatActivity() {

    companion object {
        @JvmStatic
        val ATTACHMENT_URI = "attachment_uri"
        @JvmStatic
        val ATTACHMENT_TYPE = "attachment_type"
    }

    var pdfFragment: PdfViewerFragment? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pdf_viewer_activity)

        val attachmentType = intent.getStringExtra(ATTACHMENT_TYPE)

        navigateToTargetViewer(intent.data, attachmentType)

//        if (savedInstanceState == null) {
//            supportFragmentManager.beginTransaction()
//                .replace(R.id.container, PdfViewerFragment.newInstance(pdf_uri))
//                .commitNow()
//        }
    }

    private fun navigateToTargetViewer(uri: Uri?, type: String?) {
        if (uri == null || type == null) {
            Toast.makeText(this, "Error arguments of attachment!", Toast.LENGTH_SHORT).show()
            return
        }

        when (type) {
            "application/pdf" -> {
                if (pdfFragment == null) {
                    pdfFragment = PdfViewerFragment()
                    pdfFragment!!.setPdfByUri(uri)
                }

                navigateToFragment(pdfFragment!!)
            }
            "text/html" -> {
                navigateToFragment(WebViewerFragment.newInstance(uri.toString(), type))
            }

            else -> Toast.makeText(this, "不支持打开该的附件类型", Toast.LENGTH_SHORT).show()

        }
    }

    private fun navigateToFragment(fragment: Fragment?) {
        if (fragment == null) {
            Toast.makeText(this, "Null fragment!", Toast.LENGTH_SHORT).show()
            return
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commitNow()
    }
}