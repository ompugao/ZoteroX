package com.mickstarify.zotero.ui.PdfViewer

import android.content.Intent
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
import com.mickstarify.zotero.MyLog
import com.mickstarify.zotero.R
import com.mickstarify.zotero.adapters.ItemPageAdapter
import com.mickstarify.zotero.databinding.PdfViewerFragmentBinding
import com.mickstarify.zotero.views.TabBottomSheetHelper
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.github.barteksc.pdfviewer.PDFView
import com.moyear.pdfview.view.MyPDFView

class PdfViewerFragment(private val pdfUri: Uri?,
                        private val attachmentKey: String?) : Fragment(), OnPageChangeListener, OnTapListener, OnLoadCompleteListener {

    private lateinit var viewModel: PdfViewerModel

    private lateinit var mBinding: PdfViewerFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(PdfViewerModel::class.java)

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

        viewModel.showTools.observe(viewLifecycleOwner,
            { isShow ->
                if (isShow!!) {
                    showTools()
                } else {
                    hideTools()
                }
            })

        val cPDFView: PDFView = mBinding.pdfView
        cPDFView.enableAntialiasing(true)
        cPDFView.isScrollbarFadingEnabled = true
//        cPDFView.isSwipeEnabled = true

        if (pdfUri != null) {
            cPDFView.fromUri(pdfUri)
                .onPageChange(this)
                .onTap(this)
                .onLoad(this)
                .load()
            viewModel.pageCount = cPDFView.pageCount
        }

        viewModel.nightMode.observe(viewLifecycleOwner,
            { nightMode ->
                if (nightMode) {
                    cPDFView.setNightMode(true)
                } else {
                    cPDFView.setNightMode(false)
                }
            })

        return mBinding.root
    }

    private fun showMoreMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.inflate(R.menu.menu_fragment_pdf)

        val dayNightItem = popupMenu.menu.findItem(R.id.day_night_mode)
        if (viewModel.nightMode.value!!) {
            dayNightItem.title = "日间模式"
        } else {
            dayNightItem.title = "夜间模式"
        }

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.day_night_mode -> switchDayNightMode()
                R.id.pdf_info -> showPdfInfo()
                R.id.open_pdf_external -> openPdfInOtherApp()
            }
            false
        }
        popupMenu.show()
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
        viewModel.showTools.value = !viewModel.showTools.value!!

        return false
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


//        MyLog.e("ZoteroDebug", "获取到的目录数量：${mBinding.pdfView.getTableOfContents()}")
    }



}