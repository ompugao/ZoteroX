package com.mickstarify.zotero.LibraryActivity

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import android.util.Log
import android.util.SparseArray
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.iterator
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayoutMediator
import com.kongzue.dialogx.dialogs.CustomDialog
import com.mickstarify.zotero.*
import com.mickstarify.zotero.AttachmentManager.AttachmentManager
import com.mickstarify.zotero.LibraryActivity.ItemView.*
import com.mickstarify.zotero.LibraryActivity.Notes.EditNoteDialog
import com.mickstarify.zotero.LibraryActivity.Notes.NoteInteractionListener
import com.mickstarify.zotero.LibraryActivity.Notes.NoteView
import com.mickstarify.zotero.LibraryActivity.Notes.onEditNoteChangeListener
import com.mickstarify.zotero.LibraryActivity.ViewModels.LibraryListViewModel
import com.mickstarify.zotero.ZoteroAPI.Model.Note
import com.mickstarify.zotero.ZoteroStorage.Database.Collection
import com.mickstarify.zotero.ZoteroStorage.Database.GroupInfo
import com.mickstarify.zotero.ZoteroStorage.Database.Item
import com.mickstarify.zotero.ZoteroStorage.ZoteroUtils
import com.mickstarify.zotero.adapters.ItemPageAdapter
import com.mickstarify.zotero.databinding.ContentDialogProgressBinding
import com.mickstarify.zotero.databinding.FragmentItemInfoBinding
import com.mickstarify.zotero.global.ScreenUtils


