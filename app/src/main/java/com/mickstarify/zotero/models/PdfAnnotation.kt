package com.mickstarify.zotero.models

import android.graphics.pdf.PdfDocument
import com.mickstarify.zotero.ZoteroStorage.Database.ItemData

data class PdfAnnotation(var key: String,
                         var parentItemKey: String,
                         var pageLabel: Int,
                         var color: String,
                         var position: String,
                         var type: String) {

    var text: String = ""

    var comment: String = ""

    var dateAdded: String = ""

    var dateModified: String = ""

    var sortIndex = ""

    companion object {

        @JvmStatic
        fun parse(data: List<ItemData>): PdfAnnotation {
            val map = HashMap<String, String>()

            data.forEach {
                map[it.name] = it.value
            }

            val key = map["key"] ?:""
            val parentItem = map["parentItem"] ?:""
            val annotationPageLabel = map["annotationPageLabel"]?.toIntOrNull()  ?:-1
            val annotationColor = map["annotationColor"] ?:""
            val annotationPosition = map["annotationPosition"] ?:""
            val annotationType = map["annotationType"] ?:""
            val annotationText = map["annotationText"] ?:""
            val dateAdded = map["dateAdded"] ?:""
            val dateModified = map["dateModified"] ?:""
            val annotationSortIndex = map["annotationSortIndex"] ?:""
            val annotationComment = map["annotationComment"] ?:""

            val annotation = PdfAnnotation(key, parentItem, annotationPageLabel, annotationColor, annotationPosition, annotationType)
            annotation.text = annotationText
            annotation.comment = annotationComment
            annotation.dateAdded = dateAdded
            annotation.dateModified = dateModified
            annotation.sortIndex = annotationSortIndex
            return annotation
        }
    }

}