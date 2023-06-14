package com.mickstarify.zotero.adapters

import android.graphics.Color
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.mickstarify.zotero.R
import com.mickstarify.zotero.models.PdfAnnotation

class PdfAnnotationAdapter: BaseQuickAdapter<PdfAnnotation, BaseViewHolder>(R.layout.item_pdf_annotation, null) {

    var onAnnotationNavigateListener: OnAnnotationNavigateListener? = null

    override fun convert(holder: BaseViewHolder, item: PdfAnnotation) {

        val txtAnnoText = holder.getView<TextView>(R.id.txt_annotation_text)
        val txtAnnoPage = holder.getView<TextView>(R.id.txt_annotation_page)
        val txtAnnoComment = holder.getView<TextView>(R.id.txt_annotation_comment)

        val colorIndicator = holder.getView<LinearLayout>(R.id.color_indicator)

        val annotationColor = Color.parseColor(item.color)

        txtAnnoPage.text = "页 ${item.pageLabel}"
        txtAnnoText.text = item.text
        txtAnnoComment.text = item.comment

//            txtAnnoText.setTextColor(annotationColor)
        colorIndicator.setBackgroundColor(annotationColor)

        val cardView = holder.getView<CardView>(R.id.item_view)
        cardView.setOnClickListener { onAnnotationNavigateListener?.onNavigate(item) }

    }

    interface OnAnnotationNavigateListener {
        fun onNavigate(annotation: PdfAnnotation)
    }

}