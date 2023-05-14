package com.mickstarify.zotero.LibraryActivity.ViewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mickstarify.zotero.LibraryActivity.ListEntry

class CustomizedListViewModel(application: Application) : AndroidViewModel(application) {

    var listViewModel: LibraryListViewModel? = null

    private val items = MutableLiveData<List<ListEntry>>()

    fun getItems(): LiveData<List<ListEntry>> = items
    fun setItems(items: List<ListEntry>) {
        this.items.value = items
    }

    fun fetchAllItems() {
//        listViewModel?.

    }

    init {

    }


}