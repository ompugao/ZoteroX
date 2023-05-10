package com.mickstarify.zotero.AttachmentManager

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mickstarify.zotero.MyLog
import com.mickstarify.zotero.R
import com.mickstarify.zotero.ZoteroApplication
import com.mickstarify.zotero.ZoteroStorage.AttachmentStorageManager
import com.mickstarify.zotero.ZoteroStorage.Database.Item
import com.mickstarify.zotero.adapters.AttachmentListAdapter
import com.mickstarify.zotero.databinding.ActivityAttachmentManagerBinding
import com.mickstarify.zotero.databinding.ContentDialogProgressBinding
import com.mickstarify.zotero.models.AttachmentEntry
import com.mickstarify.zotero.views.MaterialProgressDialog
import javax.inject.Inject

class AttachmentManager : AppCompatActivity(), Contract.View, AttachmentListAdapter.AttachInteractionListener {
    lateinit var presenter: AttachmentManagerPresenter
    lateinit var mBinding: ActivityAttachmentManagerBinding

    var  attachmentsListAdapter: AttachmentListAdapter? = null

    @Inject
    lateinit var attachmentStorageManager: AttachmentStorageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as ZoteroApplication).component.inject(this)

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_attachment_manager)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        presenter = AttachmentManagerPresenter(this, this)

    }

    override fun initUI() {
        mBinding.btnBack.setOnClickListener {
            finish()
        }

        mBinding.btnMore.setOnClickListener {
            showMoreMenu(it)
        }

        val rvAttachments = mBinding.root.findViewById<RecyclerView>(R.id.rvAttachments)
        rvAttachments.layoutManager = LinearLayoutManager(this)

        attachmentsListAdapter = AttachmentListAdapter(this, attachmentStorageManager)
        rvAttachments.adapter = attachmentsListAdapter

        attachmentsListAdapter?.listener = this

//        attachmentsListAdapter.setOnItemClickListener()
//        attachmentsListAdapter?.setOnItemClickListener {
//            _, view, position ->
//
//            MyLog.d("ZoteroDebug", "Open attachment: ${position}")
//
//        }

    }

    fun updateAttachments(entries: List<AttachmentEntry>) {
//        attachmentsListAdapter?.data = entries.toMutableList()
        attachmentsListAdapter?.setData(entries.toMutableList())
        attachmentsListAdapter?.notifyDataSetChanged()

    }

    private fun showMoreMenu(view: View?) {
        val popup = PopupMenu(applicationContext, view)
        menuInflater.inflate(R.menu.activity_attachment_manager, popup.menu)

        popup.menu.findItem(R.id.download_all).let {
            if (presenter.isDownloading())
                it.title = getString(R.string.cancel_download)
            else
                it.title = getString(R.string.download_all_attachments)
        }

        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.download_all -> presenter.pressedDownloadAttachments()
            }

            false
        }

        popup.show()

    }

    override fun makeToastAlert(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
        toast.show()
    }

    override fun createErrorAlert(title: String, message: String, onClick: () -> Unit) {
        val alert = MaterialAlertDialogBuilder(this)
        alert.setIcon(R.drawable.ic_error_black_24dp)
        alert.setTitle(title)
        alert.setMessage(message)
        alert.setPositiveButton(getString(R.string.oK)) { _, _ -> onClick() }
        try {
            alert.show()
        } catch (exception: WindowManager.BadTokenException) {
            MyLog.e("zotero", "error cannot show error dialog. ${message}")
        }
    }

    var progressBar: ProgressBar? = null

    var progressDialog: AlertDialog? = null

    override fun showLibraryLoadingAnimation() {
        if (progressBar == null) {
            progressBar = findViewById(R.id.progressBar)
        }

        if (progressDialog == null) {
            progressDialog = MaterialProgressDialog.createProgressDialog(this, "", getString(R.string.calculating_attachments)).show()
        }
    }

    var progressBinding: ContentDialogProgressBinding? = null
    fun updateDownloadProgress(progress: Int, total: Int) {
        val progressString = if (progress == 0) {
            ""
        } else {
            if (total > 0) {
                "$progress/${total} KB"
            } else {
                "${progress} KB"
            }
        }

        val progressMsg = "附件已下载：${progressString}"

        if (progressDialog == null) {
            val dialogBuilder = MaterialAlertDialogBuilder(this)

            if (progressDialog == null) {
                progressBinding = ContentDialogProgressBinding.inflate(layoutInflater)
                progressBinding?.txtContent?.text = progressMsg

            }

            dialogBuilder.setTitle("下载中")
                .setCancelable(false)
                .setView(progressBinding?.root)
                .setNegativeButton(getString(R.string.cancel)) {
                    view, p2 ->
                }

            progressDialog = dialogBuilder.show()
        } else {
            progressBinding?.txtContent?.text = progressMsg
        }
    }

    fun hideDownloadProgress() {
        progressDialog?.dismiss()
        progressDialog = null
        progressBinding = null

    }

    override fun hideLibraryLoadingAnimation() {
        progressBar?.visibility = View.GONE

        val textView = findViewById<TextView>(R.id.txt_attachment_downloading)
        textView.visibility = View.GONE

        progressDialog?.dismiss()
        progressDialog = null
    }

    override fun setDownloadButtonState(text: String, enabled: Boolean) {

    }

    override fun updateLoadingProgress(message: String, current: Int, total: Int) {
        if (progressBar == null) {
            progressBar = findViewById(R.id.progressBar)
        }
        progressBar?.let {
            it.visibility = View.VISIBLE
            it.isIndeterminate = current == 0
            it.progress = current
            it.max = total
            it.isActivated = true
        }

        val textView = findViewById<TextView>(R.id.txt_attachment_downloading)
        textView.visibility = View.VISIBLE
        textView.text = "正在下载: $message"
    }

    override fun displayAttachmentInformation(
        nLocal: Int,
        sizeLocal: String,
        nRemote: Int
    ) {
        findViewById<TextView>(R.id.txt_number_attachments).text =
            "${getString(R.string.downloaded)}: $nLocal/$nRemote" +
                    "\n${getString(R.string.used)}: $sizeLocal"

        //todo add free disk space.
    }


    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        if (presenter.isDownloading()) {
            this.makeToastAlert("Do not exit while download is active. Cancel it first.")
        } else {
            super.onBackPressed()
        }
    }

    override fun onAttachmentDownload(item: Item) {
        presenter.onAttachmentDownload(item)
    }

    override fun onAttachmentOpen(item: Item) {
        presenter.onAttachmentOpen(item)

    }


}
