package com.mickstarify.zotero.LibraryActivity.Fragments

import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayoutMediator
import com.mickstarify.zotero.LibraryActivity.ViewModels.LibraryListViewModel
import com.mickstarify.zotero.PreferenceManager
import com.mickstarify.zotero.ZoteroStorage.Database.Collection
import com.mickstarify.zotero.ZoteroStorage.Database.Item
import com.mickstarify.zotero.adapters.ItemPageAdapter
import com.mickstarify.zotero.adapters.LibraryListInteractionListener
import com.mickstarify.zotero.databinding.FragmentLibraryHomeBinding
import javax.inject.Inject

/**
 * @version V1.0
 * @Author : Moyear
 * @Time : 2022/12/5 17:31
 * @Description : 主页文库列表
 */
class HomeFragment : Fragment() , LibraryListInteractionListener {

    private lateinit var viewModel: LibraryListViewModel

    private lateinit var mBinding: FragmentLibraryHomeBinding

    @Inject
    lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(LibraryListViewModel::class.java)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentLibraryHomeBinding.inflate(inflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTabView()

//        val fabZoteroSave =
//            requireView().findViewById<FloatingActionButton>(R.id.fab_action_zotero_save)
//        fabZoteroSave.setOnClickListener {
//            val url = "https://www.zotero.org/save"
//            val intent = Intent(Intent.ACTION_VIEW)
//            intent.data = Uri.parse(url)
//            startActivity(intent)
//        }
    }

    private fun initTabView() {
        val tabs = arrayListOf(
            ItemPageAdapter.TabItem("全部", CustomizedLibraryListFragment()),
//            ItemPageAdapter.TabItem("在读", CustomizedLibraryListFragment()),
//            ItemPageAdapter.TabItem("待读", CustomizedLibraryListFragment())
        )

        val itemPageAdapter = ItemPageAdapter(childFragmentManager, lifecycle, tabs)
        mBinding.viewPager.adapter = itemPageAdapter

        TabLayoutMediator(mBinding.tabLayout, mBinding.viewPager) { tab, position ->
            tab.text = tabs[position].tabTitle
        }.attach()

    }



    override fun onItemOpen(item: Item) {
        viewModel.onItemClicked(item)
    }

    override fun onCollectionOpen(collection: Collection) {
        viewModel.onCollectionClicked(collection)
    }

    override fun onItemAttachmentOpen(item: Item) {
        viewModel.onAttachmentClicked(item)
    }

//    override fun onRefresh() {
//        Log.d("Zotero", "got request for refresh")
//        viewModel.onLibraryRefreshRequested()
//    }



}