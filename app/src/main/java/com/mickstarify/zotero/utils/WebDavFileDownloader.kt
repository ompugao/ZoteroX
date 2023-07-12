package com.mickstarify.zotero.utils

import com.mickstarify.zotero.MyLog
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors

class WebDavFileDownloader(
    sardine: OkHttpSardine,
    private val url: String,
    private val outputFilePath: String
) {

    val client: OkHttpClient = sardine.client

    private var numThreads: Int = 3

    private val executor = Executors.newFixedThreadPool(numThreads)
    private var blockSize: Long = -1

    private val downloadedBlocks = mutableListOf<File>()

    var downloadListener: DownloadListener ?= null

    var downloadStatus = HashMap<Int, DownStatus>()

    private var fileSize = -1L

    /**
     * 一个链接最大下载的文件长度
     */
    private var fileBlockSize = 1048576

    data class DownStatus(var status: Int, var progress: Long) {
        companion object {
            const val UNSTART = 0
            const val DOWNLOADING = 1
            const val FINISH = 2
        }
    }

    private fun init() {
        // 发送初始请求以获取文件的大小
        val request = Request.Builder()
            .url(url)
            .addHeader("Accept-Encoding", "identity") //添加这句话避免gzip压缩导致在Response中获取不到Content-Length属性
            .build()
        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            MyLog.e("ZoteroDebug", "Request failed: ${response.code} ${response.message}")
            return
        }

        fileSize = response.body?.contentLength() ?: 0
        response.close()

        blockSize = fileSize / numThreads

        for (i in 0 until numThreads) {
            downloadStatus[i] = DownStatus(DownStatus.UNSTART, 0)
        }

    }

    fun startDownload() {
        init()

        var startByte = 0L
        var endByte = blockSize - 1

        MyLog.d("ZoteroDebug", "Start to download webdav file: ${url}, filesize: $fileSize download by every block: ${blockSize}")

        for (i in 0 until numThreads) {
            if (i == numThreads - 1) {
                endByte = Long.MAX_VALUE // 最后一个线程下载剩余的字节
            }

            downloadStatus[i]?.status = DownStatus.DOWNLOADING

            val downloadThread = DownloadThread(i, url, startByte, endByte)
            executor.execute(downloadThread)

            startByte += blockSize
            endByte += blockSize
        }

        executor.shutdown()
    }

    private inner class DownloadThread(
        private val num: Int,
        private val url: String,
        private val startByte: Long,
        private val endByte: Long
    ) : Runnable {
        override fun run() {
            val request = Request.Builder()
                .url(url)
                .header("Range", "bytes=$startByte-$endByte")
                .build()

            try {
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    MyLog.e("ZoteroDebug", "Request failed: ${response.code} ${response.message}")
                    return
                }

                val responseBody = response.body
                responseBody?.byteStream()?.use { inputStream ->
                    val fileBlock = File("$outputFilePath.part${startByte / blockSize}")
                    val fileOutputStream = FileOutputStream(fileBlock)
                    val buffer = ByteArray(1024)
                    var bytesRead: Int

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        fileOutputStream.write(buffer, 0, bytesRead)

                        synchronized(downloadStatus) {
                            downloadStatus[num]?.let {
                                it.progress +=bytesRead
                            }

                            var progress = 0L
                            for ((_, value) in downloadStatus) {
                                progress += value.progress
                            }
//                            MyLog.d("ZoteroDebug", "Downloading progress: ${progress}/${fileSize}, write file block: ${outputFile.name}, ")
                            downloadListener?.onDownload(progress, fileSize)
                        }

                    }
                    fileOutputStream.close()

                    MyLog.d("ZoteroDebug", "Thread $num download completed: bytes $startByte-$endByte")

                    downloadStatus[num]?.status = DownStatus.FINISH

                    synchronized(downloadedBlocks) {
                        downloadedBlocks.add(fileBlock)
                        if (downloadedBlocks.size == numThreads) {
                            mergeFiles()
                        }
                    }
                }
                response.close()
            } catch (e: Exception) {
                e.printStackTrace()
                downloadListener?.onFail(e)
            }
        }
    }

    private fun mergeFiles() {
        val outputFile = File(outputFilePath)
        val fileOutputStream = FileOutputStream(outputFile)

        try {
            for (blockFile in downloadedBlocks) {
                blockFile.inputStream().use { inputStream ->
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        fileOutputStream.write(buffer, 0, bytesRead)
                    }
                }
                blockFile.delete()
                MyLog.d("ZoteroDebug", "File block ${blockFile.path} merge to ${outputFile.name} completed and deleted.")
            }

            var notFinish = false
            for ((_, value ) in downloadStatus) {
                if (value.status != DownStatus.FINISH) {
                    notFinish = true
                }
            }

            if (!notFinish) {
                MyLog.d("ZoteroDebug", "Merge file blocks completed: $outputFilePath")
                downloadListener?.onComplete(outputFile)

                executor.shutdown()
            }

        } catch (e: Exception) {
            downloadListener?.onFail(e)
            e.printStackTrace()
        } finally {
            fileOutputStream.close()
        }
    }

    interface DownloadListener {
        fun onDownload(progress: Long, total: Long)

        fun onComplete(file: File?)

        fun onFail(e: Exception)
    }
}