package com.mickstarify.zotero

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mickstarify.zotero.ui.main.PdfViewerFragment

class PdfViewerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pdf_viewer_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, PdfViewerFragment.newInstance())
                .commitNow()
        }
    }
}