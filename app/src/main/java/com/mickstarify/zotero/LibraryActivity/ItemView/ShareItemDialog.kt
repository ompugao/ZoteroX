package com.mickstarify.zotero.LibraryActivity.ItemView

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mickstarify.zotero.R
import com.mickstarify.zotero.ZoteroStorage.Database.Item
import java.util.HashMap
import java.util.LinkedList

class ShareItemDialog(item: Item) {

    // maps name to a isChecked flag
    val isChecked = HashMap<String, Boolean>()
    val pairs = getShareableParameters(item)

    fun buildShareText(): String {
        var string = ""
        for ((name, value) in pairs) {
            if (isChecked[name] == true) {
                string += "$name: ${value.trim()}"
                if (string != "") {
                    string += "; \n"
                }
            }
        }
        Log.d("zotero", "built $string")
        return string
    }

    fun show(context: Context?, shareItemListener: onShareItemListener?) {
        if (context == null) {
            Log.e("zotero", "Error, got null context on share item.")
            return
        }

        val dialogBuilder = MaterialAlertDialogBuilder(context).create()
        val inflater = LayoutInflater.from(context)
        val dialogView: View = inflater.inflate(R.layout.dialog_share_item, null)

        val previewTextView = dialogView.findViewById<EditText>(R.id.edittext_share_preview)

        val checkboxLayout = dialogView.findViewById<LinearLayout>(R.id.linearlayout_checkbox)
        for ((name, value) in pairs) {
            val checkbox = CheckBox(context)
            this.isChecked[name] = false
            checkbox.setText(name)
            if (name == "Title" || name == "Date" || name == "Author") {
                this.isChecked[name] = true
                checkbox.isChecked = true
            }
            checkbox.setOnClickListener { v ->
                Log.d("zotero", "pressed $name - $value")
                this.isChecked[name] = !this.isChecked[name]!!
                previewTextView.setText(buildShareText())
            }
            checkboxLayout.addView(checkbox)
        }

        dialogBuilder.setButton(AlertDialog.BUTTON_POSITIVE, "分享") {
            _, _ ->
            shareItemListener?.shareItem(previewTextView.text.toString())

        }

        dialogBuilder.setButton(AlertDialog.BUTTON_NEGATIVE, "取消") {
                _, _ ->
            dialogBuilder.dismiss()
        }
        
        previewTextView.setText(buildShareText())
//        dialogBuilder.setTitle("Create Shareable Text")
        dialogBuilder.setView(dialogView)
        dialogBuilder.show()
    }

    fun getShareableParameters(item: Item): List<Pair<String, String>> {
        /* Returns a list of (titles, values) for a given item. */

        // we will specify these manually so they appear on the top.
        val pairs = LinkedList<Pair<String, String>>()
        if (item.data.containsKey("title")) {
            pairs.add(Pair("Title", item.data["title"] ?: "none"))
        }
        pairs.add(Pair("Author", item.getAuthor()))
        if (item.data.containsKey("date") && item.data["date"] != "") {
            pairs.add(Pair("Date", item.data["date"] ?: "none"))
        }

        // the rest of the data will be based on what's available.
        for ((name, value) in item.data) {
            if (name != "title" && name != "date" && name != "author" && name != "key") {
                if (value != "") {
                    pairs.add(Pair(name, value))
                }
            }
        }
        return pairs
    }
}