package com.mickstarify.zotero.LibraryActivity.ViewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mickstarify.zotero.LibraryActivity.ListEntry
import com.mickstarify.zotero.MyItemFilter
import com.mickstarify.zotero.MyLog
import com.mickstarify.zotero.ZoteroStorage.Database.Collection
import com.mickstarify.zotero.ZoteroStorage.Database.Item
import com.mickstarify.zotero.global.SingleLiveEvent

class LibraryListViewModel(application: Application) : AndroidViewModel(application) {

    private val items = SingleLiveEvent<List<ListEntry>>()

    // view的位置
    private var mutativePosition: Int = 0
    //偏移
    private var rvMutativeOffset: Int = 0

    fun getItems(): LiveData<List<ListEntry>> = items
    fun setItems(items: List<ListEntry>) {
        this.items.value = items
    }

    fun setItemsInBackgroundThread(items: List<ListEntry>) {
        this.items.postValue(items)
    }

    private val itemClicked = SingleLiveEvent<Item>()
    fun getOnItemClicked(): SingleLiveEvent<Item> = itemClicked
    fun onItemClicked(item: Item) {
        this.itemClicked.value = item
    }

    private val attachmentClicked = SingleLiveEvent<Item>()
    fun getOnAttachmentClicked(): LiveData<Item> = attachmentClicked
    fun onAttachmentClicked(attachment: Item) {
        this.attachmentClicked.value = attachment
    }

    private val attachmentToDownload = SingleLiveEvent<Item>()

    fun getAttachmentToDownload(): LiveData<Item> = attachmentToDownload

    fun onAttachmentToDownload(item: Item) {
        this.attachmentToDownload.value = item
    }

    private val collectionClicked = MutableLiveData<Collection>()
    fun getOnCollectionClicked(): LiveData<Collection> = collectionClicked
    fun onCollectionClicked(collection: Collection) {
        this.collectionClicked.value = collection
    }

    private val scannedBarcode = MutableLiveData<String>()
    fun scannedBarcodeNumber(barcodeNo: String) {
        scannedBarcode.value = barcodeNo
    }

    fun getScannedBarcode(): LiveData<String> = scannedBarcode

    private val isShowingLoadingAnimation = MutableLiveData<Boolean>(false)
    fun setIsShowingLoadingAnimation(value: Boolean) {
        if (isShowingLoadingAnimation.value != value) {
            isShowingLoadingAnimation.value = value
        }
    }

    fun getIsShowingLoadingAnimation(): LiveData<Boolean> = isShowingLoadingAnimation

    private val onLibraryRefreshRequested = SingleLiveEvent<Int>()
    fun onLibraryRefreshRequested() {
        // changes the value so any listener will get pinged.
        onLibraryRefreshRequested.value = (onLibraryRefreshRequested.value ?: 0) + 1
    }

    fun getOnLibraryRefreshRequested(): LiveData<Int> = onLibraryRefreshRequested

    private val libraryFilterText = MutableLiveData<String>("")
    fun getLibraryFilterText(): LiveData<String> = libraryFilterText
    fun setLibraryFilterText(query: String) {
        if (this.libraryFilterText.value != query) {
            this.libraryFilterText.value = query
        }
    }

    fun getMutativePosition(): Int = mutativePosition
    fun setMutativePosition(position: Int) {
        mutativePosition = position
    }

    fun getMutativeOffset(): Int = rvMutativeOffset
    fun setMutativeOffset(offset: Int) {
        rvMutativeOffset = offset
    }

    val filteredTag = MutableLiveData<String>()
    fun filterByTag(tag: String, items: List<ListEntry>): List<ListEntry> {
        return items.filter {
            return@filter it.isItem() && it.getItem().tags.map {it.tag}.contains(tag)
        }
    }

    private var isInMyStarPage = false


    fun isStared(item: Item): Boolean {
        return MyItemFilter.get(getApplication()).isStared(item)
    }

    fun isStared(collection: Collection): Boolean {
        return MyItemFilter.get(getApplication()).isStared(collection)
    }

    fun addToStar(collection: Collection) {
        MyItemFilter.get(getApplication()).addToStar(collection)
    }

    fun addToStar(item: Item) {
        MyItemFilter.get(getApplication()).addToStar(item)
    }

    fun removeStar(item: Item) {
        MyItemFilter.get(getApplication()).removeStar(item)
    }

    fun removeStar(collection: Collection) {
        MyItemFilter.get(getApplication()).removeStar(collection)
    }


//    fun onItemOpen(item: Item) {
////        listViewModel.onItemClicked(item)
//    }
//
//    fun onCollectionOpen(collection: Collection) {
////        listViewModel.onCollectionClicked(collection)
//    }
//
//    fun onItemAttachmentOpen(item: Item) {
////        listViewModel.onAttachmentClicked(item)
//    }


}