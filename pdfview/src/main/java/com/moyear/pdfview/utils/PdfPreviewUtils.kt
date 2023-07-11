package com.moyear.pdfview.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Handler

import android.os.Looper
import android.util.Log
import android.util.LruCache
import android.widget.ImageView
import com.blankj.utilcode.util.ConvertUtils
import org.benjinus.pdfium.PdfiumSDK
import java.lang.Exception
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.math.min


/**
 * 预览缩略图工具类
 *
 * 1、pdf页面转为缩略图
 * 2、图片缓存管理（仅保存到内存，可使用LruCache，注意空间大小控制）
 * 3、多线程管理（线程并发、阻塞、Future任务取消）
 *
 * 作者：齐行超
 * 日期：2019.08.08
 */
class PdfPreviewUtils private constructor() {

    /**
     * 获得图片缓存对象
     * @return 图片缓存
     */
    //图片缓存管理
    var imageCache: ImageCache
        private set

    //线程池
    private var executorService: ExecutorService

    //线程任务集合（可用于取消任务）
    private var tasks: HashMap<String, Future<*>>

    /**
     * 从pdf文件中加载图片
     *
     * @param context     上下文
     * @param imageView   图片控件
     * @param pdfiumSDK  pdf核心对象
//     * @param pdfDocument pdf文档对象
     * @param pdfName     pdf文件名称
     * @param pageNum     pdf页码
     */
    fun loadBitmapFromPdf(imageView: ImageView?,
        pdfiumSDK: PdfiumSDK?,
        pdfName: String,
        pageNum: Int
    ) {
        //判断参数合法性
        if (imageView == null || pdfiumSDK == null || pageNum < 0) {
            return
        }
        try {
            //缓存key
            val keyPage = pdfName + pageNum

            //为图片控件设置标记
            imageView.tag = keyPage
//            Log.i("PreViewUtils", "加载pdf第页${pageNum}的缩略图：$keyPage")

            val maxWidth = 200
            val maxHeight = 300

            //获得imageview的尺寸（注意：如果使用正常控件尺寸，太占内存了）
            val w = imageView.measuredWidth
            val h = imageView.measuredHeight
            val reqWidth = if (w == 0 || w > maxWidth) ConvertUtils.dp2px(100f) else w
            val reqHeight = if (h == 0 || h > maxHeight) ConvertUtils.dp2px(150f) else h

//            Log.i("PreViewUtils", "pdf缩略图宽：$reqWidth  高: $reqHeight")

            //内存大小= 图片宽度 * 图片高度 * 一个像素占的字节数（RGB_565 所占字节：2）
            //注意：如果使用正常控件尺寸，太占内存了，所以此处指定四缩略图看着会模糊一点
//            val reqWidth = 100
//            val reqHeight = 150

            //从缓存中取图片
            val bitmap = imageCache.getBitmapFromLruCache(keyPage)
            bitmap?.let {
                imageView.setImageBitmap(it)
                return
            }

            //使用线程池管理子线程
            val future = executorService.submit {
                //打开页面（调用renderPageBitmap方法之前，必须确保页面已open，重要）
                pdfiumSDK.openPage(pageNum)

                //调用native方法，将Pdf页面渲染成图片
                val bm = Bitmap.createBitmap(reqWidth, reqHeight, Bitmap.Config.RGB_565)
                pdfiumSDK.renderPageBitmap(bm, pageNum, 0, 0, reqWidth, reqHeight)

                //切回主线程，设置图片
                if (bm != null) {
                    //将图片加入缓存
                    imageCache.addBitmapToLruCache(keyPage, bm)

                    //切回主线程加载图片
                    Handler(Looper.getMainLooper()).post {
                        if (imageView.tag.toString() == keyPage) {
                            imageView.setImageBitmap(bm)
//                            Log.i("PreViewUtils", "加载pdf缩略图：$keyPage......已设置！！")
                        }
                    }
                }
            }

            //将任务添加到集合
            tasks[keyPage] = future
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun loadAnnotationThumbnail(imageView: ImageView?,
                                pdfiumSDK: PdfiumSDK?,
//                               pdfDocument: PdfDocument?,
                                fileName: String,
                                pageIndex: Int,
                                startX: Int,
                                startY: Int,
                                endX: Int,
                                endY:Int) {
        //判断参数合法性
        if (imageView == null || pdfiumSDK == null || pageIndex < 0) {
            return
        }

        val rect = Rect(startX, startY, endX, endY)

        //使用线程池管理子线程
        val future = executorService.submit {
            val bitmap = getAnnotationThumbnail(pdfiumSDK, fileName, pageIndex, rect)
            bitmap?.let {
                //切回主线程加载图片
                Handler(Looper.getMainLooper()).post {
                    imageView.setImageBitmap(it)
                }
            }
        }

        //缓存key
        val keyPage = "${fileName}_${pageIndex}_${rect}"
        //将任务添加到集合
        tasks[keyPage] = future
    }

    /**
     * 因为zotero中pdf批注的原点是从左下角开始的，所以需要先进行以下坐标转换
     * 然后才能进行
     */
    fun getAnnotationThumbnail(pdfiumSDK: PdfiumSDK?,
//                               pdfDocument: PdfDocument?,
                               fileName: String,
                               pageIndex: Int,
                               rect: Rect): Bitmap? {
        //判断参数合法性
        if (pdfiumSDK == null || pageIndex < 0) {
            return null
        }

        val minSize = 526

        val startX = rect.left
        val startY = rect.top
        val endX = rect.right
        val endY = rect.bottom

        var bitmap: Bitmap? = null

        try {
            //缓存key
            val keyPage = "${fileName}_${pageIndex}_${rect}"

            var scaleRatio =  minSize.toFloat() / min(endX - startX, endY - startY).toFloat()

            val reqWidth = ((endX - startX) * scaleRatio).toInt()
            val reqHeight = ((endY - startY) * scaleRatio).toInt()

            //从缓存中取图片
            bitmap = imageCache.getBitmapFromLruCache(keyPage)
            bitmap?.let {
                Log.d("PreViewUtils", "从缓存中获取图片:$keyPage")
                return it
            }

            //打开页面（调用renderPageBitmap方法之前，必须确保页面已open，重要）
//            pdfiumCore.openPage(pdfDocument, pageIndex)
            pdfiumSDK.openPage(pageIndex)

            val pageWidth = pdfiumSDK.getPageWidthPoint(pageIndex)
            val pageHeight = pdfiumSDK.getPageHeightPoint(pageIndex)

//            Log.d("PreViewUtils", "pdf宽：$pageWidth  高: $pageHeight")

            // 因为只是获取局部图像，所以要先进行偏移
            val drawStartX = (-startX * scaleRatio).toInt()
            val drawStartY = ((startY + endY - startY - pageHeight) * scaleRatio).toInt()

//            Log.i("PreViewUtils", "图宽：$reqWidth  高: $reqHeight， startX:$drawStartX, startY:$drawStartY")

            val scaledPageHeight = (pageHeight * scaleRatio).toInt()
            val scaledPageWidth = (pageWidth * scaleRatio).toInt()

            //调用native方法，将Pdf页面渲染成图片
            val bm = Bitmap.createBitmap(reqWidth, reqHeight, Bitmap.Config.RGB_565)
            pdfiumSDK.renderPageBitmap(bm, pageIndex, drawStartX, drawStartY, scaledPageWidth, scaledPageHeight)

            if (bm != null) {
                //将图片加入缓存
                imageCache.addBitmapToLruCache(keyPage, bm)
                bitmap = bm
            }
        } catch (e: Exception) {
            Log.e("PreViewUtils", "Error: $e")
        }
        return bitmap
    }

    /**
     * 取消从pdf文件中加载图片的任务
     *
     * @param keyPage 页码
     */
    fun cancelLoadBitmapFromPdf(keyPage: String?) {
        if (keyPage == null || !tasks.containsKey(keyPage)) {
            return
        }
        try {
            Log.i("PreViewUtils", "取消加载pdf缩略图：$keyPage")
            val future = tasks[keyPage]
            if (future != null) {
                future.cancel(true)
                Log.i("PreViewUtils", "取消加载pdf缩略图：$keyPage......已取消！！")
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    /**
     * 图片缓存管理
     */
    inner class ImageCache {
        //图片缓存
        private val lruCache: LruCache<String, Bitmap>?

        /**
         * 从缓存中取图片
         * @param key 键
         * @return 图片
         */
        @Synchronized
        fun getBitmapFromLruCache(key: String?): Bitmap? {
            return lruCache?.get(key)
        }

        /**
         * 向缓存中加图片
         * @param key 键
         * @param bitmap 图片
         */
        @Synchronized
        fun addBitmapToLruCache(key: String?, bitmap: Bitmap?) {
            if (getBitmapFromLruCache(key) == null) {
                if (lruCache != null && bitmap != null) lruCache.put(key, bitmap)
            }
        }

        /**
         * 清空缓存
         */
        fun clearCache() {
            lruCache?.evictAll()
        }

        //构造函数
        init {
            //初始化 lruCache
            //int maxMemory = (int) Runtime.getRuntime().maxMemory();
            //int cacheSize = maxMemory/8;
            val cacheSize = 1024 * 1024 * 30 //暂时设定30M

            lruCache = object : LruCache<String, Bitmap>(cacheSize) {
                override fun sizeOf(key: String?, value: Bitmap): Int {
                    return value.rowBytes * value.height
                }
            }
        }
    }

    companion object {
        /**
         * 单例（仅主线程调用，无需做成线程安全的）
         *
         * @return PreviewUtils实例对象
         */
        //单例
        private var instance: PdfPreviewUtils? = null

        @JvmStatic
        fun getInstance(): PdfPreviewUtils {
            if (instance == null) {
                instance = PdfPreviewUtils()
            }

            return instance!!
        }
    }

    /**
     * 默认构造函数
     */
    init {
        //初始化图片缓存管理对象
        imageCache = ImageCache()
        //创建并发线程池(建议最大并发数大于1屏grid item的数量)
        executorService = Executors.newFixedThreadPool(20)
        //创建线程任务集合，用于取消线程执行
        tasks = HashMap()
    }
}