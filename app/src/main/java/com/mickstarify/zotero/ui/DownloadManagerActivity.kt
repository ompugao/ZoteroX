package com.mickstarify.zotero.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.mickstarify.zotero.R
import com.mickstarify.zotero.databinding.ActivityDownloadManagerBinding
import com.yuan.library.dmanager.download.DownloadManager

class DownloadManagerActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityDownloadManagerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_download_manager)

        val downloadManager = DownloadManager.getInstance()
    }
}