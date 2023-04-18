package com.mickstarify.zotero.LibraryActivity.Fragments

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.getbase.floatingactionbutton.FloatingActionButton
import com.getbase.floatingactionbutton.FloatingActionsMenu
import com.mickstarify.zotero.LibraryActivity.ViewModels.LibraryListViewModel
import com.mickstarify.zotero.MyLog
import com.mickstarify.zotero.PreferenceManager
import com.mickstarify.zotero.R
import com.mickstarify.zotero.ZoteroApplication
import com.mickstarify.zotero.ZoteroStorage.Database.Collection
import com.mickstarify.zotero.ZoteroStorage.Database.Item
import com.mickstarify.zotero.adapters.LibraryItemLongClickListener
import com.mickstarify.zotero.adapters.LibraryListInteractionListener
import com.mickstarify.zotero.adapters.LibraryListRecyclerViewAdapter
import com.mickstarify.zotero.databinding.FragmentLibraryHomeBinding
import javax.inject.Inject

/**
 * @version V1.0
 * @Author : Moyear
 * @Time : 2022/12/5 17:31
 * @Description : 主页文库列表
 */
class HomeFragment : Fragment() , LibraryListInteractionListener, SwipeRefreshLayout.OnRefreshListener{

    private lateinit var viewModel: LibraryListViewModel

    private lateinit var mBinding: FragmentLibraryHomeBinding

    @Inject
    lateinit var preferenceManager: PreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentLibraryHomeBinding.inflate(inflater)
        return mBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(LibraryListViewModel::class.java)

        val recyclerView = mBinding.recyclerView
        val adapter = LibraryListRecyclerViewAdapter(requireContext(), emptyList(), this)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.getItems().observe(viewLifecycleOwner) { entries ->
            MyLog.d("zotero", "Loading new set of item (${entries.size} length)")
            adapter.items = entries
            adapter.notifyDataSetChanged()

//            if (entries.size == 0) {
//                showEmptyList()
//            } else {
//                hideEmptyList()
//            }
        }

        val swipeRefresh = mBinding.swipeRefreshLibrary
        swipeRefresh.setOnRefreshListener(this)
        viewModel.getIsShowingLoadingAnimation().observe(viewLifecycleOwner) {
            swipeRefresh.isRefreshing = it
        }

//        val fabZoteroSave =
//            requireView().findViewById<FloatingActionButton>(R.id.fab_action_zotero_save)
//        fabZoteroSave.setOnClickListener {
//            val url = "https://www.zotero.org/save"
//            val intent = Intent(Intent.ACTION_VIEW)
//            intent.data = Uri.parse(url)
//            startActivity(intent)
//        }

        adapter.setItemLongClick(object : LibraryItemLongClickListener {
            override fun onItemClick(item: Item) {
                showMoreOperateMenuDialog(item)
            }
        })

        hideFabButtonWhenScrolling()
    }

    private fun showMoreOperateMenuDialog(item: Item) {
        val array = arrayOf("查看信息", "打开附件")

        val dialog = AlertDialog.Builder(requireContext())
        dialog.setItems(array) { dialog, which ->
           when (which) {
               0 -> onItemOpen(item)
               1 -> onItemAttachmentOpen(item)

           }
        }
        dialog.show()
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

    override fun onRefresh() {
        Log.d("Zotero", "got request for refresh")
        viewModel.onLibraryRefreshRequested()
    }

    private fun hideFabButtonWhenScrolling() {
        val recyclerView = mBinding.recyclerView
        val fab = mBinding.fabMore

        // now creating the scroll listener for the recycler view
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // if the recycler view is scrolled
                // above hide the FAB
                if (dy > 10 && fab.isShown) {
                    fab.hide()
                }

                // if the recycler view is
                // scrolled above show the FAB
                if (dy < -10 && !fab.isShown) {
                    fab.show()
                }

                // of the recycler view is at the first
                // item always show the FAB
                if (!recyclerView.canScrollVertically(-1)) {
                    fab.show()
                }
            }
        })
    }


}