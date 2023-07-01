package com.mickstarify.zotero.ui.PdfViewer.operate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.mickstarify.zotero.R
import com.mickstarify.zotero.databinding.FragmentPdfViewOperateBinding
import com.mickstarify.zotero.ui.PdfViewer.PdfViewerModel
import kotlin.math.roundToInt

class PdfViewOperateFragment : Fragment(), View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private lateinit var viewModel: PdfViewerModel

    private lateinit var mBinding: FragmentPdfViewOperateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(PdfViewerModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentPdfViewOperateBinding.inflate(inflater, container, false)

        mBinding.switchNightMode.isChecked = viewModel.nightMode.value!!
        mBinding.switchNightMode.setOnCheckedChangeListener(this)

        mBinding.switchScrollHorizon.isChecked = viewModel.scrollHorizontal.value!!
        mBinding.switchScrollHorizon.setOnCheckedChangeListener(this)

        mBinding.btnJump.setOnClickListener(this)

        initCurrentProgress()

        return mBinding.root
    }

    private fun initCurrentProgress() {

        mBinding.txtProgress.text = "当前页面: ${viewModel.pageCount}/${viewModel.pageCount}"

        // 即时跳转切换按钮
        mBinding.switchJumpLiveUpdate.isChecked = viewModel.isJumpToPageWhenScroll
        mBinding.switchJumpLiveUpdate.setOnCheckedChangeListener { _, isChecked ->
            viewModel.isJumpToPageWhenScroll = isChecked
        }

        val progress = viewModel.currentPage.toFloat() / viewModel.pageCount.toFloat()

        mBinding.seekbarProgress.progress = (progress * 100).roundToInt()
        mBinding.seekbarProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val pageIndex = ((progress / 100.0) * viewModel.pageCount).roundToInt()

                mBinding.txtProgress.text = "当前页面: $pageIndex/${viewModel.pageCount}"

                val isLiveUpdate = mBinding.switchJumpLiveUpdate.isChecked
                if (isLiveUpdate) {
                    viewModel.jumpToPage(pageIndex)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_jump -> jumpToPage()
        }
    }

    private fun jumpToPage() {
        val progress = mBinding.seekbarProgress.progress
        val pageIndex = ((progress / 100.0) * viewModel.pageCount).roundToInt()

        viewModel.jumpToPage(pageIndex)

    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when (buttonView?.id) {
            R.id.switch_night_mode -> viewModel.nightMode.value = isChecked
            R.id.switch_scroll_horizon -> viewModel.scrollHorizontal.value = isChecked
        }
    }

}