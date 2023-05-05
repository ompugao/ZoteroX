package com.mickstarify.zotero.LibraryActivity.Notes

import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mickstarify.zotero.R
import com.mickstarify.zotero.ZoteroAPI.Model.Note


class NoteView(val context: FragmentActivity, val note: Note, val listener: NoteInteractionListener) {

    private lateinit var edtNote: EditText

    private var dialog: AlertDialog? = null

//    private var noteText = Html.fromHtml(note.note)

//    private val isNoteEditable: MutableLiveData<Boolean> = MutableLiveData()

    private val isNoteChanged = MutableLiveData<Boolean>()


    fun show() {
//        val binding = LayoutDialogNoteEditBinding.inflate((context as Activity).layoutInflater)

        edtNote = EditText(ContextThemeWrapper(context, R.style.EditText_ReadOnly))
        edtNote.setText(Html.fromHtml(note.note))

        dialog = MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.note))
            .setView(edtNote)
            .setCancelable(false)
            .setNegativeButton(context.getString(R.string.cancel)) { _, _ -> }
            .setPositiveButton(context.getString(R.string.edit)) { _, _ -> onPositiveButtonClick() }
            .show()

        dialog?.getButton(AlertDialog.BUTTON_POSITIVE)
            ?.setOnClickListener {
                enableNoteEdit()
            }

        edtNote.background = null
        edtNote.isFocusableInTouchMode = false

        edtNote.layoutParams?.let {
            (it as FrameLayout.LayoutParams).setMargins(40, 0, 40, 0)
        }

        edtNote.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                isNoteChanged.value = true

            }
        })

        isNoteChanged.observe(context) {
            if (it) {
                val button = dialog?.getButton(AlertDialog.BUTTON_POSITIVE)
                button?.text = context.getString(R.string.save)
            }
        }
    }

    private fun onPositiveButtonClick() {
        if (isNoteChanged.value!!) {
            note.note = edtNote.text.toString()
            listener.editNote(note)
        } else {
            enableNoteEdit()
        }
    }

    private fun enableNoteEdit() {
        edtNote.isFocusableInTouchMode = true
    }

    private fun disableNoteEdit() {
        edtNote.isFocusableInTouchMode = true
    }
}