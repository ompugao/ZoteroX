package com.mickstarify.zotero.views

import android.content.Context
import android.view.*
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.annotation.DrawableRes
import androidx.annotation.MenuRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.mickstarify.zotero.R
import com.mickstarify.zotero.adapters.ItemPageAdapter
import com.mickstarify.zotero.databinding.LayoutContentTabViewpagerBinding
import kotlin.math.roundToInt


class MaterialDialogHelper private constructor(val context: Context,
                                               val fragmentManager: FragmentManager,
                                               val layoutInflater: LayoutInflater,
                                               val lifecycle: Lifecycle, val tabs: List<ItemPageAdapter.TabItem>) {

    init {
        initLayout()
    }

    private lateinit var binding: LayoutContentTabViewpagerBinding

    private var title = ""

    private var menuResId = -1

    private var menuListener: PopupMenu.OnMenuItemClickListener? = null

    companion object {

//        private lateinit var INSTANCE: TabBottomSheetHelper

        fun get(activity: AppCompatActivity, tabs: List<ItemPageAdapter.TabItem>): MaterialDialogHelper {
            val INSTANCE = MaterialDialogHelper(activity, activity.supportFragmentManager, activity.layoutInflater, activity.lifecycle, tabs)
            return INSTANCE
        }

        fun get(fragment: Fragment, tabs: List<ItemPageAdapter.TabItem>): MaterialDialogHelper {
            val INSTANCE = MaterialDialogHelper(fragment.requireActivity(), fragment.childFragmentManager, fragment.layoutInflater, fragment.lifecycle, tabs)
            return INSTANCE
        }

    }

    private fun initLayout() {
        binding = LayoutContentTabViewpagerBinding.inflate(layoutInflater)

        binding.txtDialogTitle.text = title

        val itemPageAdapter = ItemPageAdapter(fragmentManager, lifecycle, tabs)
        binding.viewPager.adapter = itemPageAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabs[position].tabTitle
        }.attach()

    }

    fun create(): AlertDialog {
        val dialogBuilder = MaterialAlertDialogBuilder(context)
        dialogBuilder.setView(binding.root)

        val dialog = dialogBuilder.create()

        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display: Display = windowManager.defaultDisplay
        val layoutParams: WindowManager.LayoutParams = dialog.window!!.attributes
        layoutParams.height = ((display.height * 0.8).roundToInt())
        layoutParams.width = display.width
        dialog.window!!.attributes = layoutParams

        binding.btnClose.setOnClickListener {
            dialog.dismiss()
        }

        binding.btnMore.setOnClickListener {
            showMenu()
        }

        return dialog
    }

    fun setTitle(title: String): MaterialDialogHelper {
        this.title = title
        binding.txtDialogTitle.text = title
        return this
    }

    fun addButton(@DrawableRes resId: Int, onClickListener: View.OnClickListener?) {
        val btnImage = layoutInflater.inflate(R.layout.button_only_image, binding.lvButtons, false)
            .findViewById<ImageButton>(R.id.btn_image)
        btnImage.setImageDrawable(context.getDrawable(resId))
        onClickListener?.let { btnImage.setOnClickListener(it) }
    }

    fun setMenu(@MenuRes menuId: Int, menuListener: PopupMenu.OnMenuItemClickListener?) {
        this.menuResId = menuId
        this.menuListener = menuListener

        binding.btnMore.visibility = View.VISIBLE
    }

    private fun showMenu() {
        val popupMenu = PopupMenu(context, binding.btnMore)
        popupMenu.inflate(menuResId)

        menuListener?.let {
            popupMenu.setOnMenuItemClickListener(it)
        }
        popupMenu.show()
    }

    fun setTabVisibility(isVisible: Boolean): MaterialDialogHelper {
        if (isVisible) {
            binding.tabLayout.visibility = View.VISIBLE
        } else {
            binding.tabLayout.visibility = View.GONE
        }
        return this
    }

}