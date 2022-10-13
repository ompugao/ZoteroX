package com.mickstarify.zotero.ZoteroAPI.Syncing

import com.mickstarify.zotero.ZoteroStorage.ZoteroDB.ZoteroDB

interface OnSyncChangeListener {
    fun startSyncAnimation(useSmallAnimation: Boolean)
    fun stopSyncAnimation()
    fun createErrorAlert(title: String, message: String, onClick: () -> Unit)
    fun setSyncProgress(progress: Int, total: Int)
    fun makeToastAlert(message: String)
    fun finishLibrarySync(db: ZoteroDB)
}