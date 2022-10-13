package com.mickstarify.zotero.di.module

import android.content.Context
import com.mickstarify.zotero.PreferenceManager
import com.mickstarify.zotero.ZoteroApplication
import com.mickstarify.zotero.ZoteroStorage.AttachmentStorageManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ApplicationModule(val app: ZoteroApplication) {

    @Provides
    fun provideContext(): Context {
        return app
    }

    @Singleton
    @Provides
    fun providesAttachmentStorageManager(
        context: Context,
        preferenceManager: PreferenceManager
    ): AttachmentStorageManager {
        return AttachmentStorageManager(context, preferenceManager)
    }

    @Singleton
    @Provides
    fun providePreferenceManager(context: Context): PreferenceManager {
        return PreferenceManager(context)
    }
}