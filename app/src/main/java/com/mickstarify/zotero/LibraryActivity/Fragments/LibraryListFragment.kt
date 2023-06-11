package com.mickstarify.zotero.LibraryActivity.Fragments

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mickstarify.zotero.*
import com.mickstarify.zotero.LibraryActivity.LibraryActivity
import com.mickstarify.zotero.LibraryActivity.ViewModels.LibraryListViewModel
import com.mickstarify.zotero.ZoteroStorage.Database.Collection
import com.mickstarify.zotero.ZoteroStorage.Database.Item
import com.mickstarify.zotero.adapters.LibraryItemLongClickListener
import com.mickstarify.zotero.adapters.LibraryListInteractionListener
import com.mickstarify.zotero.adapters.LibraryListRecyclerViewAdapter
import javax.inject.Inject


class LibraryListFragment : Fragment(), LibraryListInteractionListener,
    SwipeRefreshLayout.OnRefreshListener {

    companion object {
        fun newInstance() = LibraryListFragment()
    }

    private lateinit var viewModel: LibraryListViewModel

    private lateinit var fab: FloatingActionButton

    @Inject
    lateinit var preferenceManager: PreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.library_list_fragment, container, false)
    }

    fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager

//        val imm: InputMethodManager =
//            requireContext().getSystemService(InputMethodManager::class.java) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        } 
        
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.activity_library_actionbar, menu)
        super.onCreateOptionsMenu(menu, inflater)

        val menuItem = menu.findItem(R.id.search)
        menuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                (requireActivity() as LibraryActivity).presenter.closeQuery()
                return true
            }
        })

        val searchManager =
            (requireActivity() as LibraryActivity).getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView = menu.findItem(R.id.search).actionView as SearchView
        searchView.apply {
            setSearchableInfo(searchManager.getSearchableInfo((requireActivity() as LibraryActivity).componentName))
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
//                    setTitle("Search results for $query")
                    // we will only search on submit for android 7<=
                    if (!preferenceManager.shouldLiveSearch()) {
                        viewModel.setLibraryFilterText(query)
                    }
                    (requireActivity() as LibraryActivity).presenter.addFilterState(query)
                    Log.d("zotero", "queryOnQuery $query")
                    hideKeyboard(requireActivity())
                    return true

                    // need to dismiss keyboard.
                }

                override fun onQueryTextChange(query: String): Boolean {
                    if (!preferenceManager.shouldLiveSearch()) {
                        return false
                    }
                    Log.d("zotero", "queryOnQuery $query")
                    viewModel.setLibraryFilterText(query)
                    return true
                }

            })
        }
    }

    lateinit var searchView: SearchView

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel = ViewModelProvider(requireActivity()).get(LibraryListViewModel::class.java)
        (requireActivity().application as ZoteroApplication).component.inject(this)
        val recyclerView = requireView().findViewById<RecyclerView>(R.id.recycler_view)
        val adapter = LibraryListRecyclerViewAdapter(requireContext(),  this)

        recyclerView.adapter = adapter

        val linearLayoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = linearLayoutManager

        recyclerView.addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val view = recyclerView.layoutManager?.getChildAt(0) ?: return

                //获取第一个view所在的位置
                recyclerView.layoutManager?.getPosition(view)?.let {  viewModel.setMutativePosition(it) }

                recyclerView.layoutManager?.getChildAt(0)?.top?.let { viewModel.setMutativeOffset(it) }
            }
        })

        linearLayoutManager.scrollToPositionWithOffset(viewModel.getMutativePosition(), viewModel.getMutativeOffset())

        viewModel.getItems().observe(viewLifecycleOwner) { entries ->
            if (entries.isNullOrEmpty()) {
                showEmptyList()
            } else {
                hideEmptyList()
            }

            adapter.setData(entries)

            MyLog.d("zotero", "Loading new set of item (${entries.size} length)")
        }

        viewModel.filteredTag.observe(viewLifecycleOwner) {
            tag ->
            tag?.let {
                val filteredEntries = if (it == "") {
                    viewModel.getItems().value
                } else {
                    viewModel.filterByTag(it, viewModel.getItems().value!!)
                }

                if (filteredEntries.isNullOrEmpty()) {
                    showEmptyList()
                } else {
                    hideEmptyList()
                }
                adapter.setData(filteredEntries!!)

                MyLog.d("Zotero", "filter items by tag: $tag")
            }
        }

        val swipeRefresh =
            requireView().findViewById<SwipeRefreshLayout>(R.id.library_swipe_refresh)
        swipeRefresh.setOnRefreshListener(this)
        viewModel.getIsShowingLoadingAnimation().observe(viewLifecycleOwner) {
            if (swipeRefresh.isRefreshing != it) {
                swipeRefresh.isRefreshing = it
            }
        }

        fab = requireView().findViewById(R.id.fab_add_item)

        fab.setOnClickListener {
//            val array = arrayOf("Zotero Save", "Scan ISBN")
            val array = arrayOf("Zotero Save")

            val dialog = MaterialAlertDialogBuilder(requireContext())
            dialog.setItems(array) { dialog, which ->
                when (which) {
                    0 -> {
                        val url = "https://www.zotero.org/save"
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(url)
                        startActivity(intent)
                    }
//                    1 -> findNavController().navigate(R.id.action_libraryListFragment_to_barcodeScanningScreen)
                }
            }
            dialog.show()
        }

        adapter.setItemLongClick(object : LibraryItemLongClickListener {
            override fun onItemLongClick(item: Item) {
                showMoreOperateMenuDialog(item)
            }

            override fun onCollectionLongClick(collection: Collection) {
                showCollectionMoreOperateMenuDialog(collection)
            }
        })

        hideFabButtonWhenScrolling()
    }

    private fun showMoreOperateMenuDialog(item: Item) {
        val array = arrayOf("查看信息", "在线查看", "添加到收藏", "下载附件")

        if (viewModel.isStared(item)) {
            array[2] = "取消收藏"
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
        dialog.setItems(array) { dialog, which ->
            when (which) {
                0 -> showItemInfo(item)
                1 -> viewItOnline(item)
                2 -> starOrRemoveStar(item)
                3 -> downloadItem(item)
            }
        }
        dialog.show()
    }

    private fun viewItOnline(item: Item) {
        val urlString = item.data.get("url")
        val uri = Uri.parse(urlString)

        val intent = Intent()
        intent.action = "android.intent.action.VIEW"
        intent.data = uri
        startActivity(intent)

    }

    private fun showCollectionMoreOperateMenuDialog(collection: Collection) {
        val arrayFolder = arrayOf("添加到收藏", "属性")

        if (viewModel.isStared(collection)) {
            arrayFolder[0] = "取消收藏"
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())

        dialog.setItems(arrayFolder) { dialog, which ->
            when (which) {
                0 -> starOrRemoveStar(collection)
                1 -> showCollectionInfo(collection)
            }
        }

        dialog.show()
    }

    private fun starOrRemoveStar(collection: Collection) {
        if (viewModel.isStared(collection)) {
            viewModel.removeStar(collection)
        } else {
            viewModel.addToStar(collection)
        }
    }

    private fun starOrRemoveStar(item: Item) {
        if (viewModel.isStared(item)) {
            viewModel.removeStar(item)
        } else {
            viewModel.addToStar(item)
        }
    }

    private fun showCollectionInfo(collection: Collection) {
        val msg = "分类名：${collection.name} \nkey: ${collection.key} " +
                "\n父Key: ${collection.parentCollection} \n是否有子集合: ${collection.hasChildren()} " +
                "\n版本: ${collection.version}"
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("集合信息")
            .setMessage(msg).setPositiveButton("确定") {_,_ ->}
            .show()

    }

    private fun showItemInfo(item: Item) {
        viewModel.onItemClicked(item)
    }

    override fun onItemOpen(item: Item) {
//        viewModel.onItemClicked(item)
    }

    override fun onCollectionOpen(collection: Collection) {
        viewModel.onCollectionClicked(collection)
    }

    override fun onItemAttachmentOpen(item: Item) {
        viewModel.onAttachmentClicked(item)
//        MyLog.d("Zotero attachment", "open attachment of " + item.itemKey)
    }

    override fun onRefresh() {
        Log.d("Zotero", "got request for refresh")
        viewModel.onLibraryRefreshRequested()
    }

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

    fun hideFabButtonWhenScrolling() {
        val recyclerView = requireView().findViewById<RecyclerView>(R.id.recycler_view)
        val fab = requireView().findViewById<FloatingActionButton>(R.id.fab_add_item)

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
//                    fab.expand()
                    fab.show()
                }

                // of the recycler view is at the first
                // item always show the FAB
                if (!recyclerView.canScrollVertically(-1)) {
                    fab.visibility = View.VISIBLE
//                    fab.expand()
                    fab.show()
                }
            }
        })
    }

    /**
     * download all attachments of this item
     */
    private fun downloadItem(item: Item) {
        // todo: 实现下载item的所有附件
        // download the first pdf attachment of this item
        item.getPdfAttachment()?.let {
            viewModel.onAttachmentToDownload(it)
        }

    }

}