package com.mickstarify.zotero.AttachmentManager

import android.content.Context
import com.mickstarify.zotero.R
import kotlin.math.round

class AttachmentManagerPresenter(val view: AttachmentManager, context: Context) : Contract.Presenter {
    val model: Contract.Model

    init {
        model = AttachmentManagerModel(this, context)
        view.initUI()
        model.loadLibrary()
    }

    override fun pressedDownloadAttachments() {
        if (model.isDownloading) {
            // this is a cancel request.
            view.setDownloadButtonState(view.getString(R.string.download_all_attachments), true)
            model.cancelDownload()
        } else {
            // download request.
            view.setDownloadButtonState(view.getString(R.string.cancel_download), true)
            model.downloadAttachments()
        }

    }

    override fun displayAttachmentInformation(nLocal: Int, sizeLocal: Long, nRemote: Int) {
        var sizeLocalString = if (sizeLocal < 1000000L) {
            "${(sizeLocal / 1000L).toInt()}KB"
        } else {
            // rounds off to 2 decimal places. e.g 43240000 => 43.24MB

            "${round(sizeLocal.toDouble() / 10000.0) / 100}MB"
        }

        view.displayAttachmentInformation(nLocal, sizeLocalString, nRemote)
    }

    override fun displayLoadingAnimation() {
        view.showLibraryLoadingAnimation()
    }

    override fun finishLoadingAnimation() {
        view.hideLibraryLoadingAnimation()
        if (!model.isDownloading) {
            view.setDownloadButtonState(view.getString(R.string.download_all_attachments), true)
        }
    }

    override fun makeToastAlert(message: String) {
        view.makeToastAlert(message)
    }

    override fun createErrorAlert(title: String, message: String, onClick: () -> Unit) {
        view.createErrorAlert(title, message, onClick)
    }

    override fun isDownloading(): Boolean {
        return model.isDownloading
    }

    override fun displayErrorState() {
    }

    override fun updateProgress(filename: String, current: Int, total: Int) {
        view.updateLoadingProgress(filename, current, total)
    }
}