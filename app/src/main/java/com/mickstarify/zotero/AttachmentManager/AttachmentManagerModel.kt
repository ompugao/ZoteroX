package com.mickstarify.zotero.AttachmentManager

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mickstarify.zotero.MyLog
import com.mickstarify.zotero.PreferenceManager
import com.mickstarify.zotero.R
import com.mickstarify.zotero.SyncSetup.AuthenticationStorage
import com.mickstarify.zotero.ZoteroApplication
import com.mickstarify.zotero.ZoteroAPI.DownloadProgress
import com.mickstarify.zotero.ZoteroAPI.ZoteroAPI
import com.mickstarify.zotero.ZoteroStorage.AttachmentStorageManager
import com.mickstarify.zotero.ZoteroStorage.Database.AttachmentInfo
import com.mickstarify.zotero.ZoteroStorage.Database.GroupInfo
import com.mickstarify.zotero.ZoteroStorage.Database.Item
import com.mickstarify.zotero.ZoteroStorage.Database.ZoteroDatabase
import com.mickstarify.zotero.ZoteroStorage.ZoteroDB.ZoteroDB
import com.mickstarify.zotero.adapters.AttachmentListAdapter
import com.mickstarify.zotero.models.AttachmentEntry
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.internal.operators.observable.ObservableFromIterable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.LinkedList
import javax.inject.Inject

