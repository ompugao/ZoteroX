package com.mickstarify.zotero.ZoteroAPI

import android.content.Context
import android.util.Log
import com.mickstarify.zotero.ZoteroStorage.AttachmentStorageManager
import com.mickstarify.zotero.ZoteroStorage.Database.Item
import com.mickstarify.zotero.utils.WebDavFileDownloader
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import net.lingala.zip4j.ZipFile
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.io.InputStream
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


class Webdav(
    address: String,
    val username: String,
    val password: String,
    val allowInsecureSSL: Boolean = false
) {
    var sardine: OkHttpSardine
    var address: String

    private val DOWNLOAD_THREAD = 4

//    private val downloadManager = DownloadManager.getInstance()

    fun testConnection(): Boolean {
        return sardine.exists(address)
    }

    fun downloadPropToString(webpath: String): String {
        val reader = sardine.get(webpath).source()
        val bufferedReader = reader.buffer()
        val s = bufferedReader.readUtf8()
        bufferedReader.close()
        return s
    }

    fun downloadFileRx(
        attachment: Item,
        context: Context,
        attachmentStorageManager: AttachmentStorageManager
    ): Observable<DownloadProgress> {
        val webpathProp = address + "/${attachment.itemKey.uppercase(Locale.ROOT)}.prop"
        val webpathZip = address + "/${attachment.itemKey.uppercase(Locale.ROOT)}.zip"

        val observable: Observable<DownloadProgress> = Observable.create { emitter ->
            try {
                val propString = downloadPropToString(webpathProp)
                val prop = WebdavProp(propString)

                val tempFile = attachmentStorageManager.createTempFile("${attachment.itemKey.uppercase(Locale.ROOT)}.zip")

                val downloadListener = object: WebDavFileDownloader.DownloadListener {
                    override fun onDownload(progress: Long, total: Long) {
                        emitter.onNext(DownloadProgress(progress, total, prop.mtime, prop.hash))
                    }

                    override fun onComplete(file: File?) {
                        file?.let {
                            val zipFile2 = ZipFile(it)
                            if (!zipFile2.isValidZipFile) {
                                throw RuntimeException("${file.name} is not a valid zip file.")
                            }

                            val attachmentFilename =
                                zipFile2.fileHeaders.firstOrNull()?.fileName
                                    ?: throw RuntimeException("Error empty zipfile.")

                            zipFile2.extractAll(context.cacheDir.absolutePath)
                            it.delete() // don't need this anymore.
                            attachmentStorageManager.writeAttachmentFromFile(
                                File(
                                    context.cacheDir,
                                    attachmentFilename
                                ), attachment
                            )
                            File(context.cacheDir, attachmentFilename).delete()
                            emitter.onComplete()
                        }
                    }

                    override fun onFail(e: Exception) {
                        emitter.onError(e)
                    }
                }

                val downloader = WebDavFileDownloader(sardine, webpathZip, tempFile.path)
                downloader.downloadListener = downloadListener
                downloader.startDownload()

            } catch (e: Exception) {
                Log.e("zotero", "big exception hit ${e} || ${emitter.isDisposed}")
                // this your brain on rxjava. this is to avoid a undeliverable crash on dispose().
                if (!emitter.isDisposed) {
                    throw e
                }
            }
        }
        return observable
    }

    fun uploadAttachment(
        attachment: Item,
        attachmentStorageManager: AttachmentStorageManager
    ): Completable {
        /*Uploading will take 3 steps,
        * 1. Compress attachment into a ZIP file (using internal cache dir)
        * 2. Create  F3FXJF_NEW.prop file.
        * 3. Upload to webdav server F3FXJF_NEW.zip and F3FXJF_NEW.prop
        * 4. send a delete request and  rename request to server so we have
        *  F3FXJF.zip + F3FXJF.prop resulting*/

        return Completable.fromAction {
            // STEP 1 CREATE ZIP
            val fileInputStream =
                attachmentStorageManager.getItemInputStream(attachment).source()
            val filename = attachmentStorageManager.getFilenameForItem(attachment)
            val tempFile = attachmentStorageManager.createTempFile(filename)
            val sinkBuffer = tempFile.outputStream().sink().buffer()
            sinkBuffer.writeAll(fileInputStream)
            sinkBuffer.close()
            fileInputStream.close()

            val zipFile =
                attachmentStorageManager.createTempFile("${attachment.itemKey.toUpperCase(Locale.ROOT)}_NEW.zip")

            ZipFile(zipFile).addFile(tempFile)
            tempFile.delete()
            // STEP 2 -- CREATE PROP
            val propFile =
                attachmentStorageManager.createTempFile("${attachment.itemKey.toUpperCase(Locale.ROOT)}_NEW.prop")
            val propContent = WebdavProp(
                attachmentStorageManager.getMtime(attachment),
                attachmentStorageManager.calculateMd5(attachment)
            ).serialize()
            val outputStream = propFile.outputStream()
            outputStream.write(propContent.toByteArray(Charsets.UTF_8))
            outputStream.close()

            // STEP 3 -- UPLOAD ZIP AND PROP
            val newZipPath = address + "/${attachment.itemKey.toUpperCase(Locale.ROOT)}_NEW.zip"
            val newPropPath = address + "/${attachment.itemKey.toUpperCase(Locale.ROOT)}_NEW.prop"

            // mostly useless step, delete any old _NEW.zip files that shouldn't exist
            // but might incase of a failed update earlier.
            if (sardine.exists(newZipPath)) {
                sardine.delete(newZipPath)
            }
            if (sardine.exists(newPropPath)) {
                sardine.delete(newPropPath)
            }

            // upload files
            sardine.put(newPropPath, propFile, "text/plain")
            sardine.put(newZipPath, zipFile, "application/zip")

            zipFile.delete()
            propFile.delete()

            // STEP 4 -- DELETE AND RENAME TO REPLACE OLD CONTENT.
            val zipPath = address + "/${attachment.itemKey.toUpperCase(Locale.ROOT)}.zip"
            val propPath = address + "/${attachment.itemKey.toUpperCase(Locale.ROOT)}.prop"

            sardine.delete(propPath)
            sardine.delete(zipPath)

            sardine.move(newPropPath, propPath)
            sardine.move(newZipPath, zipPath)

            // DONE.
        }

    }

    init {
        val clientBuilder = OkHttpClient.Builder()
            .protocols(Collections.singletonList(Protocol.HTTP_1_1))
            .callTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS);

        if (allowInsecureSSL){
            val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {
                    @Throws(CertificateException::class)
                    override fun checkClientTrusted(
                        chain: Array<X509Certificate>,
                        authType: String
                    ) {
                    }

                    @Throws(CertificateException::class)
                    override fun checkServerTrusted(
                        chain: Array<X509Certificate>,
                        authType: String
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return arrayOf()
                    }
                }
            )
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            val sslSocketFactory = sslContext.socketFactory
            clientBuilder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            clientBuilder.hostnameVerifier(HostnameVerifier { hostname, session -> true })
        }

        sardine = OkHttpSardine(
            clientBuilder.build()
        )

        if (username != "" && password != "") {
            sardine.setCredentials(username, password, true)
        }

        this.address = if (address.endsWith("/zotero")) {
            address
        } else {
            if (address.endsWith("/")) { // so we don't get server.com//zotero
                address + "zotero"
            } else {
                address + "/zotero"
            }
        }
    }



}