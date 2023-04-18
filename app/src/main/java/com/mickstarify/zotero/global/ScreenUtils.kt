package com.mickstarify.zotero.global

import android.content.Context
import android.content.res.Configuration

object ScreenUtils {

//    /**
//     * 判断是否平板设备，此值不会改变
//     */
//    val isTabletDevice: Boolean by lazy {
//        SystemPropertiesProxy.get(context, "ro.build.characteristics")?.contains("tablet") == true
//    }

    /**
     * 动态判断是否平板窗口
     * 在平板设备上，也可能返回false。如分屏模式下
     * 如想判断物理设备是不是平板，请使用 isTabletDevice
     * @return true:平板,false:手机
     * @see isTabletDevice
     */
    fun isTabletWindow(context: Context): Boolean {
        return context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >=
                Configuration.SCREENLAYOUT_SIZE_LARGE
    }

}