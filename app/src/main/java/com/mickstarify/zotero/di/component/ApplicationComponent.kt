package com.mickstarify.zotero.di.component

import android.content.Context
import com.mickstarify.zotero.AttachmentManager.AttachmentManager
import com.mickstarify.zotero.AttachmentManager.AttachmentManagerModel
import com.mickstarify.zotero.LibraryActivity.Fragments.LibraryListFragment
import com.mickstarify.zotero.LibraryActivity.LibraryActivityModel
import com.mickstarify.zotero.SettingsActivity
import com.mickstarify.zotero.ZoteroAPI.Syncing.SyncManager
import com.mickstarify.zotero.ZoteroStorage.AttachmentStorageManager
import com.mickstarify.zotero.ZoteroStorage.ZoteroDB.ZoteroDB
import com.mickstarify.zotero.di.module.ApplicationModule
import com.mickstarify.zotero.di.module.DatabaseModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(ApplicationModule::class, DatabaseModule::class))
interface ApplicationComponent {
    val context: Context
    fun inject(libraryActivityModel: LibraryActivityModel)
    fun inject(settingsActivity: SettingsActivity)
    fun inject(attachmentManagerModel: AttachmentManagerModel)
    fun inject(attachmentStorageManager: AttachmentStorageManager)
    fun inject(syncManager: SyncManager)
    fun inject(zoteroDB: ZoteroDB)
    fun inject(libraryListFragment: LibraryListFragment)

    fun inject(attachmentManager: AttachmentManager)
}