package com.mickstarify.zotero.LibraryActivity.Notes

import com.mickstarify.zotero.ZoteroAPI.Model.Note

interface NoteInteractionListener {
    fun deleteNote(note: Note)
    fun editNote(note: Note)
}