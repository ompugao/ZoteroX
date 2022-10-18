package com.mickstarify.zotero

import android.app.Application
//import com.facebook.flipper.android.AndroidFlipperClient
//import com.facebook.flipper.android.utils.FlipperUtils
//import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin
//import com.facebook.flipper.plugins.inspector.DescriptorMapping
//import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
//import com.facebook.soloader.SoLoader
import com.mickstarify.zotero.di.component.ApplicationComponent
import com.mickstarify.zotero.di.component.DaggerApplicationComponent
import com.mickstarify.zotero.di.module.ApplicationModule

class ZoteroApplication : Application() {

    lateinit var component: ApplicationComponent

    override fun onCreate() {
        super.onCreate()

        // 初始化全局日志类
        MyLog.init()

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
