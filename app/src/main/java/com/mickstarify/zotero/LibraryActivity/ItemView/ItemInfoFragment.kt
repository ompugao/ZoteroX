package com.mickstarify.zotero.LibraryActivity.ItemView

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.mickstarify.zotero.LibraryActivity.ViewModels.LibraryListViewModel
import com.mickstarify.zotero.databinding.FragmentItemInfoBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mickstarify.zotero.adapters.ItemPageAdapter


/**
 * @version V1.0
 * @Author : Moyear
 * @Time : 2023/1/29 20:26
 * @Description : Item基本信息Fragment
 */
class ItemInfoFragment : BottomSheetDialogFragment() {

    private lateinit var mBinding: FragmentItemInfoBinding

    lateinit var libraryViewModel: LibraryListViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentItemInfoBinding.inflate(inflater)

        libraryViewModel =
            ViewModelProvider(requireActivity()).get(LibraryListViewModel::class.java)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        val itemPageAdapter = ItemPageAdapter(libraryViewModel, childFragmentManager, lifecycle)
//        mBinding.viewPager.adapter = itemPageAdapter
//
//        TabLayoutMediator(mBinding.tabLayout, mBinding.viewPager) { tab, position ->
//            tab.text = "第 ${(position + 1)}页"
//        }.attach()

    }

//    override fun onStart() {
//        super.onStart()
//        mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED) //全屏展开
//    }

}