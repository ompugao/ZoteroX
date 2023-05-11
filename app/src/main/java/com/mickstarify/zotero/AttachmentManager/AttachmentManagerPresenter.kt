package com.mickstarify.zotero.AttachmentManager

import android.content.Context
import android.content.Intent
import android.telephony.mbms.DownloadProgressListener
import com.mickstarify.zotero.AttachmentViewerActivity
import com.mickstarify.zotero.ZoteroStorage.Database.Item
import com.mickstarify.zotero.adapters.AttachmentListAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.round

class AttachmentManagerPresenter(val view: AttachmentManager, context: Context, ) : Contract.Presenter, AttachmentListAdapter.AttachInteractionListener {
    val model: AttachmentManagerModel

    init {
        model = AttachmentManagerModel(this, context)
        model.loadLibrary()

        model.getAttachmentItems().observe(view) {
            it.let {
                view.updateAttachments(it)
            }
        }

        view.initUI()
    }

    override fun pressedDownloadAttachments() {
        if (model.isDownloading) {
            // this is a cancel request.
            model.cancelDownload()
        } else {
            // download request.
            model.downloadAttachments()
        }

    }

    fun updateDownloadProgress(progress: Long, total: Long) {
        val progressKB = (progress / 1000).toInt()
        val totalKB = (total / 1000).toInt()
        view.updateDownloadProgress(progressKB, totalKB)
    }

    fun hideDownloadProgress() {
        view.hideDownloadProgress()
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

    override fun onAttachmentDownload(item: Item, progressListener: AttachmentListAdapter.DownloadListener) {
        model.downloadAttachment(item, progressListener)
    }

    override fun onAttachmentOpen(item: Item) {
        CoroutineScope(Dispatchers.Default).launch {
            val attachmentType = model.attachmentStorageManager.getAttachmentType(item)

            val isExist = model.attachmentStorageManager.simpleCheckIfAttachmentExists(item)

            withContext(Dispatchers.Main) {
                if (!isExist) {
                    createErrorAlert("附件不存在", "${item.getTitle()}不存在或者尚未下载到本地") {}
                    return@withContext
                }

                when (attachmentType) {
                    "application/pdf" -> openPdf(item, attachmentType)
                    "text/html" -> openWebPage(item, attachmentType)
                    else -> createErrorAlert("不支持的附件类型", "暂不支持打开${attachmentType}类型的附件") {}
                }
            }
        }

    }

    private fun openWebPage(item: Item, itemType: String) {
        val attachment_uri = model.attachmentStorageManager.getAttachmentUri(item)
        val intent = Intent(view, AttachmentViewerActivity::class.java)
        intent.data = attachment_uri
        intent.putExtra(AttachmentViewerActivity.ATTACHMENT_TYPE, itemType)
        view.startActivity(intent)

    }

    private fun openPdf(item: Item, itemType: String) {
        try {
            if (!model.isUseExternalPdfReader()) {
                val attachment_uri = model.attachmentStorageManager.getAttachmentUri(item)

                val intent = Intent(view, AttachmentViewerActivity::class.java)
                intent.data = attachment_uri
                intent.putExtra(AttachmentViewerActivity.ATTACHMENT_TYPE, itemType)
                view.startActivity(intent)

            } else {
                val intent = model.attachmentStorageManager.openAttachment(item)
                view.startActivity(intent)
            }
        } catch (e: Exception) {
            createErrorAlert("Error", e.toString()) {}
        }

    }
}