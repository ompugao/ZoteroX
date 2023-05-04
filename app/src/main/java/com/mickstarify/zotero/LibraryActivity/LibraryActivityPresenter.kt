package com.mickstarify.zotero.LibraryActivity

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.mickstarify.zotero.ConstValues
import com.mickstarify.zotero.LibraryActivity.ViewModels.LibraryListViewModel
import com.mickstarify.zotero.LibraryActivity.ViewModels.LibraryLoadingScreenViewModel
import com.mickstarify.zotero.MyLog
import com.mickstarify.zotero.R
import com.mickstarify.zotero.SortMethod
import com.mickstarify.zotero.ZoteroAPI.Model.Note
import com.mickstarify.zotero.ZoteroStorage.Database.Collection
import com.mickstarify.zotero.ZoteroStorage.Database.GroupInfo
import com.mickstarify.zotero.ZoteroStorage.Database.Item
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Collator
import java.util.LinkedList
import java.util.Locale
import kotlin.concurrent.thread

class LibraryActivityPresenter(val view: LibraryActivity, context: Context) : Contract.Presenter {
    override lateinit var libraryListViewModel: LibraryListViewModel
    override lateinit var libraryLoadingViewModel: LibraryLoadingScreenViewModel

    val sortMethod = compareBy<Item> {
        when (model.preferences.getSortMethod()) {
            SortMethod.TITLE -> it.getTitle().lowercase(Locale.ROOT)
            SortMethod.DATE -> it.getSortableDateString()
            SortMethod.DATE_ADDED -> it.getSortableDateAddedString()
            SortMethod.AUTHOR -> {
                val authorText = it.getAuthor().lowercase(Locale.ROOT)
                // force empty authors to the bottom. Just like the zotero desktop client.
                if (authorText == "") {
                    "zzz"
                } else {
                    authorText
                }
            }
        }
    }.thenBy { it.getTitle().lowercase(Locale.ROOT) }
//        .then(PinyinComparator())
//        .thenComparator { o1, o2 ->
//        Collator.getInstance(Locale.CHINA).compare(o1.getTitle(), o2.getTitle())
//    }

//    class PinyinComparator : Comparator<Item> {
//        override fun compare(o1: Item, o2: Item): Int {
//            return Collator.getInstance(Locale.CHINA).compare(o1.getTitle(), o2.getTitle())
//        }
//    }


    override fun openGroup(groupTitle: String) {
        model.getGroupByTitle(groupTitle)?.also {
            model.startGroupSync(it)
        }

    }

    override fun startUploadingAttachmentProgress(attachment: Item) {
        view.showAttachmentUploadProgress(attachment)
    }

    override fun stopUploadingAttachmentProgress() {
        view.hideAttachmentUploadProgress()
        view.makeToastAlert("Finished uploading attachment.")
    }

    override fun onResume() {
        if (model.isLoaded()) {
            model.checkAttachmentStorageAccess()
            model.checkAllAttachmentsForModification()
        }
    }

    override fun displayGroupsOnActionBar(groups: List<GroupInfo>) {
        groups.forEach { groupInfo: GroupInfo ->
            Log.d("zotero", "got group ${groupInfo.name}")
            view.addSharedCollection(groupInfo)
        }
    }

    override fun modifyNote(note: Note) {
        model.modifyNote(note)
    }

    override fun createNote(note: Note) {
        if (note.note.trim() != "") {
            model.createNote(note)
        }
    }

    override fun deleteNote(note: Note) {
        model.deleteNote(note)
    }

    override fun redisplayItems() {
        if (model.isLoaded()) {
            if (model.getCurrentCollection() != "unset") {
                setCollection(model.getCurrentCollection())
            }
        }
    }

    override fun cancelAttachmentDownload() {
        model.cancelAttachmentDownload()
        view.hideAttachmentDownloadProgress()
    }

    override fun isShowingContent(): Boolean {
        return model.isDisplayingItems
    }

    override fun updateLibraryRefreshProgress(
        progress: Int,
        total: Int,
        message: String
    ) {
        Log.d("zotero", "Updating library loading progress.")

        if (total > 25 && view.getCurrentScreen() != AvailableScreens.LIBRARY_LOADING_SCREEN) {
            view.navController.navigate(R.id.libraryLoadingScreen)
        }

        libraryLoadingViewModel.setAmountOfDownloadedEntries(progress)
        libraryLoadingViewModel.setTotalAmountOfEntries(total)
        libraryLoadingViewModel.setLoadingMessage(message)

//        view.updateLibraryLoadingProgress(progress, total, message)
    }

    override fun isLiveSearchEnabled(): Boolean {
        return model.preferences.shouldLiveSearch()
    }

