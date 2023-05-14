package com.mickstarify.zotero.LibraryActivity.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mickstarify.zotero.LibraryActivity.ViewModels.CustomizedListViewModel
import com.mickstarify.zotero.LibraryActivity.ViewModels.LibraryListViewModel
import com.mickstarify.zotero.MyLog
import com.mickstarify.zotero.R
import com.mickstarify.zotero.ZoteroStorage.Database.Collection
import com.mickstarify.zotero.ZoteroStorage.Database.Item
import com.mickstarify.zotero.adapters.LibraryItemLongClickListener
import com.mickstarify.zotero.adapters.LibraryListInteractionListener
import com.mickstarify.zotero.adapters.LibraryListRecyclerViewAdapter
import com.mickstarify.zotero.databinding.FragmentCustomizedLibararyListBinding

class CustomizedLibraryListFragment : Fragment(), LibraryListInteractionListener, SwipeRefreshLayout.OnRefreshListener {

    private lateinit var listViewModel: LibraryListViewModel

//    private lateinit var homeViewModel: HomeFragmentViewModel


    private lateinit var viewModel: CustomizedListViewModel

    lateinit var mBinding: FragmentCustomizedLibararyListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listViewModel = ViewModelProvider(requireActivity()).get(LibraryListViewModel::class.java)

        viewModel = ViewModelProvider(requireActivity()).get(CustomizedListViewModel::class.java)
//        homeViewModel = ViewModelProvider(requireActivity()).get(HomeFragmentViewModel::class.java)
//        viewModel.listViewModel = listViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mBinding = FragmentCustomizedLibararyListBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        val recyclerView = mBinding.list.recyclerView
//        val adapter = LibraryListRecyclerViewAdapter(requireContext(), , emptyList(), this)
//
//        recyclerView.adapter = adapter
//        recyclerView.layoutManager = LinearLayoutManager(requireContext())
//
//        listViewModel.getItems().observe(viewLifecycleOwner) { entries ->
//            MyLog.d("zotero", "Loading new set of item (${entries.size} length)")
//
//            adapter.items = entries
//            adapter.notifyDataSetChanged()
//
//            if (entries.isNullOrEmpty()) {
//                showEmptyList()
//            } else {
//                hideEmptyList()
//            }
//        }
//
//        val swipeRefresh = mBinding.list.swipeRefreshLibrary
//        swipeRefresh.setOnRefreshListener(this)

//
//        listViewModel.getIsShowingLoadingAnimation().observe(viewLifecycleOwner) {
//            swipeRefresh.isRefreshing = it
//        }


//        adapter.setItemLongClick(object : LibraryItemLongClickListener {
//            override fun onItemLongClick(item: Item) {
//                showItemMoreOperateMenuDialog(item)
//            }
//
//            override fun onCollectionLongClick(collection: Collection) {
//                showCollectionMoreOperateMenuDialog(collection)
//            }
//        })

//        hideFabButtonWhenScrolling()

    }

    private fun showItemMoreOperateMenuDialog(item: Item) {
        val array = arrayOf("查看信息", "打开附件")

        val dialog = MaterialAlertDialogBuilder(requireContext())

        dialog.setItems(array) { dialog, which ->
            when (which) {
                0 -> onItemOpen(item)
                1 -> onItemAttachmentOpen(item)

            }
        }
        dialog.show()
    }

    private fun showCollectionMoreOperateMenuDialog(collection: Collection) {

        val arrayFolder = arrayOf("打开", "在新标签打开", "属性")

        val dialog = MaterialAlertDialogBuilder(requireContext())

        dialog.setItems(arrayFolder) { dialog, which ->
//            when (which) {
//                0 -> onItemOpen(item)
//                1 -> onItemAttachmentOpen(item)
//
        }

        dialog.show()
    }

//    private fun hideFabButtonWhenScrolling() {
//        val recyclerView = mBinding.recyclerView
//        val fab = mBinding.fabMore
//
//        // now creating the scroll listener for the recycler view
//        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//
//                // if the recycler view is scrolled
//                // above hide the FAB
//                if (dy > 10 && fab.isShown) {
//                    fab.hide()
//                }
//
//                // if the recycler view is
//                // scrolled above show the FAB
//                if (dy < -10 && !fab.isShown) {
//                    fab.show()
//                }
//
//                // of the recycler view is at the first
//                // item always show the FAB
//                if (!recyclerView.canScrollVertically(-1)) {
//                    fab.show()
//                }
//            }
//        })
//    }

    fun showEmptyList() {
        val layout =
            requireView().findViewById<LinearLayout>(R.id.list_empty_view)
        layout.visibility = View.VISIBLE
    }

    fun hideEmptyList() {
        val layout =
            requireView().findViewById<LinearLayout>(R.id.list_empty_view)
        layout.visibility = View.GONE
    }

    override fun onItemOpen(item: Item) {
//        listViewModel.onItemClicked(item)
//        listViewModel.onItemOpen(item)
    }

    override fun onCollectionOpen(collection: Collection) {
//        listViewModel.onCollectionClicked(collection)
//        listViewModel.onCollectionOpen(collection)
    }

    override fun onItemAttachmentOpen(item: Item) {
//        listViewModel.onAttachmentClicked(item)
    }

    override fun onRefresh() {
        Log.d("Zotero", "got request for refresh")
//        listViewModel.onLibraryRefreshRequested()
    }


}