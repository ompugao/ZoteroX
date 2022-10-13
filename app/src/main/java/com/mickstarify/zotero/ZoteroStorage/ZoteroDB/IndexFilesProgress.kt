package com.mickstarify.zotero.ZoteroStorage.ZoteroDB

data class IndexFilesProgress(
    val currentIndex: Int,
    val totalNumber: Int,
    val currentFilename: String
)