    override fun closeQuery() {
        this.setCollection(model.getCurrentCollection())
    }

    override fun addFilterState(query: String) {
        /* This method tells the model to add a filtered state to the the states stack
        * This will allow a user to use the back button to return to the library before
        * the filter was initiated. The need for this method is a little contrived but I had to hack
        * the functionality in so forgive me. */

        val oldState = model.state
        model.states.add(LibraryModelState().apply {
            this.currentGroup = oldState.currentGroup
            this.currentCollection = oldState.currentCollection
            this.filterText = query
        })
    }

    override fun onTagOpen(tagName: String) {
        this.filterEntries("tag:${tagName}")
        this.addFilterState("tag:${tagName}")
        view.setTitle("Tag: $tagName")
    }

    override fun showLoadingAlertDialog(message: String) {
        view.showLoadingAlertDialog(message)
    }

    override fun hideLoadingAlertDialog() {
        view.hideLoadingAlertDialog()
    }

    override fun filterEntries(query: String) {
        Log.d("zotero", "filtering $query")
        if (query == "" || !model.isLoaded()) {
            //not going to waste my time lol.
            return
        }

        if (query.startsWith("tag:")) {
            val tagName = query.substring(4) // remove tag:
            val entries = model.getItemsForTag(tagName).map { ListEntry(it) }
            libraryListViewModel.setItems(entries)
            return
        }

        val collections = model.filterCollections(query)
        val items = model.filterItems(query).sort()

        val entries = LinkedList<ListEntry>()
        entries.addAll(collections.sortedBy { it.name.toLowerCase(Locale.ROOT) }
            .map { ListEntry(it) })
        entries.addAll(
            items
                .map { ListEntry(it) })

        Log.d("zotero", "setting items ${entries.size}")
        libraryListViewModel.setItems(entries)
    }

    override fun attachmentDownloadError(message: String) {
        view.hideAttachmentDownloadProgress()

        // Error getting Attachment
        if (message == "") {
            createErrorAlert(
                view.getString(R.string.error_getting_attachment),
                view.getString(R.string.get_attachment_internet_error)
            ) { }
        } else {
            createErrorAlert(view.getString(R.string.error_getting_attachment), message) { }
        }
    }

    override fun finishDownloadingAttachment() {
        view.hideAttachmentDownloadProgress()
    }

    override fun createYesNoPrompt(
        title: String,
        message: String,
        yesText: String,
        noText: String,
        onYesClick: () -> Unit,
        onNoClick: () -> Unit
    ) {
        view.createYesNoPrompt(title, message, yesText, noText, onYesClick, onNoClick)
    }

    override fun showBasicSyncAnimation() {
        libraryListViewModel.setIsShowingLoadingAnimation(true)
    }

    override fun hideBasicSyncAnimation() {
        libraryListViewModel.setIsShowingLoadingAnimation(false)
        // we are finished loading and should hide the loading screen.
        view.navController.navigate(R.id.libraryListFragment)
    }

    override fun openAttachment(item: Item) {
        model.openAttachment(item)
        MyLog.d("ZoteroDebug", "open attachment: ${item.itemKey}")

    }

    override fun updateAttachmentDownloadProgress(progress: Long, total: Long) {
        val progressKB = (progress / 1000).toInt()
        val totalKB = (total / 1000).toInt()
        view.updateAttachmentDownloadProgress(progressKB, totalKB)
    }

    /**
     * 显示文库加载动画
     */
    override fun showLibraryLoadingAnimation() {
//        view.showLoadingAnimation(showScreen = true)
        view.setTitle("")

        (view as LibraryActivity).navController.navigate(R.id.libraryLoadingScreen)
//        (view as LibraryActivity).navController.navigate(R.id.libraryListFragment)
    }

    override fun hideLibraryLoadingAnimation() {
//        view.hideLoadingAnimation()
//        view.hideLibraryContentDisplay()
        Log.d("zotero", "loading library list fragment")
        (view as LibraryActivity).navController.navigate(R.id.libraryListFragment)
    }

    override fun requestLibraryRefresh() {
        libraryListViewModel.setIsShowingLoadingAnimation(true)
        // 正式开始从zotero服务器获取数据，然后刷新
        model.refreshLibrary(useSmallLoadingAnimation = true)
    }

    override fun selectItem(
        item: Item,
        longPress: Boolean
    ) {
        Log.d("zotero", "pressed ${item.itemType}")
        if (item.itemType == "attachment") {
            this.openAttachment(item)
        } else if (item.itemType == "note") {
            val note = Note(item)
            view.showNote(note)
        } else {
            model.selectedItem = item
            view.showItemDialog(item)
        }
    }

