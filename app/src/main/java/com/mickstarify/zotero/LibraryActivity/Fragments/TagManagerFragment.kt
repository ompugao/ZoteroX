package com.mickstarify.zotero.LibraryActivity.Fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mickstarify.zotero.LibraryActivity.LibraryActivityModel
import com.mickstarify.zotero.LibraryActivity.ViewModels.LibraryListViewModel
import com.mickstarify.zotero.LibraryActivity.ViewModels.TagManagerViewModel
import com.mickstarify.zotero.MyLog
import com.mickstarify.zotero.adapters.TagListAdapter
import com.mickstarify.zotero.databinding.DialogTagColorPickerBinding
import com.mickstarify.zotero.databinding.FragmentTagManagerBinding
import com.skydoves.colorpickerview.listeners.ColorListener
import kotlinx.coroutines.*

class TagManagerFragment : Fragment {

    private constructor() {}

    private lateinit var mBinding: FragmentTagManagerBinding

    private lateinit var libraryActivityModel: LibraryActivityModel

    private lateinit var libraryListModel: LibraryListViewModel

    private lateinit var viewModel: TagManagerViewModel

    private lateinit var adapter: TagListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        libraryActivityModel = ViewModelProvider(requireActivity()).get(LibraryActivityModel::class.java)

        libraryListModel = ViewModelProvider(requireActivity()).get(LibraryListViewModel::class.java)

        viewModel = ViewModelProvider(requireActivity()).get(TagManagerViewModel::class.java)

        mBinding = FragmentTagManagerBinding.inflate(inflater)

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getMutableTags().observe(viewLifecycleOwner) { itemTags ->
            itemTags
                .map { viewModel.convertTag(it) }
                .let {
                    viewModel.tagItems.value = viewModel.sortTags(it)
                }
        }

        libraryListModel.filteredTag.observe(viewLifecycleOwner) {
            viewModel.filterWithTag(it)
        }

        viewModel.tagItems.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            // 对标签先按照指定得偏好排序后显示
            adapter.updateData(it)
        }

        viewModel.styleItems.observe(viewLifecycleOwner) {
            viewModel.updateMyTagStyle(it)
        }

        fetchTags()

        adapter = TagListAdapter(requireContext())

        val layoutManager = FlexboxLayoutManager(context)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.flexWrap = FlexWrap.WRAP
        layoutManager.justifyContent = JustifyContent.FLEX_START

        mBinding.rvTags.layoutManager = layoutManager

        mBinding.rvTags.adapter = adapter

        adapter.clickListener = object : TagListAdapter.OnTagClickListener {
            override fun onClick(tag: String) {
                if (libraryListModel.filteredTag.value == tag) {
                    libraryListModel.filteredTag.value = ""
                } else {
                    libraryListModel.filteredTag.value = tag
                }
            }

            override fun onLongClick(tag: String) {
                showTagOperate(tag)
            }
        }
    }

    private fun fetchTags() {
        if (viewModel.itemTags == null) {
            CoroutineScope(Dispatchers.IO).launch {
                val itemTags = async {
                    libraryActivityModel.getUniqueItemTags()
                }

                withContext(Dispatchers.Main) {
                    viewModel.setTagCollection(itemTags.await())
                }
            }

            MyLog.e("ZoteroDebug", "从数据库中获取")
        }
    }

    private fun showTagOperate(tag: String) {
        val arrays1 = arrayOf("分配颜色")
        val arrays2 = arrayOf("分配颜色", "取消颜色分配")
        val arrays = if (viewModel.isTagStyled(tag)) arrays2 else arrays1

        val builder = MaterialAlertDialogBuilder(requireContext())

        builder.setItems(arrays) {
            _, position ->
            when (position) {
                0 -> showTagStylePickerDialog(tag)
                1 -> viewModel.removeTagStyle(tag)
            }
        }
        builder.show()
    }

    private fun showTagStylePickerDialog(tag: String) {
        val binding = DialogTagColorPickerBinding.inflate(layoutInflater)

        val tagColor = viewModel.getTagColor(tag)

        if (!tagColor.isNullOrEmpty()) {
            binding.colorPreview.setBackgroundColor(Color.parseColor(tagColor))
            binding.colorPicker.setInitialColor(Color.parseColor(tagColor))
        }

        binding.colorPicker.setColorListener(object : ColorListener {
            override fun onColorSelected(color: Int, fromUser: Boolean) {
                binding.colorPreview.setBackgroundColor(color)
                binding.edtColorHex.setText(viewModel.parseColor(color))
            }
        })

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("标签样式")
            .setView(binding.root)
            .setPositiveButton("确定"
            ) { p0, p1 ->
                viewModel.modifyTagColor(
                    tag,
                    viewModel.parseColor(binding.colorPicker.color)
                )
            }
        dialog.show()

    }

    companion object {
        @JvmStatic
        fun newInstance(): TagManagerFragment {
            return TagManagerFragment()
        }
    }

}