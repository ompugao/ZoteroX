package com.mickstarify.zotero.LibraryActivity.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mickstarify.zotero.LibraryActivity.ListEntry
import com.mickstarify.zotero.MyLog
import com.mickstarify.zotero.ZoteroStorage.Database.Collection
import com.mickstarify.zotero.ZoteroStorage.Database.Item
import com.mickstarify.zotero.global.SingleLiveEvent

class LibraryListViewModel : ViewModel() {
    private val items = MutableLiveData<List<ListEntry>>()


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



}