package com.mickstarify.zotero.ZoteroAPI

import com.mickstarify.zotero.ZoteroAPI.Model.CollectionPOJO

data class ZoteroAPICollectionsResponse(
    val isCached: Boolean,
    val collections: List<CollectionPOJO>,
    val totalResults: Int
)