class AttachmentManagerModel(val presenter: AttachmentManagerPresenter, val context: Context) :
    Contract.Model {

    lateinit var zoteroAPI: ZoteroAPI
    lateinit var zoteroDB: ZoteroDB

    @Inject
    lateinit var zoteroDatabase: ZoteroDatabase

    @Inject
    lateinit var attachmentStorageManager: AttachmentStorageManager

    @Inject
    lateinit var preferenceManager: PreferenceManager

    override var isDownloading = false // useful for button state.

    init {
        ((context as Activity).application as ZoteroApplication).component.inject(this)
        val auth = AuthenticationStorage(context)
        if (auth.hasCredentials()) {
            zoteroAPI = ZoteroAPI(
                auth.getUserKey(),
                auth.getUserID(),
                auth.getUsername(),
                attachmentStorageManager
            )
        } else {
            presenter.createErrorAlert(
                "No credentials Available",
                "There is no credentials available to connect " +
                        "to the zotero server. Please relogin to the app (clear app data if necessary)"
            ) { (context.finish()) }
        }

        if (!preferenceManager.hasShownCustomStorageWarning() && attachmentStorageManager.storageMode == AttachmentStorageManager.StorageMode.CUSTOM) {
            preferenceManager.setShownCustomStorageWarning(true)

            presenter.createErrorAlert(
                context.getString(R.string.custom_storage_selected),
                context.getString(R.string.warning_use_custom_storage)
            ) {}
        }
    }

    data class downloadAllProgress(
        var status: Boolean,
        var currentIndex: Int,
        var attachment: Item
    )

    override fun cancelDownload() {
        Log.d("zotero", "canceling download")
        isDownloading = false
        downloadDisposable?.dispose()
        presenter.finishLoadingAnimation()
    }

    var downloadDisposable: Disposable? = null

    override fun downloadAttachments() {
        if (isDownloading) {
            Log.e("zotero", "Error already downloading")
            return
        }
        // just incase it's still running.
        this.stopCalculateMetaInformation()
        isDownloading = true

        val toDownload = LinkedList<Item>()

        Completable.fromAction {
            val attachmentItems = zoteroDB.items!!.filter { it.itemType == "attachment" && it.data["linkMode"] != "linked_file" }
            for (attachment in attachmentItems) {
                if (attachment.isDownloadable()) {
                    toDownload.add(attachment)
                }
            }

        }.subscribeOn(Schedulers.io()).andThen(ObservableFromIterable(toDownload.withIndex()).map {
            val i = it.index
            val attachment = it.value
            var status = true

            if (!attachmentStorageManager.checkIfAttachmentExists(attachment, checkMd5 = false)) {
                zoteroAPI.downloadItemRx(attachment, zoteroDB.groupID, context)
                    .blockingSubscribe(object : Observer<DownloadProgress> {
                        var setMetadata = false
                        override fun onComplete() {
                            // do nothing.
                        }

                        override fun onSubscribe(d: Disposable) {
                        }

                        override fun onNext(it: DownloadProgress) {
                            if (setMetadata == false && it.metadataHash != "") {
                                val err = zoteroDatabase.writeAttachmentInfo(
                                    AttachmentInfo(
                                        attachment.itemKey,
                                        zoteroDB.groupID,
                                        it.metadataHash,
                                        it.mtime,
                                        if (preferenceManager.isWebDAVEnabled()) {
                                            AttachmentInfo.WEBDAV
                                        } else {
                                            AttachmentInfo.ZOTEROAPI
                                        }
                                    )
                                ).blockingGet()
                                err?.let { throw(err) }
                                setMetadata = true
                            }
                        }

                        override fun onError(e: Throwable) {
                            Log.d("zotero", "got error on api download, $e")
                            status = false
                        }

                    })
            }
            downloadAllProgress(status, i, attachment)
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()))
            .subscribe(object : Observer<downloadAllProgress> {
                var localAttachmentSize = 0L
                var nLocalAttachments = 0

                override fun onComplete() {
                    calculateMetaInformation()
                    isDownloading = false
                    presenter.finishLoadingAnimation()
                    presenter.createErrorAlert(
                        "Finished Downloading",
                        "All your attachments have been downloaded.",
                        {})
                }

                override fun onSubscribe(d: Disposable) {
                    presenter.updateProgress("Starting Download", 0, toDownload.size)
                    downloadDisposable = d
                }

                override fun onNext(progress: downloadAllProgress) {
                    Log.d(
                        "zotero",
                        "got progress, status= ${progress.status} item=${progress.attachment.itemKey} disposeState= ${downloadDisposable?.isDisposed}"
                    )
                    if (progress.status) {
                        localAttachmentSize += attachmentStorageManager.getFileSize(
                            progress.attachment
                        )
                        nLocalAttachments++
                        presenter.displayAttachmentInformation(
                            nLocalAttachments,
                            localAttachmentSize,
                            toDownload.size
                        )

                        presenter.updateProgress(
                            progress.attachment.data["filename"] ?: "unknown",
                            progress.currentIndex,
                            toDownload.size
                        )
                    } else {
                        presenter.makeToastAlert("Error downloading ${progress.attachment.getTitle()}")
                        presenter.updateProgress("", progress.currentIndex, toDownload.size)
                    }
                }

                override fun onError(e: Throwable) {
                    presenter.createErrorAlert(
                        "Error downloading attachments",
                        "The following error occurred: ${e}",
                        {})
                }

            })
    }

    fun downloadAttachment(item: Item, downloadListener: AttachmentListAdapter.DownloadListener?) {
        if (isDownloading) {
            Log.d("zotero", "not downloading ${item.getTitle()} because i am already downloading.")
            return
        }
        isDownloading = true

        val downloadItem = zoteroAPI.downloadItemRx(item, zoteroDB.groupID, context)

        downloadItem
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<DownloadProgress> {
                override fun onSubscribe(d: Disposable) {

                }

                override fun onError(e: Throwable) {
                    isDownloading = false
                    presenter.hideDownloadProgress()

                    presenter.createErrorAlert("Error Downloading", e.toString()) {}

                    downloadListener?.onError(e.toString())
                }

                override fun onComplete() {
                    isDownloading = false
//                    MyLog.d("ZoteroDebug", "Downloading attachment ${item.getTitle()} complete!")

//                    presenter.hideDownloadProgress()

                    downloadListener?.onSuccess()
                }

                override fun onNext(t: DownloadProgress) {
//                    MyLog.d("ZoteroDebug", "Downloading attachment progress: ${t.progress} ")
//                    presenter.updateDownloadProgress(t.progress, t.total)

                    downloadListener?.onProgress((t.progress/1000).toInt(), (t.total/1000).toInt())
                }
            })
    }

    override fun loadLibrary() {
        zoteroDB = ZoteroDB(context, groupID = GroupInfo.NO_GROUP_ID)
        zoteroDB.collections = LinkedList()
        zoteroDB.loadItemsFromDatabase()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : CompletableObserver {
                override fun onComplete() {
                    calculateMetaInformation()
                    presenter.finishLoadingAnimation()

                    loadAttachments()
                }

                override fun onSubscribe(d: Disposable) {
                    presenter.displayLoadingAnimation()
                }

                override fun onError(e: Throwable) {
                    presenter.createErrorAlert(
                        "Error loading items",
                        "There was an error loading your items. " +
                                "Please verify that the library has synced fully first. Message: ${e.message}",
                        { presenter.displayErrorState() }
                    )
                }

            })
    }

    data class FilesystemMetadataObject(
        val fileName: String,
        val uri: Uri?,
        val exists: Boolean,
        val size: Long
    )

    fun stopCalculateMetaInformation() {
        calculateMetadataDisposable?.dispose()
    }

    var calculateMetadataDisposable: Disposable? = null
    fun calculateMetaInformation() {
        /* This method scans the local storage and determines what has already been downloaded on the device. */

        val attachmentItems =
            zoteroDB.items!!.filter { it.itemType == "attachment" && it.isDownloadable() }

        val observable = Observable.fromIterable(attachmentItems).map {
            return@map getAttachmentFileMeta(it) ?: FilesystemMetadataObject("", null, false, -1)
        }
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<FilesystemMetadataObject> {
                var totalSize: Long = 0
                var nAttachments = 0

                override fun onSubscribe(d: Disposable) {
                    presenter.displayLoadingAnimation()
                    calculateMetadataDisposable = d
                }

                override fun onNext(metadata: FilesystemMetadataObject) {
                    if (metadata.exists) {
                        nAttachments++
                        totalSize += metadata.size
                    }
                    presenter.displayAttachmentInformation(
                        nAttachments,
                        totalSize,
                        attachmentItems.size
                    )
                }

                override fun onError(e: Throwable) {
                    presenter.createErrorAlert("Error reading filesystem", e.toString(), {})
                    Log.e("zotero", e.stackTraceToString())
                    presenter.finishLoadingAnimation()
                }

                override fun onComplete() {
                    presenter.finishLoadingAnimation()
                }

            })

    }

     fun getAttachmentFileMeta(it: Item): FilesystemMetadataObject? {
        if (it.itemType != "attachment") {
            MyLog.e("zotero", "${it.itemKey} is not an attachment")
            return null
        }
//        Log.d("zotero", "checking if ${it.data["filename"]} exists")

        val fileName = attachmentStorageManager.getFilenameForItem(it)

        if (!it.isDownloadable() || it.data["linkMode"] == "linked_file") {
            return FilesystemMetadataObject("", null, false, -1)
        } else {
            val exists = attachmentStorageManager.checkIfAttachmentExists(it, false)
            val size = if (exists) {
                attachmentStorageManager.getFileSize(it)
            } else {
                -1
            }
            return FilesystemMetadataObject(fileName, null, exists, size)
        }
    }

    private val attachmentEntries = MutableLiveData<List<AttachmentEntry>>()

    fun getAttachmentItems(): LiveData<List<AttachmentEntry>> {
        return attachmentEntries
    }

    fun getAllAttachments(): List<Item> {
        return zoteroDB.items!!.filter { it.itemType == "attachment" }
    }

    fun loadAttachments() {
        CoroutineScope(Dispatchers.Default).launch {
            getAllAttachments().map {
                AttachmentEntry(it.getTitle(), attachmentStorageManager.getFilenameForItem(it), it.itemKey, it,
                    attachmentStorageManager.getAttachmentType(it))
            }.let {
                attachmentEntries.postValue(it)
            }
        }
    }

    fun isUseExternalPdfReader(): Boolean {
        return preferenceManager.isUseExternalPdfReader()
    }
}