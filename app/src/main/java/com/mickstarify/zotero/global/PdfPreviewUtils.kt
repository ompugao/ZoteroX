package com.mickstarify.zotero.global

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler

import android.os.Looper
import android.util.Log
import android.util.LruCache
import android.widget.ImageView
import com.shockwave.pdfium.PdfDocument

import com.shockwave.pdfium.PdfiumCore
import java.lang.Exception
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future


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
    private val imageCache: ImageCache

    //线程池
    private var executorService: ExecutorService

    //线程任务集合（可用于取消任务）
    private var tasks: HashMap<String, Future<*>>

    /**
     * 从pdf文件中加载图片
     *
     * @param context     上下文
     * @param imageView   图片控件
     * @param pdfiumCore  pdf核心对象
     * @param pdfDocument pdf文档对象
     * @param pdfName     pdf文件名称
     * @param pageNum     pdf页码
     */
    fun loadBitmapFromPdf(
        context: Context?,
        imageView: ImageView?,
        pdfiumCore: PdfiumCore?,
        pdfDocument: PdfDocument?,
        pdfName: String,
        pageNum: Int
    ) {
        //判断参数合法性
        if (imageView == null || pdfiumCore == null || pdfDocument == null || pageNum < 0) {
            return
        }
        try {
            //缓存key
            val keyPage = pdfName + pageNum

            //为图片控件设置标记
            imageView.tag = keyPage
            Log.i("PreViewUtils", "加载pdf缩略图：$keyPage")

            //获得imageview的尺寸（注意：如果使用正常控件尺寸，太占内存了）
            /*int w = imageView.getMeasuredWidth();
            int h = imageView.getMeasuredHeight();
            final int reqWidth = w == 0 ? UIUtils.dip2px(context,100) : w;
            final int reqHeight = h == 0 ? UIUtils.dip2px(context,150) : h;*/

            //内存大小= 图片宽度 * 图片高度 * 一个像素占的字节数（RGB_565 所占字节：2）
            //注意：如果使用正常控件尺寸，太占内存了，所以此处指定四缩略图看着会模糊一点
            val reqWidth = 100
            val reqHeight = 150

            //从缓存中取图片
            val bitmap = imageCache.getBitmapFromLruCache(keyPage)
            bitmap?.let {
                imageView.setImageBitmap(it)
                return
            }

            //使用线程池管理子线程
            val future = executorService.submit {
                //打开页面（调用renderPageBitmap方法之前，必须确保页面已open，重要）
                pdfiumCore.openPage(pdfDocument, pageNum)

                //调用native方法，将Pdf页面渲染成图片
                val bm = Bitmap.createBitmap(reqWidth, reqHeight, Bitmap.Config.RGB_565)
                pdfiumCore.renderPageBitmap(pdfDocument, bm, pageNum, 0, 0, reqWidth, reqHeight)

                //切回主线程，设置图片
                if (bm != null) {
                    //将图片加入缓存
                    imageCache.addBitmapToLruCache(keyPage, bm)

                    //切回主线程加载图片
                    Handler(Looper.getMainLooper()).post {
                        if (imageView.tag.toString() == keyPage) {
                            imageView.setImageBitmap(bm)
                            Log.i("PreViewUtils", "加载pdf缩略图：$keyPage......已设置！！")
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