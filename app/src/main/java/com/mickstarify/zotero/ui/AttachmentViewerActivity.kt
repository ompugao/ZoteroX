package com.mickstarify.zotero.ui

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.mickstarify.zotero.R
import com.mickstarify.zotero.ZoteroApplication
import com.mickstarify.zotero.ZoteroStorage.AttachmentStorageManager
import com.mickstarify.zotero.ZoteroStorage.ZoteroDB.ZoteroDB
import com.mickstarify.zotero.ui.PdfViewer.PdfViewerFragment
import com.mickstarify.zotero.ui.WebViewer.WebViewerFragment
import javax.inject.Inject

class AttachmentViewerActivity : AppCompatActivity() {

    companion object {
        @JvmStatic
        val ATTACHMENT_URI = "attachment_uri"

        @JvmStatic
        val ATTACHMENT_KEY = "attachment_key"

        @JvmStatic
        val ATTACHMENT_TYPE = "attachment_type"
    }

    private var pdfFragment: PdfViewerFragment? = null

//    @Inject
//    lateinit var attachmentStorageManager: AttachmentStorageManager

//    @Inject
//    lateinit var zoteroDB: ZoteroDB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pdf_viewer_activity)

//        (application as ZoteroApplication).component.inject(this)

        val attachmentKey = intent.getStringExtra(ATTACHMENT_KEY)
        val attachmentType = intent.getStringExtra(ATTACHMENT_TYPE)

//        if (!attachmentKey.isNullOrEmpty()) {
//            Toast.makeText(this, "key: $attachmentKey", Toast.LENGTH_SHORT).show()
//        } else {
//            navigateToTargetViewer(intent.data, attachmentKey, attachmentType)
//        }

        navigateToTargetViewer(intent.data, attachmentKey, attachmentType)
    }

    private fun navigateToTargetViewer(uri: Uri?, attachmentKey: String?, type: String?) {
        if (uri == null || type == null) {
            Toast.makeText(this, "Error arguments of attachment!", Toast.LENGTH_SHORT).show()
            return
        }

        when (type) {
            "application/pdf" -> {
                if (pdfFragment == null) {
                    pdfFragment = PdfViewerFragment(uri, attachmentKey)
//                    pdfFragment!!.setPdfByUri(uri)
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