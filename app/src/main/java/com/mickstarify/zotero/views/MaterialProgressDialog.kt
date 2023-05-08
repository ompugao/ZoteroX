package com.mickstarify.zotero.views

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mickstarify.zotero.databinding.ContentDialogProgressBinding

class MaterialProgressDialog {

    companion object {

        @JvmStatic
        fun createProgressDialog(context: Context, layoutInflater: LayoutInflater, title: String? = null, msg: String): MaterialAlertDialogBuilder {

            val dialogBuilder = MaterialAlertDialogBuilder(context)

            val binding = ContentDialogProgressBinding.inflate(layoutInflater)
            binding.txtContent.text = msg

            if (title.isNullOrEmpty()) dialogBuilder.setTitle(title)

            // Connecting to WebDAV Server
            dialogBuilder
                .setView(binding.root)
//                .setCancelable(false)

            return dialogBuilder
        }

        @JvmStatic
        fun createProgressDialog(activity: Activity, title: String, msg: String): MaterialAlertDialogBuilder {
            return createProgressDialog(activity, activity.layoutInflater, title, msg)
        }

        @JvmStatic
        fun createProgressDialog(fragment: Fragment, title: String, msg: String): MaterialAlertDialogBuilder {
            return createProgressDialog(fragment.requireContext(), fragment.layoutInflater, title, msg)
        }
    }

}