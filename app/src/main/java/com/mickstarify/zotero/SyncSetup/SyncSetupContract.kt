package com.mickstarify.zotero.SyncSetup

interface SyncSetupContract {
    interface View {
        fun initUI()
        fun createUnsupportedAlert()
        fun startZoteroAPIActivity()
        fun showHowToZoteroSyncDialog(onProceed: () -> Unit)
        fun createAPIKeyDialog(onKeySubmit: (String) -> Unit)
        fun createAlertDialog(title: String, message: String)
        fun showLoadingAlertDialog(message: String)
        fun hideLoadingAlertDialog()
    }

    interface Presenter {
        fun selectSyncSetup(option: SyncOption)
        fun startZoteroAPISetup()
        fun hasSyncSetup(): Boolean
        fun createNetworkError(message: String)
    }

    interface Model {
        fun setupZoteroAPI()
        fun hasSyncSetup(): Boolean
        fun testAPIKey(apiKey: String)
    }
}