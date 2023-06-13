package com.mickstarify.zotero.adapters

import android.content.Context

import android.widget.TextView

import android.view.LayoutInflater
import android.view.View

import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.mickstarify.zotero.R
import com.moyear.pdfview.utils.PdfPreviewUtils
import com.shockwave.pdfium.PdfDocument
import com.shockwave.pdfium.PdfiumCore
import java.lang.Exception

/**
 * grid列表适配器
 * 作者：齐行超
 * 日期：2019.08.08
 */
class PdfThumbnailAdapter(
    private val context: Context,
    private val pdfiumCore: PdfiumCore,
    private val pdfDocument: PdfDocument,
    pdfName: String,
    totalPageNum: Int
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var pdfName: String = pdfName
    private var totalPageNum: Int = totalPageNum

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.item_pdf_thumbnail, parent, false)
        return GridViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as GridViewHolder

        //设置PDF图片
        PdfPreviewUtils.getInstance().loadBitmapFromPdf(
            context,
            holder.imgThumbnail,
            pdfiumCore,
            pdfDocument,
            pdfName,
            position
        )
        //设置PDF页码
        holder.txtPageNum.text = position.toString()
        //设置Grid事件
        holder.imgThumbnail.setOnClickListener {
            delegate?.onGridItemClick(position)
        }
        return
    }

    override fun onViewDetachedFromWindow(viewHolder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(viewHolder)

        val holder = viewHolder as GridViewHolder
        try {
            //item不可见时，取消任务
            holder.imgThumbnail.let {
                PdfPreviewUtils.getInstance().cancelLoadBitmapFromPdf(it.tag.toString())
            }
            //item不可见时，释放bitmap  (注意：本Demo使用了LruCache缓存来管理图片，此处可注释掉)
            /*Drawable drawable = holder.iv_page.getDrawable();
            if (drawable != null) {
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                    bitmap = null;
                    Log.i("PreViewUtils","销毁pdf缩略图："+holder.iv_page.getTag().toString());
                }
            }*/
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return totalPageNum
    }

    internal inner class GridViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgThumbnail: ImageView = itemView.findViewById(R.id.img_thumbnail)
        var txtPageNum: TextView = itemView.findViewById(R.id.txt_page)

    }

    /**
     * 接口：Grid事件
     */
    interface GridEvent {
        /**
         * 当选择了某Grid项
         * @param position tree节点数据
         */
        fun onGridItemClick(position: Int)
    }

    /**
     * 设置Grid事件
     * @param event Grid事件对象
     */
    fun setGridEvent(event: GridEvent?) {
        delegate = event
    }

    //Grid事件委托
    private var delegate: GridEvent? = null
}