    override fun refreshItemView() {
        val item = model.selectedItem
        if (item != null) {
            view.closeItemView()
            view.showItemDialog(item)
        }
    }

    /**
     * 打开 “我的发表”页面
     */
    override fun openMyPublications() {
        if (!model.isLoaded()) {
            Log.e("zotero", "tried to change collection before fully loaded!")
            return
        }
        model.usePersonalLibrary()
        view.setTitle(view.getString(R.string.my_publication))
        val entries = model.getMyPublications().sort().map{ListEntry(it)}
        model.isDisplayingItems = entries.isNotEmpty()
        model.setCurrentCollection(ConstValues.MY_PUBLICATIONS)
        libraryListViewModel.setItems(entries)
    }

    /**
     * 打开 “回收站页面”
     */
    override fun openTrash() {
        if (!model.isLoaded()) {
            Log.e("zotero", "tried to change collection before fully loaded!")
            return
        }
        model.usePersonalLibrary()
        view.setTitle(view.getString(R.string.trash))
        val entries = model.getTrashedItems().sort().map{ListEntry(it)}
        model.isDisplayingItems = entries.isNotEmpty()
        model.setCurrentCollection(ConstValues.TRASH)
        libraryListViewModel.setItems(entries)
    }

    override fun uploadAttachment(item: Item) {
        model.uploadAttachment(item)
    }

    override fun requestForceResync() {
        model.destroyLibrary()
    }

    override fun backButtonPressed() {
        model.loadPriorState()
        view.highlightMenuItem(model.state)
    }

    override fun setCollection(collectionKey: String, fromNavigationDrawer: Boolean) {
        /*SetCollection is the method used to display items on the listView. It
        * has to get the data, then sort it, then provide it to the view.*/
        if (!model.isLoaded()) {
            Log.e("zotero", "tried to change collection before fully loaded!")
            return
        }
        // this check covers if the user has just left their group library from the sidemenu.
        if (fromNavigationDrawer) {
            model.usePersonalLibrary()
        }

        Log.d("zotero", "Got request to change collection to ${collectionKey}")
        model.setCurrentCollection(collectionKey, usePersonalLibrary = fromNavigationDrawer)

        if (collectionKey == ConstValues.ALL_ITEMS && !model.isUsingGroups()) {
            view.setTitle(view.getString(R.string.my_library))

            thread {
                val entries = model.getLibraryItems().sort().map { ListEntry(it) }
                model.isDisplayingItems = entries.size > 0

                // 在子线程中设置数据的话需调用该方法，否为会报错
                libraryListViewModel.setItemsInBackgroundThread(entries)
            }
        } else if (collectionKey == ConstValues.UNFILED) {
            view.setTitle(view.getString(R.string.unfiled_items))

            thread {
                val entries = model.getUnfiledItems().sort().map { ListEntry(it) }
                model.isDisplayingItems = entries.size > 0

                libraryListViewModel.setItemsInBackgroundThread(entries)
            }
        } else if (collectionKey == ConstValues.TRASH){
            this.openTrash()
        } else if (collectionKey == ConstValues.MY_PUBLICATIONS){
            this.openMyPublications()
        }else if (collectionKey == ConstValues.GROUP_ALL && model.isUsingGroups()) {
            view.setTitle(model.getCurrentGroup()?.name ?: "ERROR")

            thread {
                val entries = LinkedList<ListEntry>()
                entries.addAll(model.getCollections().filter {
                    !it.hasParent()
                }.sortedBy {
                    it.name.lowercase(Locale.ROOT)
                }.map { ListEntry(it) })
                entries.addAll(model.getLibraryItems().sort().map { ListEntry(it) })
                model.isDisplayingItems = entries.size > 0

                libraryListViewModel.setItemsInBackgroundThread(entries)
            }

        }
        // It is an actual collection on the user's private.
        else {
            thread {
                val collection = model.getCollectionFromKey(collectionKey)

                val entries = LinkedList<ListEntry>()
                entries.addAll(model.getSubCollections(collectionKey).sortedBy {
                    it.name.lowercase(Locale.ROOT)
                }.map { ListEntry(it) })

                entries.addAll(model.getItemsFromCollection(collectionKey).sort().map { ListEntry(it) })
                model.isDisplayingItems = entries.size > 0

                view.runOnUiThread {
                    view.setTitle(collection?.name ?: view.getString(R.string.unknown_collection))
                    libraryListViewModel.setItems(entries)
                }
            }
        }



    }

    override fun receiveCollections(collections: List<Collection>) {
        view.clearSidebar()
        for (collection: Collection in collections.filter {
            !it.hasParent()
        }.sortedBy { it.name.toLowerCase(Locale.ROOT) }) {
            Log.d("zotero", "Got collection ${collection.name}")
            view.addNavigationEntry(collection, "Catalog")
        }
    }

