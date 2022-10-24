package com.mickstarify.zotero

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.mickstarify.zotero.ui.main.PdfViewerFragment

class PdfViewerActivity : AppCompatActivity() {

    companion object {
        val PDF_URI = "pdf_uri"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pdf_viewer_activity)

        val pdf_uri = intent.data

//        Toast.makeText(this, "uri地址：$pdf_uri", Toast.LENGTH_SHORT).show()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, PdfViewerFragment.newInstance(pdf_uri))
                .commitNow()
        }
    }
}