package com.mickstarify.zotero

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.mickstarify.zotero.ui.main.PdfViewerFragment

class PdfViewerActivity : AppCompatActivity() {

    companion object {
        val PDF_URI = "pdf_uri"
    }

    var pdfFragment: PdfViewerFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pdf_viewer_activity)

        val pdf_uri = intent.data

//        Toast.makeText(this, "uri地址：$pdf_uri", Toast.LENGTH_SHORT).show()


        if (pdfFragment == null && pdf_uri != null) {
            pdfFragment = PdfViewerFragment()
            pdfFragment!!.setPdfByUri(pdf_uri)
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.container, pdfFragment!!)
            .commitNow()

//        if (savedInstanceState == null) {
//            supportFragmentManager.beginTransaction()
//                .replace(R.id.container, PdfViewerFragment.newInstance(pdf_uri))
//                .commitNow()
//        }
    }
}