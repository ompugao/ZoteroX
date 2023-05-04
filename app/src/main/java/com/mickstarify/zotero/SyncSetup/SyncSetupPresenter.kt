package com.mickstarify.zotero.SyncSetup

import android.app.ProgressDialog
import android.content.Context
import com.mickstarify.zotero.R

class SyncSetupPresenter(private val view: SyncSetupContract.View, val context: Context) :
    SyncSetupContract.Presenter {
    override fun createNetworkError(message: String) {
        view.createAlertDialog("Network Error", message)
    }

    override fun hasSyncSetup(): Boolean {
        return model.hasSyncSetup()
    }

    override fun startZoteroAPISetup() {
        view.startZoteroAPIActivity()
    }

    override fun selectSyncSetup(option: SyncOption) {
        when (option) {
            SyncOption.ZoteroAPI -> {
                model.setupZoteroAPI()
//                view.showHowToZoteroSyncDialog({
//                    model.setupZoteroAPI()
//                })
            }
            SyncOption.ZoteroAPIManual -> {
                if (model.apiKey.isNotEmpty()) {
                    model.testAPIKey(model.apiKey)
                } else {
                    // Please input your zotero apikey
                    view.createAlertDialog("Error", context.getString(R.string.please_input_zotero_apikey))
                }

//                view.createAPIKeyDialog({ apiKey: String ->
//                    model.testAPIKey(apiKey)
//                })
            }
            else -> view.createUnsupportedAlert()
        }
    }

    private val model = SyncSetupModel(this, context)

    private lateinit var progressDialog: ProgressDialog

    fun setApiKey(apikey: String) {
        model.apiKey = apikey
    }

    fun showLoadingAlertDialog(message: String) {
        view.showLoadingAlertDialog(message)
    }

    fun hideLoadingAlertDialog() {
        view.hideLoadingAlertDialog()
    }

    init {
        view.initUI()
    }


}