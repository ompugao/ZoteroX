package com.mickstarify.zotero.ZoteroAPI

import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import com.thegrizzlylabs.sardineandroid.impl.handler.InputStreamResponseHandler
import com.thegrizzlylabs.sardineandroid.impl.handler.ResponseHandler
import okhttp3.Headers
import okhttp3.Headers.Companion.toHeaders
import okhttp3.Request
import java.io.IOException
import java.io.InputStream

class MyOkHttpSardine: OkHttpSardine() {

}