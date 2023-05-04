package com.mickstarify.zotero.LibraryActivity

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import androidx.appcompat.widget.AppCompatSpinner
//import com.google.firebase.analytics.FirebaseAnalytics
import com.mickstarify.zotero.PreferenceManager
import com.mickstarify.zotero.R
import android.widget.ArrayAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class LibraryFilterMenuDialog(val context: Context, val onFilterChange: (() -> (Unit))) {
    lateinit var preferences: PreferenceManager

    var selected_sorting_method = "UNSET"
    var is_showing_pdf: Boolean = false
    var is_showing_notes: Boolean = false

    fun setSortingMethod(index: Int) {
        try {
            selected_sorting_method =
                context.resources.getStringArray(R.array.sort_options_values)[index]
        } catch (e: ArrayIndexOutOfBoundsException) {
            Log.e("zotero", "Error array out of index for LibraryFilterDialog")
        }
    }

    private fun saveSettings(onlyNotes: Boolean, onlyPDFs: Boolean) {
        preferences.setIsShowingOnlyPdfs(onlyPDFs)
        preferences.setIsShowingOnlyNotes(onlyNotes)

        preferences.setSortMethod(selected_sorting_method)

        val params = Bundle().apply {
            putBoolean("show_pdfs", onlyPDFs)
            putBoolean("only_notes", onlyNotes)
            putString("sort_method", selected_sorting_method)
        }
//        FirebaseAnalytics.getInstance(context).logEvent("set_filter", params)

        onFilterChange()
    }

    private fun getSortString(method: String): String {
        val i = context.resources.getStringArray(R.array.sort_options_values).indexOf(method)
        if (i == -1) {
            val params = Bundle()
            params.putString("method", method)
//            FirebaseAnalytics.getInstance(context).logEvent("error_sort_method_not_found", params)
            return "Error"
        }
        return context.resources.getTextArray(R.array.sort_options_entries)[i].toString()
    }

    var sortingOrderButton: ImageButton? = null

    fun setSortButtonAscending() {
        sortingOrderButton?.apply {
            this.contentDescription = "Sort Descendingly"
            this.setImageResource(R.drawable.ic_arrow_upward_24px)
        }
    }

    fun setSortButtonDescending() {
        sortingOrderButton?.apply {
            this.contentDescription = "Sort descendingly"
            this.setImageResource(R.drawable.ic_arrow_downward_24px)
        }
    }

    fun show() {
        val dialogBuilder = MaterialAlertDialogBuilder(context).create()
        val inflater = LayoutInflater.from(context)
        val dialogView: View = inflater.inflate(R.layout.dialog_filter_menu, null)

        val sortingMethodSpinner = dialogView.findViewById<AppCompatSpinner>(R.id.spinner_sort_by)
        val checkbox_show_only_pdf = dialogView.findViewById<CheckBox>(R.id.checkBox_show_only_pdf)
        val checkbox_show_only_notes =
            dialogView.findViewById<CheckBox>(R.id.checkBox_show_only_notes)

        checkbox_show_only_notes.isChecked = this.is_showing_notes
        checkbox_show_only_pdf.isChecked = this.is_showing_pdf

        val spinnerItems = context.resources.getTextArray(R.array.sort_options_entries)
        val spinnerAdapter = ArrayAdapter(
            this.context,
            R.layout.simple_spinner_item_select, spinnerItems
        )
        sortingMethodSpinner.adapter = spinnerAdapter
        sortingMethodSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                setSortingMethod(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        //设置spinner显示未当前的排序模式
        spinnerItems.forEachIndexed { index, charSequence ->
            if (charSequence.equals(this.getSortString(selected_sorting_method)))
                sortingMethodSpinner.setSelection(index)
        }

        val cancelButton = dialogView.findViewById<Button>(R.id.btn_cancel)
        val submitButton = dialogView.findViewById<Button>(R.id.btn_submit)

        cancelButton.setOnClickListener {
            dialogBuilder.dismiss()
        }

        submitButton.setOnClickListener {
            saveSettings(checkbox_show_only_notes.isChecked, checkbox_show_only_pdf.isChecked)
            dialogBuilder.dismiss()
        }

        sortingOrderButton = dialogView.findViewById<ImageButton>(R.id.button_sort_order)
        if (preferences.isSortedAscendingly()) {
            setSortButtonAscending()
        } else {
            setSortButtonDescending()
        }

        sortingOrderButton?.setOnClickListener {
            if (preferences.isSortedAscendingly()) {
                setSortButtonDescending()
                preferences.setSortDirection(PreferenceManager.SORT_METHOD_DESCENDING)
            } else {
                setSortButtonAscending()
                preferences.setSortDirection(PreferenceManager.SORT_METHOD_ASCENDING)
            }
        }


        dialogBuilder.setView(dialogView)

        //not letting user dismiss dialog because otherwise the keyboard stays and it's a pain to
        //dismiss it. (need to find currentFocusedView, etc)
        dialogBuilder.setCanceledOnTouchOutside(false)

        dialogBuilder.show()
    }


    init {
        preferences = PreferenceManager(context)
        selected_sorting_method =
            preferences.sortMethodToString(preferences.getSortMethod()) //terrible code, i know.
        is_showing_pdf = preferences.getIsShowingOnlyPdfs()
        is_showing_notes = preferences.getIsShowingOnlyNotes()

    }

}