    override fun createErrorAlert(
        title: String,
        message: String,
        onClick: () -> Unit
    ) {
        view.createErrorAlert(title, message, onClick)
    }

    override fun makeToastAlert(message: String) {
        view.makeToastAlert(message)
    }

    private lateinit var model: LibraryActivityModel

//    private val model = LibraryActivityModel(this, (context as Activity).application)
//    private var model: LibraryActivityModel = ViewModelProvider(view as LibraryActivity).get(LibraryActivityModel::class.java)

    init {

        model = ViewModelProvider(view as LibraryActivity).get(LibraryActivityModel::class.java)
        model.setLibraryActivityPresenter(this)

        libraryListViewModel =
            ViewModelProvider(view as LibraryActivity).get(LibraryListViewModel::class.java)
        libraryLoadingViewModel =
            ViewModelProvider(view as LibraryActivity).get(LibraryLoadingScreenViewModel::class.java)

        view.initUI()

        if (libraryLoadingViewModel.isFirstLoad().value!!) {
            // 显示 “加载页面”以及加载信息
            // todo start loading screen.
            view.navController.navigate(R.id.libraryLoadingScreen)

            libraryLoadingViewModel.setLoadingMessage(view.getString(R.string.loading_library))

            // 打开应用时，默认加载本地zotero文库
            model.loadLibraryLocally()
            model.loadGroups()

//        // 默认一打开应用就连接zotero服务器，获取最新的文库列表
//        if (model.shouldIUpdateLibrary()) {
//            model.loadGroups()
//            model.downloadLibrary()
//        } else {
//            model.loadLibraryLocally()
//            model.loadGroups()
//        }
            libraryLoadingViewModel.setFirstLoad(false)
        } else {
            // 显示侧边栏列表
            val collections = this.model.getCollections()
            receiveCollections(collections)
        }


        libraryListViewModel.getOnItemClicked().observe(view) { item ->
            this.selectItem(item, longPress = false)
        }

        libraryListViewModel.getOnAttachmentClicked().observe(view) {
            this.openAttachment(it)
        }

        libraryListViewModel.getOnCollectionClicked().observe(view) {
            this.setCollection(it.key)
        }
        libraryListViewModel.getOnLibraryRefreshRequested().observe(view) {
            this.requestLibraryRefresh()
        }
        libraryListViewModel.getScannedBarcode().observe(view) { barcodeNo ->
            view.openZoteroSaveForQuery(barcodeNo)
        }
        libraryListViewModel.getLibraryFilterText().observe(view) {
            Log.d("zotero", "got filter text $it")
            filterEntries(it)
        }
    }

    // extension function to sort lists of items
    private fun List<Item>.sort(): List<Item> {
        if (model.preferences.isSortedAscendingly()) {
            return this.sortedWith(sortMethod).sortByPinyin()
        }

        return this.sortedWith(sortMethod).sortByPinyin().reversed()
    }

    /**
     * 列表按Item标题的中文拼音进行排序
     * 顺序如下：数字在前，然后是中文拼音，而后是英语字母
     */
    private fun List<Item>.sortByPinyin(): List<Item> {
        return this.sortedWith { o1, o2 ->
            Collator.getInstance(Locale.CHINA).compare(o1?.getTitle(), o2?.getTitle())
        }
    }

    fun deleteLocalAttachment(attachment: Item) {
        model.deleteLocalAttachment(attachment)
    }

    fun openHome() {
        view.navController.navigate(R.id.homeLibraryFragment)
    }

    fun requireIntent(clz: Class<*>): Intent {
        return Intent(view, clz)
    }

    fun startActivity(intent: Intent) {
        view.startActivity(intent)
    }

    fun finish() {
        view.finish()
    }

    fun showFullSyncRequirementDialog() {

        // It requires a full library resync if you want to access your items. Would you like to resync your library?
        createYesNoPrompt(view.getString(R.string.full_sync_title),
            view.getString(R.string.full_sync_clue),
            view.getString(R.string.oK),
            view.getString(R.string.cancel),
            {
                CoroutineScope(Dispatchers.IO).launch {
                    model.zoteroDatabase.deleteAllItemsForGroup(GroupInfo.NO_GROUP_ID)
                    model.groups?.forEach {
                        val zDb = model.zoteroGroupDB.getGroup(it.id)
                        zDb.destroyItemsDatabase()
                        model.zoteroDatabase.deleteAllItemsForGroup(it.id)
                    }

                    model.destroyLibrary()

                    withContext(Dispatchers.Main) {
                        model.refreshLibrary()
                    }

                }
            },
            {}
        )

    }

}
