package com.mickstarify.zotero.ui.PdfViewer

import android.content.Intent
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.listener.OnTapListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mickstarify.zotero.R
import com.mickstarify.zotero.adapters.ItemPageAdapter
import com.mickstarify.zotero.databinding.PdfViewerFragmentBinding
import com.mickstarify.zotero.views.TabBottomSheetHelper
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.lifecycle.Observer
import com.mickstarify.zotero.LibraryActivity.ItemView.ItemBasicInfoFragment
import com.mickstarify.zotero.LibraryActivity.ItemView.ItemTagsFragment
import com.mickstarify.zotero.MyLog
import com.mickstarify.zotero.models.PdfAnnotation
import com.mickstarify.zotero.ui.PdfViewer.operate.PdfViewOperateFragment
import com.mickstarify.zotero.views.MaterialDialogHelper
import com.moyear.pdfview.view.MyPDFView
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

class PdfViewerFragment(private val pdfUri: Uri?,
                        private val attachmentKey: String?) : Fragment(), OnPageChangeListener, OnTapListener, OnLoadCompleteListener {

    private lateinit var viewModel: PdfViewerModel

    private lateinit var mBinding: PdfViewerFragmentBinding

    private lateinit var pdfView: MyPDFView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(PdfViewerModel::class.java)

        //初始化参数
        viewModel.initParams(attachmentKey, pdfUri)

//        Toast.makeText(requireContext(), "key: $attachmentKey", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = PdfViewerFragmentBinding.inflate(inflater, container, false)
        
        mBinding.btnBack.setOnClickListener {
            requireActivity().finish()
        }

        mBinding.txtPdfTitle.text = viewModel.getAttachmentName()
        mBinding.txtPdfTitle.setOnLongClickListener {
            Toast.makeText(requireContext(), viewModel.getAttachmentName(), Toast.LENGTH_SHORT).show()
            false
        }

        mBinding.btnMore.setOnClickListener { showMoreMenu(it) }

        mBinding.showContent.setOnClickListener { showPdfContents() }

        mBinding.fabMore.setOnClickListener { showOperateBottomDialog() }

        viewModel.showTools.observe(viewLifecycleOwner,
            { isShow ->
                if (isShow!!) {
                    showTools()
                } else {
                    hideTools()
                }
            })

        pdfView = mBinding.pdfView
        pdfView.enableAntialiasing(true)
        pdfView.isScrollbarFadingEnabled = true
//        cPDFView.isSwipeEnabled = true

        if (pdfUri != null) {
            pdfView.fromUri(pdfUri)
                .onPageChange(this)
                .onTap(this)
//                .swipeHorizontal(true)
                .onLoad(this)
                .load()
            viewModel.pageCount = pdfView.pageCount
        }

        viewModel.nightMode.observe(viewLifecycleOwner,
            { nightMode ->
                if (nightMode) {
                    pdfView.setNightMode(true)
                } else {
                    pdfView.setNightMode(false)
                }
            })


        //
        viewModel.bindPdfiumSDK(pdfView.getPdfiumSdk())
        viewModel.loadAttachmentAnnotations()


        viewModel.scrollHorizontal.observe(viewLifecycleOwner, {
            isHorizontal ->
            setScrollHorizontally(isHorizontal)
        })

        viewModel.jumpToPageObserver.observe(viewLifecycleOwner, {
            pageIndex ->
            pdfView.jumpTo(pageIndex, true)
        })

        return mBinding.root
    }

    private fun showOperateBottomDialog() {
        val tabs = listOf(
            ItemPageAdapter.TabItem("信息", ItemBasicInfoFragment.newInstance(viewModel.parentItem!!)),
            ItemPageAdapter.TabItem("标签", ItemTagsFragment.newInstance(viewModel.parentItem!!)),
        )

        val helper = TabBottomSheetHelper.get(this, tabs)
        helper.setTitle("更多")
        val dialog = helper.create()
        dialog.show()

    }

    private fun showMoreMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.inflate(R.menu.menu_fragment_pdf)

