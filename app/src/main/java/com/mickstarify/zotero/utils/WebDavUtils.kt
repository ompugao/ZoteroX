package com.mickstarify.zotero.utils

import com.mickstarify.zotero.MyLog
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.closeQuietly
import okhttp3.internal.notifyAll
import okhttp3.internal.wait
import okio.Buffer
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.RandomAccessFile
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.math.ceil

class WebDavUtils {

    interface DownloadListener {
        fun onDownload(progress: Long, total: Long)
    }

    /**
     * 多线程下载文件，调用方需判断是否适合多线程分片下载哦
     */
    fun downloadFileMultiThread(sardine: OkHttpSardine, url: String, outputPath: String, downloadlistener: DownloadListener, invoking: Function2<File?, String?, Unit>) {
        val contentLength = sardine.getContentLength(url)
        if (contentLength == -1L) return
        val file = File(outputPath) ?: return

        MyLog.e("ZoteroDebug", "start contentLength $contentLength")

        if (contentLength > 25 * 1024 * 1024) {
            val accessFile = RandomAccessFile(file.name, "rw")
            val singleDownloadLength = contentLength / 3L
            val count = ceil((contentLength / singleDownloadLength.toFloat())).toInt()

            val lock = ReentrantLock()
            val runCount = AtomicInteger(0) // 执行次数
            val writeCount = AtomicLong()

            val complete = {
                val get = runCount.get()

                MyLog.e("ZoteroDebug", "task#$get has already download $writeCount")

                if (get == count) {
                    runCount.set(0)
                    if (writeCount.get() == contentLength) {
                        invoking.invoke(File(outputPath), null)
                    } else {
                        invoking.invoke(null, "error: download $get with $contentLength")
                    }
                }
            }

            var start = 0L
            var end = 0L

            for (i in 0 until count) {
                // 后面可换成线程池
                end = start + singleDownloadLength
                if (end > contentLength) {
                    end = contentLength
                }

                startThread(LongRange(start, end)) { range ->

                    MyLog.e("ZoteroDebug", "start thread: $start $end")

                    val buffer = Buffer()
                    val obj = object : OutputStream() {
                        override fun write(b: Int) {
                        }

                        override fun write(b: ByteArray, off: Int, len: Int) {
                            buffer.write(b, off, len)

                            downloadlistener.onDownload(writeCount.toLong(), contentLength)
                        }
                    }

                    rangeDownloadAndSaveToFile(sardine, url, range.first, range.last, obj, {
//                        val doWriteToFile = {
//                            accessFile.seek(it.first)
//                            writeToAccessFile(buffer, accessFile)
//                            synchronized(accessFile) {
//                                accessFile.notifyAll()
//                            }
//
//                            writeCount.getAndAdd(it.last - it.first)
//                            runCount.incrementAndGet()
//                            lock.unlock()
//                            complete()
//                        }

                        // 下载成功
                        if (lock.tryLock()) {
//                            doWriteToFile()

                            accessFile.seek(it.first)
                            writeToAccessFile(buffer, accessFile)
                            synchronized(accessFile) {
                                accessFile.notifyAll()
                            }

                            writeCount.getAndAdd(it.last - it.first)
                            runCount.incrementAndGet()
                            lock.unlock()
                            complete()
                        } else {
                            synchronized(accessFile) {
                                (accessFile as Any).wait()

                                if (lock.tryLock()) {
//                                    doWriteToFile()

                                    accessFile.seek(it.first)
                                    writeToAccessFile(buffer, accessFile)
                                    synchronized(accessFile) {
                                        accessFile.notifyAll()
                                    }
                                    writeCount.getAndAdd(it.last - it.first)
                                    runCount.incrementAndGet()
                                    lock.unlock()
                                    complete()
                                }
                            }
                        }
                    }, {
                        runCount.incrementAndGet()
                    })
                }
                start = end
            }
        } else {
            downloadFile(sardine, url, outputPath) {
                invoking.invoke(it, null)
            }
        }
    }

    /**
     * @param url 远程路径
     * @param filePath 文件路径
     */
    private fun downloadFile(sardine: OkHttpSardine, url: String, filePath: String, invoking: Function1<File?, Unit>) {
        if (url.isEmpty() || filePath.isEmpty()) return;
        val request = Request.Builder().url(url).build();

        val call = sardine.client.newCall(request)
        val outputFile = File(filePath) ?: return;

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                invoking.invoke(null)
            }

            override fun onResponse(call: Call, response: Response) {
                val code = response.code
                if (code == 200) {
                    val body = response.body
                    val byteStream = body?.byteStream()
                    if (byteStream == null) {
                        invoking.invoke(null)
                        return
                    }
                    val contentLength = body.contentLength()
                    saveInputStreamToFie(byteStream, contentLength, outputFile.outputStream(), true)
                    invoking.invoke(outputFile)
                } else {
                    invoking.invoke(null)
                }

            }
        })
    }


    private fun startThread(range: LongRange, invoking: Function1<LongRange, Unit>) {
        thread {
            invoking.invoke(range)
        }
    }

    private fun rangeDownloadAndSaveToFile(
        sardine: OkHttpSardine,
        url: String,
        start: Long,
        end: Long,
        outputStream: OutputStream,
        success: Function1<LongRange, Unit>,
        error: Function1<LongRange, Unit>
    ) {
        if (start < 0 && end < 0 || start > end) {
            error.invoke(LongRange(start, end))
            return
        }

        val build = Request.Builder().url(url)
            .addHeader("Range", "bytes=$start-$end")
            .build()

        sardine.client.newCall(build)
            .enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    val code = response.code
                    MyLog.e("ZoteroDebug", "rangeDownloadAndSaveToFile $start $end code:$code")

                    if (code == 200 || code == 206) {
                        val byteStream = response.body?.byteStream() ?: return
                        saveInputStreamToFie(byteStream, end - start, outputStream, false)
                        success.invoke(LongRange(start, end))
                    } else {
                        error.invoke(LongRange(start, end))
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    error.invoke(LongRange(start, end))
                }
            })
    }

    private fun saveInputStreamToFie(
        inputStream: InputStream,
        contentLength: Long,
        outputStream: OutputStream,
        printProgress: Boolean = false
    ) {

        var writeCount = 0L // 写入文件的数量
        val fileSink = outputStream.sink().buffer()
        val bufferedSource = inputStream.source()
        var lastProgress = 0.0f

        val readCount = 64 * 1024L // 512kb
        val writeCache = Buffer()

        try {
            while (true) {
                val read = bufferedSource.read(writeCache, readCount)
                if (read <= 0) break
                fileSink.write(writeCache, read)
                writeCount += read

                if (printProgress && contentLength > 0) {
                    val progress = writeCount / contentLength.toFloat() * 100
                    if (progress - lastProgress > 1) {
                        MyLog.e("ZoteroDebug", "download percent: $progress")
                        lastProgress = progress
                    }
                }

                if (writeCount == contentLength) {
                    break
                }
            }
        } catch (ex: Throwable) {
            ex.printStackTrace()
        } finally {
            bufferedSource.closeQuietly()
            fileSink.closeQuietly()
        }
    }

    private fun writeToAccessFile(buffer: Buffer, accessFile: RandomAccessFile) {
        val byteArray = ByteArray(8192)
        while (true) {
            val read = buffer.read(byteArray)
            if (read == -1) {
                break
            } else {
                accessFile.write(byteArray, 0, read)
            }
        }
    }

}