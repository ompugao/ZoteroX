package com.mickstarify.zotero.adapters

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.mickstarify.zotero.R
import com.mickstarify.zotero.models.PdfAnnotation
import com.moyear.pdfview.utils.PdfPreviewUtils
import org.benjinus.pdfium.PdfiumSDK
import org.json.JSONObject
import kotlin.math.roundToInt

class PdfAnnotationAdapter(private val pdfiumSDK: PdfiumSDK?,
//                           private val pdfDocument: PdfDocument?,
                           private val fileName: String): BaseQuickAdapter<PdfAnnotation, BaseViewHolder>(R.layout.item_pdf_annotation, null) {

    var onAnnotationNavigateListener: OnAnnotationNavigateListener? = null

    override fun convert(holder: BaseViewHolder, item: PdfAnnotation) {

        val txtAnnoText = holder.getView<TextView>(R.id.txt_annotation_text)
        val txtAnnoPage = holder.getView<TextView>(R.id.txt_annotation_page)
        val txtAnnoComment = holder.getView<TextView>(R.id.txt_annotation_comment)

        val colorIndicator = holder.getView<LinearLayout>(R.id.color_indicator)

        val imgAnnotationThumbnail = holder.getView<ImageView>(R.id.img_annotation_thumbnail)

        val annotationColor = Color.parseColor(item.color)

        txtAnnoPage.text = "页 ${item.pageLabel}"
        txtAnnoText.text = item.text
        txtAnnoComment.text = item.comment

        when (item.type) {
            "image" -> {
                showImageAnnotation(imgAnnotationThumbnail, item)
            }
            else -> {
                imgAnnotationThumbnail.visibility = View.GONE
            }
        }

        colorIndicator.setBackgroundColor(annotationColor)

        val cardView = holder.getView<CardView>(R.id.item_view)
        cardView.setOnClickListener { onAnnotationNavigateListener?.onNavigate(item) }

    }

    private fun showImageAnnotation(imgAnnotationThumbnail: ImageView, item: PdfAnnotation) {
        imgAnnotationThumbnail.visibility = View.VISIBLE

        val jsonObject = JSONObject( item.position)

//      position={"pageIndex":11,"rects":[[26.954000000000004,459.23799999999994,310.925,734.0974719387756]]}

        var pageIndex = 0
        var startX = 0
        var startY = 0
        var endX = 0
        var endY = 0

        try {
            pageIndex = jsonObject.getInt("pageIndex")
            val posStr = jsonObject.getString("rects").replace("[", "").replace("]", "")
            if (!posStr.isNullOrEmpty()) {
                val group = posStr.split(",")
                startX = group[0].toDouble().roundToInt()
                startY = group[1].toDouble().roundToInt()
                endX = group[2].toDouble().roundToInt()
                endY = group[3].toDouble().roundToInt()
            }
        } catch (e: Exception) {
            return
        }

//        MyLog.e("ZoteroDebug", "pageIndex: $pageIndex, $startX, $startY, $endX, $endY")

        PdfPreviewUtils.getInstance().loadAnnotationThumbnail(imgAnnotationThumbnail, pdfiumSDK,
//            pdfDocument,
            fileName, pageIndex, startX, startY, endX, endY)
    }

    interface OnAnnotationNavigateListener {
        fun onNavigate(annotation: PdfAnnotation)
    }

}