//        val dayNightItem = popupMenu.menu.findItem(R.id.day_night_mode)
//        if (viewModel.nightMode.value!!) {
//            dayNightItem.title = "日间模式"
//        } else {
//            dayNightItem.title = "夜间模式"
//        }

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.page_view -> showPdfViewConfigDialog()
//                R.id.day_night_mode -> switchDayNightMode()
                R.id.pdf_info -> showPdfInfo()
                R.id.open_pdf_external -> openPdfInOtherApp()
            }
            false
        }
        popupMenu.show()
    }

    private fun showPdfViewConfigDialog() {
        val tabs = listOf(
            ItemPageAdapter.TabItem("查看", PdfViewOperateFragment()),
//            ItemPageAdapter.TabItem("信息", ItemBasicInfoFragment.newInstance(viewModel.parentItem!!)),
//            ItemPageAdapter.TabItem("标签", ItemTagsFragment.newInstance(viewModel.parentItem!!)),
        )

        val helper = MaterialDialogHelper.get(this, tabs)
        helper.setTitle("视图布局")
        helper.setTabVisibility(false)
        val dialog = helper.create()
        dialog.show()

    }

    private fun switchDayNightMode() {
        viewModel.nightMode.value = !viewModel.nightMode.value!!
    }

    private fun showPdfInfo() {
        val builder = MaterialAlertDialogBuilder(requireContext())

        val documentMeta = mBinding.pdfView.documentMeta

        val infos =
            "附件名：${viewModel.getAttachmentName()}\n\n" +
            "文档名：${viewModel.getAttachmentFileName()}\n\n" +
                "Url：${pdfUri}\n\n" +
                "修改日期：${documentMeta.modDate}"

        builder.setTitle("文档信息")
        builder.setMessage(infos)
        builder.setPositiveButton("确定", null)
        builder.show()

    }

    private fun openPdfInOtherApp() {
        var intent = Intent(Intent.ACTION_VIEW)
        val uri = viewModel.pdfUri

//        MyLog.d("zotero", "opening PDF with Uri $uri")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent = Intent(Intent.ACTION_VIEW)
            intent.data = uri
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        } else {
            intent.setDataAndType(uri, "application/pdf")
            intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            intent = Intent.createChooser(intent, "Open File")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        }
        startActivity(intent)
    }

    private fun updateProgress(current: Int, total: Int) {
        mBinding.txtProgress.text = "$current/$total"

        viewModel.pageCount = total
        viewModel.currentPage = current
    }

    override fun onPageChanged(page: Int, pageCount: Int) {
        updateProgress(page, pageCount)
    }

    override fun onTap(e: MotionEvent?): Boolean {
        viewModel.showOrCollapseToolbar()

        val x = e?.x ?:0f
        val y = e?.y ?:0f

        extraText(x, y)

//        addAnnotation(x, y)
        return false
    }

    private fun addAnnotation(x: Float, y: Float) {
        val bound = Rect(197,376,552,383)
        viewModel.pdfiumSDK!!.openPage(0)
        viewModel.pdfiumSDK!!.addTextAnnotation(0, "fasfd", "#ffff0000", bound)
    }

    private fun extraText(x: Float, y: Float) {
        val pdfSdk = viewModel.pdfiumSDK

        val pdfFile = viewModel.pdfFile
        val mappedX: Float = -pdfView.currentXOffset + x
        val mappedY: Float = -pdfView.currentYOffset + y

        val page = viewModel.pdfFile?.getPageAtOffset(
            if (pdfView.isSwipeVertical) mappedY else mappedX,
            pdfView.zoom
        )

        val pageScaledSize = viewModel.pdfFile?.getScaledPageSize(page!!, pdfView.zoom)
        val pageX: Int
        val pageY: Int
        if (pdfView.isSwipeVertical) {
            pageX = pdfFile!!.getSecondaryPageOffset(page!!, pdfView.zoom).toInt()
            pageY = pdfFile.getPageOffset(page, pdfView.zoom).toInt()
        } else {
            pageY = pdfFile!!.getSecondaryPageOffset(page!!, pdfView.zoom).toInt()
            pageX = pdfFile.getPageOffset(page, pdfView.zoom).toInt()
        }

        val deviceX = x.roundToInt()
        val deviceY = y.roundToInt()

//        // pdfview的左上角位置
//        val startX = -pdfView.currentXOffset.toInt() - pageX
//        val startY = -pdfView.currentYOffset.toInt() - pageY
//
//        MyLog.e("ZoteroDebug", "startX: $startX, startY: $startY")
        MyLog.e("ZoteroDebug", "page:$page 未缩放的deviceX: $deviceX, 未缩放的deviceY: $deviceY")

//        MyLog.e("ZoteroDebug", "pageX: $pageX, pageY: ${pageY}")


        val pointF = pdfFile.mapDeviceCoordinateToPage(page, pageX, pageY, pageScaledSize!!.width.toInt(), pageScaledSize.height.toInt(), 0,  deviceX, deviceY)

//        val mapped = pdfFile.mapRectToDevice(
//            page, pageX, pageY,
//            pageSize!!.width.toInt(),
//            pageSize!!.height.toInt(), link.getBounds()
//        )
//        mapped.sort()

        val pos = pdfSdk!!.textGetCharIndexAtPos(page, pointF.x.toDouble(), pointF.y.toDouble(), 5.0, 5.0)
        MyLog.e("ZoteroDebug", "TextCharIndex: $pos, x: ${pointF.x}, y: ${pointF.y}")

        val rectF = RectF(0f, 0f, 100f, 100f)

        val extractText = pdfSdk!!.extractText(page, rectF)
        MyLog.e("ZoteroDebug", "extractText: $extractText")

    }

    private fun showTools() {
        mBinding.fabMore.show()

        mBinding.topToolbar.visibility = View.VISIBLE
        mBinding.topToolbar.animation = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_slide_down)

    }

    private fun hideTools() {
        mBinding.fabMore.hide()

        mBinding.topToolbar.animation = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_slide_up)
        mBinding.topToolbar.visibility = View.GONE
    }

    private fun showPdfContents() {
        val pdfView = mBinding.pdfView

        val tabs = arrayListOf(
            ItemPageAdapter.TabItem("缩略图", PdfThumbnailsFragment(pdfView)),
            ItemPageAdapter.TabItem("大纲", PdfContentsFragment(pdfView)),
            ItemPageAdapter.TabItem("注释", PdfAnnotationsFragment(pdfView))
        )

        val dialogHelper = TabBottomSheetHelper.get(this, tabs)
        dialogHelper.setTitle("目录")

        dialogHelper.create().show()

    }

    override fun loadComplete(nbPages: Int) {
        //获得文档书签信息
        viewModel.setContents(mBinding.pdfView.getTableOfContents())
        viewModel.loadPdfCore(mBinding.pdfView.getPdfFile())

        viewModel.pdfAnnotations.observe(viewLifecycleOwner,
            { annotations ->
                annotations?.forEach { annotation->

                    // position={"pageIndex":0,"rects":[[303.65,385.82,552.745,392.985],[197.234,376.238,552.717,383.404],[197.234,366.657,248.672,373.823]]}, type=highlight)

                    val jsonObject = JSONObject(annotation.position)
                    val pageIndex = jsonObject.get("pageIndex") as Int
                    val rects = jsonObject.get("rects") as JSONArray

//                    for (i in 0 until rects.length()) {
//                        val array = rects.getJSONArray(i)
//
//                        MyLog.e("ZoteroDebug", "pageIndex: $pageIndex  array: $array")
//
//                        array?.let {
//                            val left = it[0].toString().toFloat().roundToInt() ?:0
//                            val top = it[1].toString().toFloat().roundToInt() ?:0
//                            val right = it[2].toString().toFloat().roundToInt() ?:0
//                            val bottom = it[3].toString().toFloat().roundToInt() ?:0
//
//                            val bound = Rect(left, top, right, bottom)
//
//                            MyLog.e("ZoteroDebug", "bound: $bound")
//                            viewModel.pdfiumSDK!!.addTextAnnotation(pageIndex!!, annotation.text, annotation.color, bound)
//
//                            return@observe
//                        }
//                    }
                }
            })

//        MyLog.e("ZoteroDebug", "获取到的目录数量：${mBinding.pdfView.getTableOfContents()}")
    }

    fun setScrollHorizontally(isHorizontal: Boolean) {
        pdfView.setSwipeHorizon(isHorizontal)
//        pdfView.setSwipe
//        pdfView.isAnnotationRendering = false

    }


}