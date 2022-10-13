package com.mickstarify.zotero.SyncSetup

import android.content.Context
import android.content.SharedPreferences

/**
 * 本地储存Zotero账户信息
 * 当前是使用sharedPreferences明文储存，来保存zotero的用户id、密码以及用户名
 * todo： 修改为其他方式加密储存
 */
class AuthenticationStorage(context: Context) {
    private var sharedPreferences: SharedPreferences =
        context.getSharedPreferences("credentials", Context.MODE_PRIVATE)

    fun hasCredentials(): Boolean {
        return (sharedPreferences.contains("userkey") && sharedPreferences.getString(
            "userkey",
            ""
        ) != "")
    }

    fun getUsername(): String {
        return sharedPreferences.getString("username", "error")!!
    }

    fun setCredentials(username: String, userID: String, userkey: String) {
        val editor = sharedPreferences.edit()
        editor.putString("username", username)
        editor.putString("userID", userID)
        editor.putString("userkey", userkey)
        editor.apply()
    }

    fun getUserKey(): String {
        return sharedPreferences.getString("userkey", "error")!!
    }

    fun getUserID(): String {
        return sharedPreferences.getString("userID", "error")!!
    }

    /**
     * 销毁保存在sharedPreferences的认证信息
     */
    fun destroyCredentials() {
        val editor = sharedPreferences.edit()
        editor.remove("username")
        editor.remove("userID")
        editor.remove("userkey")
        editor.apply()
    }
}