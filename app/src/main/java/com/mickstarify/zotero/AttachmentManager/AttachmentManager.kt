package com.mickstarify.zotero.AttachmentManager

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mickstarify.zotero.MyLog
import com.mickstarify.zotero.R
import com.mickstarify.zotero.databinding.ActivityAttachmentManagerBinding
import com.mickstarify.zotero.views.MaterialProgressDialog

class AttachmentManager : AppCompatActivity(), Contract.View {
    lateinit var presenter: Contract.Presenter
    lateinit var mBinding: ActivityAttachmentManagerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_attachment_manager)

//        mBinding = ActivityAttachmentManagerBinding.inflate(layoutInflater)
//        setContentView(R.layout.activity_attachment_manager)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        presenter = AttachmentManagerPresenter(this, this)
    }

    override fun initUI() {
//        findViewById<LinearLayout>(R.id.ll_meta_information).visibility = View.INVISIBLE
        findViewById<Button>(R.id.button_download).setOnClickListener {
            presenter.pressedDownloadAttachments()
        }

        mBinding.btnBack.setOnClickListener {
            finish()
        }

//        disableDownloadButton()
    }

    private fun disableDownloadButton() {
//        setDownloadButtonState("Loading Library", false)
        findViewById<Button>(R.id.button_download).visibility = View.GONE
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

    override fun hideLibraryLoadingAnimation() {
        progressBar?.visibility = View.GONE

        val textView = findViewById<TextView>(R.id.txt_attachment_downloading)
        textView.visibility = View.GONE

        progressDialog?.dismiss()
        progressDialog = null
    }

    override fun setDownloadButtonState(text: String, enabled: Boolean) {
        val button = findViewById<Button>(R.id.button_download)
        button.visibility = View.VISIBLE
        button.isEnabled = enabled
        button.text = text

        MyLog.d("zotero", "button state changed. ${button.text} ${button.isEnabled}")
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

//        val textView = findViewById<TextView>(R.id.txt_loading_library_text)
        val textView = findViewById<TextView>(R.id.txt_attachment_downloading)
        textView.visibility = View.VISIBLE
        textView.text = message
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


}