class LibraryActivity : BaseActivity(),
    NavigationView.OnNavigationItemSelectedListener,
    ItemAttachmentEntry.OnAttachmentFragmentInteractionListener,
    NoteInteractionListener {

    private val MENU_ID_UNFILED_ITEMS: Int = 1
    private val MENU_ID_TRASH: Int = 2
    private val MENU_ID_MY_PUBLICATIONS = 3

    private val MENU_ID_COLLECTIONS_OFFSET: Int = 10

    lateinit var presenter: LibraryActivityPresenter
//    private var itemView: ItemViewFragment? = null

    lateinit var navController: NavController
    lateinit var navHostFragment: NavHostFragment

    private lateinit var toolbar: Toolbar

    // used to "uncheck" the last pressed menu item if we change via code.
    private var currentPressedMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)

        toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        setTitle("")

        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        navController = navHostFragment.navController

        this.setupActionbar()
        val navigationView = findViewById<NavigationView>(R.id.nav_view_library)
        navigationView.setNavigationItemSelectedListener(this)

        presenter = LibraryActivityPresenter(this, this)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeLibraryFragment -> {
                    libraryHomeScreenShown()
                }
                R.id.libraryListFragment -> {
                    libraryListScreenShown()
                }
                R.id.libraryLoadingScreen -> {
                    // 显示文库加载界面
                    libraryLoadingScreenShown()
                }
                R.id.barcodeScanningScreen -> {
                    barcodeScanningScreenShown()
                }
                else -> {
                    throw(NotImplementedError("Error screen $destination not handled."))
                }
            }

        }

    }

    private fun libraryHomeScreenShown() {
        setTitle(getString(R.string.homepage))
        mDrawerToggle.isDrawerIndicatorEnabled = true
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun barcodeScanningScreenShown() {
        setTitle("Barcode Scanner")
        mDrawerToggle.isDrawerIndicatorEnabled = false
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    /**
     * 显示文库加载界面
     */
    private fun libraryLoadingScreenShown() {
//        supportActionBar?.title = "加载中..."
        //加载动画的时候不显示toolbar标题
        setTitle("")
        mDrawerToggle.isDrawerIndicatorEnabled = true
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    /**
     * 显示文库界面
     */
    private fun libraryListScreenShown() {
        setTitle("文库")
        mDrawerToggle.isDrawerIndicatorEnabled = true
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    lateinit var collectionsMenu: Menu
    lateinit var sharedCollections: Menu

    fun initUI() {
        setupActionbar()
        val navigationView = findViewById<NavigationView>(R.id.nav_view_library)
        navigationView.setCheckedItem(R.id.my_library)
        collectionsMenu = navigationView.menu.addSubMenu(
            R.id.group_collections,
            Menu.NONE,
            Menu.NONE,
            getString(R.string.nav_header_collections)
        )

        // 添加“非分类条目”栏目
        navigationView.menu.add(R.id.group_other, MENU_ID_UNFILED_ITEMS, Menu.NONE, getString(R.string.nav_menu_unfiled_items))
            .setIcon(R.drawable.baseline_description_24).isCheckable = true

        // 添加“我的出版物”栏目
        navigationView.menu.add(
            R.id.group_other,
            MENU_ID_MY_PUBLICATIONS,
            Menu.NONE,
            getString(R.string.nav_menu_my_publication)
        ).setIcon(R.drawable.baseline_book_24).isCheckable = true

        // add our "Trash" entry
        navigationView.menu.add(R.id.group_other, MENU_ID_TRASH, Menu.NONE, getString(R.string.nav_menu_trash))
            .setIcon(R.drawable.baseline_delete_24).isCheckable = true

        sharedCollections = navigationView.menu.addSubMenu(
            R.id.group_shared_collections,
            Menu.NONE,
            Menu.NONE,
            "Group Libraries"
        )

        navController.navigate(R.id.libraryListFragment)
    }

    val collectionKeyByMenuId = SparseArray<String>()

    fun addNavigationEntry(collection: Collection, parent: String) {
        // we will add a collection to the menu, with the following menu ID.
        val menuId = MENU_ID_COLLECTIONS_OFFSET + collectionKeyByMenuId.size()
        collectionKeyByMenuId.put(menuId, collection.key)

        collectionsMenu.add(R.id.group_collections, menuId, Menu.NONE, collection.name)
            .setIcon(R.drawable.ic_folder).isCheckable = true
    }

    fun highlightMenuItem(state: LibraryModelState) {
        /* Given some collection name / group name, highlight that corresponding item.
        * Needed for back button, whereby the app loads a prior state and needs the UI
        * to reflect this change with respect to the item selected.  */

        currentPressedMenuItem?.let {
            it.isChecked = false
        }

        if (state.isUsingGroup()) {
            for (menuItem: MenuItem in sharedCollections) {
                if (menuItem.title.toString() == state.currentGroup.name) {
                    menuItem.isChecked = true
                    currentPressedMenuItem = menuItem
                    break
                }
            }
            return
        }

        val menuItem: MenuItem? = if (state.currentCollection == ConstValues.ALL_ITEMS) {
            val navigationView = findViewById<NavigationView>(R.id.nav_view_library)
            navigationView.menu.findItem(R.id.my_library)
        } else if (state.currentCollection == ConstValues.UNFILED) {
            val navigationView = findViewById<NavigationView>(R.id.nav_view_library)
            navigationView.menu.findItem(MENU_ID_UNFILED_ITEMS)
        } else if (state.currentCollection == ConstValues.MY_PUBLICATIONS) {
            val navigationView = findViewById<NavigationView>(R.id.nav_view_library)
            navigationView.menu.findItem(MENU_ID_MY_PUBLICATIONS)
        } else if (state.currentCollection == ConstValues.TRASH) {
            val navigationView = findViewById<NavigationView>(R.id.nav_view_library)
            navigationView.menu.findItem(MENU_ID_TRASH)
        } else {
            // if it reaches here that means it's a collection.
            val index = collectionKeyByMenuId.indexOfValue(state.currentCollection)
            if (index >= 0) {
                val menuId = collectionKeyByMenuId.keyAt(index)
                collectionsMenu.findItem(menuId)
            } else {
                null
            }
        }
        menuItem?.isChecked = true
        menuItem?.let {
            currentPressedMenuItem = it
        }
    }

    /* Shows a shared Collection (group) on the sidebar. */
    fun addSharedCollection(groupInfo: GroupInfo) {
        val navigationView = findViewById<NavigationView>(R.id.nav_view_library)
        sharedCollections.add(
            R.id.group_shared_collections,
            Menu.NONE,
            Menu.NONE,
            groupInfo.name
        ).setIcon(R.drawable.ic_folder).isCheckable = true
    }

    fun clearSidebar() {
        collectionsMenu.clear()
    }

    fun showFilterMenu() {
        LibraryFilterMenuDialog(this, { presenter.redisplayItems() }).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }

        when (item.itemId) {
            R.id.filter_menu -> {
                showFilterMenu()
            }

            R.id.settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
            R.id.attachment_manager -> {
                val intent = Intent(this, AttachmentManager::class.java)
                startActivity(intent)
            }
            R.id.force_resync -> {
                presenter.requestForceResync()
            }
            android.R.id.home -> {
                onBackPressed()
            }

        }
        return super.onOptionsItemSelected(item)
    }

    lateinit var searchView: SearchView
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.activity_library_actionbar, menu)
        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        Log.d("zotero", "pressed ${item.groupId} - ${item.itemId}")

        currentPressedMenuItem?.let {
            it.isChecked = false
        }

        currentPressedMenuItem = item
        if (item.itemId == R.id.my_library) {
            presenter.setCollection("all", fromNavigationDrawer = true)
        } else if (item.itemId == R.id.nav_home) {
            //todo: 打开首页页面
            presenter.openHome()
        } else if (item.itemId == MENU_ID_UNFILED_ITEMS) {
            presenter.setCollection("unfiled_items", fromNavigationDrawer = true)
        } else if (item.itemId == MENU_ID_MY_PUBLICATIONS) {
            presenter.openMyPublications()
        } else if (item.itemId == MENU_ID_TRASH) {
            presenter.openTrash()
        } else if (item.groupId == R.id.group_shared_collections) {
            // this is the id that refers to group libraries
            presenter.openGroup(item.title.toString())
        } else if (item.groupId == R.id.group_collections) {
            // this is the id that refers to user collections. The term "group collections" is misleading.
            presenter.setCollection(collectionKeyByMenuId[item.itemId], fromNavigationDrawer = true)
        } else {
            Log.e("zotero", "error unhandled menuitem. ${item.title}")
        }
        val drawer = findViewById<DrawerLayout>(R.id.drawerlayout_library)
        drawer.closeDrawer(GravityCompat.START)
        // shouldnt be neccessary but there was a bug where android wouldn't highlight on certain occassions
        item.isChecked = true
        return true
    }

    fun createErrorAlert(title: String, message: String, onClick: () -> Unit) {
        val alert = MaterialAlertDialogBuilder(this)
        alert.setIcon(R.drawable.ic_error_black_24dp)
        alert.setTitle(title)
        alert.setMessage(message)
        alert.setPositiveButton("Ok") { _, _ -> onClick() }
        try {
            alert.show()
        } catch (exception: WindowManager.BadTokenException) {
            Log.e("zotero", "error cannot show error dialog. ${message}")
        }
    }

    lateinit var mDrawerToggle: ActionBarDrawerToggle
    private fun setupActionbar() {
        val drawer = findViewById<DrawerLayout>(R.id.drawerlayout_library)
        mDrawerToggle = ActionBarDrawerToggle(
            this,
            drawer,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        mDrawerToggle.isDrawerIndicatorEnabled = true
        mDrawerToggle.isDrawerSlideAnimationEnabled = true

        drawer.addDrawerListener(mDrawerToggle)
        mDrawerToggle.syncState()

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.show()
    }

    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)
        mDrawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mDrawerToggle.syncState()
    }

    fun setTitle(title: String) {
//        supportActionBar?.title = title
        supportActionBar?.title = ""

        toolbar.findViewById<TextView>(R.id.txt_title)?.let {
            it.text = title
        }
    }

    fun showItemDialog(item: Item) {
//        itemView = ItemViewFragment()
//        val fm = supportFragmentManager
//        itemView?.show(fm, "ItemDialog")

        val binding = FragmentItemInfoBinding.inflate(layoutInflater)

        val libraryViewModel =
            ViewModelProvider(this).get(LibraryListViewModel::class.java)

        val tabs = arrayListOf(
            ItemPageAdapter.TabItem("基本", ItemBasicInfoFragment.newInstance(item)),
            ItemPageAdapter.TabItem("标签", ItemTagsFragment.newInstance(item)),
            ItemPageAdapter.TabItem("笔记", ItemNotesFragment.newInstance(item))
        )

        val itemPageAdapter = ItemPageAdapter(libraryViewModel,
            supportFragmentManager, lifecycle, tabs)
        binding.viewPager.adapter = itemPageAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabs[position].tabTitle
        }.attach()

        libraryViewModel.getOnItemClicked().observe(this) { item ->
            binding.txtItemType.text = ZoteroUtils.getItemTypeHumanReadableString(item.itemType)
//            binding.textViewItemToolbarTitle.text = item.getTitle()

            binding.btnAddNote.setOnClickListener {
                showCreateNoteDialog(item)
            }

            binding.btnMore.setOnClickListener {
                val popupMenu = PopupMenu(this, it)
                popupMenu.inflate(R.menu.fragment_library_item_actionbar)

                popupMenu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.add_note -> showCreateNoteDialog(item)
                        R.id.share_item -> showShareItemDialog(item)
                    }
                    false
                }
                popupMenu.show()
            }
        }

        var dialog: Dialog? = null

        if (ScreenUtils.isTabletWindow(this)) {
            dialog = Dialog(this)
        } else {
            dialog = BottomSheetDialog(this)
            dialog.behavior.peekHeight = 800
        }

        dialog.setContentView(binding.root)
        dialog.show()

        binding.btnClose.setOnClickListener {
            dialog?.dismiss()
        }

    }

    /**
     * 显示创建笔记对话框
     */
    private fun showCreateNoteDialog(item: Item) {
        EditNoteDialog()
            .show(this, "", object :
                onEditNoteChangeListener {
                override fun onCancel() {
                }

                override fun onSubmit(noteText: String) {
                    Log.d("zotero", "got note $noteText")
                    if (item == null) {
                        Toast.makeText(applicationContext,
                            "Error, item unloaded from memory, please backup text and reopen app.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                    val note = Note(noteText, item.itemKey)
                    onNoteCreate(note)
                }
            })
    }


    private fun showShareItemDialog(item: Item) {
        if (item == null) {
            Toast.makeText(this, "Item not loaded yet.", Toast.LENGTH_SHORT).show()
        } else {
            ShareItemDialog(item).show(this, object : onShareItemListener {
                override fun shareItem(shareText: String) {
                    shareText(shareText)
                }

            })
        }
    }

    fun showNote(note: Note) {
        val noteView = NoteView(this, note, this)
        noteView.show()
    }

    fun closeItemView() {
//        itemView?.dismiss()
//        itemView = null
    }

    fun showLoadingAlertDialog(message: String) {
        if (progressDialog == null) {
            val dialogBuilder = MaterialAlertDialogBuilder(this)

            val binding = ContentDialogProgressBinding.inflate(layoutInflater)
            binding.txtContent.text = message

            progressDialog = dialogBuilder.setView(binding.root)
                .setCancelable(false)
                .show()
        }

//        if (progressDialog == null) {
//            progressDialog = ProgressDialog(this)
//        }
//        progressDialog?.isIndeterminate = true
//        progressDialog?.setMessage(message)
//        progressDialog?.show()
    }

    fun hideLoadingAlertDialog() {
        progressDialog?.hide()
        progressDialog = null
    }

    var progressDialog: AlertDialog? = null
    var progressContentBinding: ContentDialogProgressBinding? = null

    fun updateAttachmentDownloadProgress(progress: Int, total: Int) {
        if (progressDialog == null) {
            val dialogBuilder = MaterialAlertDialogBuilder(this)

            progressContentBinding = ContentDialogProgressBinding.inflate(layoutInflater)

            dialogBuilder.setTitle(getString(R.string.downloading_file))
                .setView(progressContentBinding!!.root)
                .setCancelable(false)
                .setNegativeButton(getString(R.string.cancel)) { dialogInterface, _ ->
                    presenter.cancelAttachmentDownload()
                }
            progressDialog = dialogBuilder.show()
        }

        val progressString = if (progress == 0) {
            ""
          } else {
            if (total > 0) {
                "$progress/${total} KB"
            } else {
                "${progress} KB"
            }
        }

        progressContentBinding?.txtContent?.text = "${getString(R.string.downloading_your_attachment)} : $progressString"
    }

    fun hideAttachmentDownloadProgress() {
//        progressDialog?.hide()
        progressDialog?.dismiss()
        progressDialog = null
    }

    var uploadProgressDialog: ProgressDialog? = null
    fun showAttachmentUploadProgress(attachment: Item) {
        uploadProgressDialog = ProgressDialog(this)
        uploadProgressDialog?.setTitle("Uploading Attachment")
        uploadProgressDialog?.setMessage(
            "Uploading ${attachment.data["filename"]}. " +
                    "This may take a while, do not close the app."
        )
        uploadProgressDialog?.isIndeterminate = true
        uploadProgressDialog?.show()
    }

    fun hideAttachmentUploadProgress() {
        uploadProgressDialog?.hide()
        uploadProgressDialog = null
    }

    fun createYesNoPrompt(
        title: String,
        message: String, yesText: String, noText: String,
        onYesClick: () -> Unit,
        onNoClick: () -> Unit
    ) {
        val alert = MaterialAlertDialogBuilder(this)
        alert.setIcon(R.drawable.ic_error_black_24dp)
        alert.setTitle(title)
        alert.setMessage(message)
        alert.setPositiveButton(yesText) { _, _ -> onYesClick() }
        alert.setNegativeButton(noText) { _, _ -> onNoClick() }
        try {
            alert.show()
        } catch (exception: WindowManager.BadTokenException) {
            Log.e("zotero", "error creating window bro")
        }
    }

    /* Is called by the fragment when an attachment is openned by the user. */
    override fun openAttachmentFileListener(item: Item) {
        presenter.openAttachment(item)
    }

    override fun forceUploadAttachmentListener(item: Item) {
        presenter.uploadAttachment(item)
    }

    override fun openLinkedAttachmentListener(item: Item) {
        presenter.openAttachment(item)
    }

    override fun deleteLocalAttachment(item: Item) {
        presenter.deleteLocalAttachment(item)
        Toast.makeText(this, "Deleted attachment ${item.getTitle()}", Toast.LENGTH_SHORT).show()
    }

//    override fun onListFragmentInteraction(item: Item?) {
//        Log.d("zotero", "got onListFragmentInteraction from item ${item?.itemKey}")
//    }

    fun makeToastAlert(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
        toast.show()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(packageName, "got intent ${intent.action} $intent")
        if (intent.action == ACTION_FILTER) {
            val query = intent.getStringExtra(EXTRA_QUERY) ?: ""
            Log.d(packageName, "got intent for library filter $query")
            presenter.filterEntries(query)
        }
    }

    override fun onPause() {
        Log.e("zotero", "paused")
        super.onPause()
    }

    override fun onPostResume() {
        Log.e("zotero", "post-resumed")
        super.onPostResume()
        presenter.onResume()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.e("zotero", "restored!")
    }

    override fun onResume() {
        super.onResume()
        Log.d("zotero", "onResume called.")
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }

    private fun onNoteCreate(note: Note) {
        presenter.createNote(note)
    }

//    override fun onNoteEdit(note: Note) {
//        presenter.modifyNote(note)
//    }
//
//    override fun onNoteDelete(note: Note) {
//        presenter.deleteNote(note)
//    }

    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        // our list view has a custom back handler
        // if we are on the barcode screen we will just want to return to the previous screen
        if (getCurrentScreen() == AvailableScreens.BARCODE_SCANNING_SCREEN) {
            navController.navigateUp()
        } else {
            presenter.backButtonPressed()
        }

        Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
    }

    companion object {
        const val ACTION_FILTER =
            "com.mickstarify.zooforzotero.intent.action.LIBRARY_FILTER_INTENT"
        const val EXTRA_QUERY = "com.mickstarify.zooforzotero.intent.EXTRA_QUERY_TEXT"
    }

    override fun deleteNote(note: Note) {
        presenter.deleteNote(note)
    }

    override fun editNote(note: Note) {
        presenter.modifyNote(note)
    }

    private fun shareText(shareText: String) {
        /* Used to share zotero items. */

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

//    override fun onTagOpenListener(tag: String) {
//        presenter.onTagOpen(tag)
//    }

    fun getCurrentScreen(): AvailableScreens {
        return when (navController.currentDestination?.id) {
            R.id.homeLibraryFragment -> AvailableScreens.LIBRARY_HOME_SCREEN
            R.id.libraryListFragment -> AvailableScreens.LIBRARY_LISTVIEW_SCREEN
            R.id.libraryLoadingScreen -> AvailableScreens.LIBRARY_LOADING_SCREEN
            R.id.barcodeScanningScreen -> AvailableScreens.BARCODE_SCANNING_SCREEN
            else -> throw (Exception("Unknown current screen ${navController.currentDestination}"))
        }
    }

    fun openZoteroSaveForQuery(query: String) {
        val url = "https://www.zotero.org/save?q=$query"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

}

enum class AvailableScreens {
    LIBRARY_LOADING_SCREEN,
    LIBRARY_HOME_SCREEN,
    LIBRARY_LISTVIEW_SCREEN,
    BARCODE_SCANNING_SCREEN
}