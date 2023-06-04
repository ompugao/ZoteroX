package com.mickstarify.zotero.views

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import com.blankj.utilcode.util.BarUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayoutMediator
import com.mickstarify.zotero.adapters.ItemPageAdapter
import com.mickstarify.zotero.databinding.LayoutContentTabViewpagerBinding

class TabBottomSheetHelper private constructor(val context: Context,
                                               val fragmentManager: FragmentManager,
                                               val layoutInflater: LayoutInflater,
                                               val lifecycle: Lifecycle, val tabs: List<ItemPageAdapter.TabItem>) {

    init {
//        this.context = context
//        this.fragmentManager = supportFragmentManager

        initLayout()
    }

//    private lateinit var context: Context

//    private lateinit var fragmentManager: FragmentManager

//    private lateinit var layoutInflater: LayoutInflater

//    private lateinit var dialog: BottomSheetDialog

    private lateinit var binding: LayoutContentTabViewpagerBinding

    private var title = ""

    companion object {

        private lateinit var INSTANCE: TabBottomSheetHelper

        fun get(activity: AppCompatActivity, tabs: List<ItemPageAdapter.TabItem>): TabBottomSheetHelper {
            INSTANCE = TabBottomSheetHelper(activity, activity.supportFragmentManager, activity.layoutInflater, activity.lifecycle, tabs)

            return INSTANCE
        }

    }

    private fun initLayout() {
        binding = LayoutContentTabViewpagerBinding.inflate(layoutInflater)

        binding.txtItemType.text = title

        val itemPageAdapter = ItemPageAdapter(fragmentManager, lifecycle, tabs)
        binding.viewPager.adapter = itemPageAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabs[position].tabTitle
        }.attach()

    }

    fun create(): BottomSheetDialog {
        val dialog = BottomSheetDialog(context)

        val heightPixels = context.resources.displayMetrics.heightPixels
        dialog.behavior.peekHeight = (heightPixels * 0.8).toInt()

        val layoutParams = binding.viewPager.layoutParams
        layoutParams.height = heightPixels - binding.toolbarSheet.height - binding.tabLayout.height - BarUtils.getStatusBarHeight()
        binding.viewPager.layoutParams = layoutParams

        dialog.setContentView(binding.root)

        binding.btnClose.setOnClickListener {
            dialog?.dismiss()
        }
        return dialog
    }

    fun setTitle(title: String): TabBottomSheetHelper {
        this.title = title
        binding.txtItemType.text = title
        return this
    }

}