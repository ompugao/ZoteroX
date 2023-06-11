package com.mickstarify.zotero

import android.app.Application
import com.mickstarify.zotero.ZoteroStorage.AttachmentStorageManager
import com.mickstarify.zotero.ZoteroStorage.ZoteroDB.ZoteroDB
import com.mickstarify.zotero.di.component.ApplicationComponent
import com.mickstarify.zotero.di.component.DaggerApplicationComponent
import com.mickstarify.zotero.di.module.ApplicationModule
import com.yuan.library.dmanager.download.DownloadManager

class ZoteroApplication : Application() {

    lateinit var component: ApplicationComponent

    var zoteroDB: ZoteroDB? = null

//    var attachmentStorageManager: AttachmentStorageManager? = null

    override fun onCreate() {
        super.onCreate()

        // 初始化全局日志类
        MyLog.init()

        // 在Application初始化下载管理器
        DownloadManager.getInstance().init(this, 3)

//        SoLoader.init(this, false)
//
//        if (BuildConfig.DEBUG && FlipperUtils.shouldEnableFlipper(this)) {
//            val client = AndroidFlipperClient.getInstance(this)
//            client.addPlugin(InspectorFlipperPlugin(this, DescriptorMapping.withDefaults()))
//            client.addPlugin(DatabasesFlipperPlugin(this));
//            client.start()
//        }

        component = DaggerApplicationComponent.builder().applicationModule(
            ApplicationModule(this)
        ).build()
    }

}
