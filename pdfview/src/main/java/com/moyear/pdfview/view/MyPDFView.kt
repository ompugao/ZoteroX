package com.moyear.pdfview.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.model.LinkTapEvent
import com.moyear.pdfview.core.PdfiumSDK

class MyPDFView(context: Context, set: AttributeSet?) : PDFView(context, set) {

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        return super.onTouchEvent(event)
    }

    override fun setNightMode(nightMode: Boolean) {
        super.setNightMode(nightMode)
        //立即刷新视图
        invalidate()
    }


}