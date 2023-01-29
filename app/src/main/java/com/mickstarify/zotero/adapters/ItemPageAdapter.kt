package com.mickstarify.zotero.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mickstarify.zotero.LibraryActivity.ItemView.ItemBasicInfoFragment
import com.mickstarify.zotero.LibraryActivity.ViewModels.LibraryListViewModel

class ItemPageAdapter(viewModel: LibraryListViewModel,
                      fragmentManager: FragmentManager,
                      lifecycle: Lifecycle,
                      tabItems: List<TabItem>
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    private val viewModel: LibraryListViewModel = viewModel

    private var tabItems: List<TabItem>? = tabItems

    override fun getItemCount(): Int {
        return if (tabItems == null) 0
        else tabItems!!.size
    }

    override fun createFragment(position: Int): Fragment {
        val fragment = tabItems?.get(position)?.fragment
        return fragment!!
    }

    class TabItem(tabTitle: String, fragment: Fragment) {
        /**
         * tab标题
         */
        var tabTitle: String = tabTitle

        /**
         * tab Fragment
         */
        var fragment: Fragment = fragment

    }
}