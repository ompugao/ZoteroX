package com.mickstarify.zotero.LibraryActivity.Fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.mickstarify.zotero.LibraryActivity.ViewModels.LibraryLoadingScreenViewModel
import com.mickstarify.zotero.R

class LibraryLoadingScreen : Fragment() {

    companion object {
        fun newInstance() = LibraryLoadingScreen()
    }

    private lateinit var viewModel: LibraryLoadingScreenViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.library_loading_screen_fragment, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel =
            ViewModelProvider(requireActivity()).get(LibraryLoadingScreenViewModel::class.java)

        val textView_message = requireView().findViewById<TextView>(R.id.textView_Library_label)
        val progressBar = requireView().findViewById<ProgressBar>(R.id.progressBar_library)

        progressBar.isIndeterminate = true

        viewModel.getLoadingMessage().observe(viewLifecycleOwner) {
            textView_message.text = it
        }
        viewModel.gettotalAmountOfEntries().observe(viewLifecycleOwner) {
            progressBar.max = it
            progressBar.isIndeterminate = false
            viewModel.setLoadingMessage("正在下载 0 of ${viewModel.gettotalAmountOfEntries().value ?: "?"}")
        }
        viewModel.getAmountOfDownloadedEntries().observe(viewLifecycleOwner) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                progressBar.setProgress(it, true)
            } else {
                progressBar.progress = it
            }
            viewModel.setLoadingMessage("正在下载 ${it} of ${viewModel.gettotalAmountOfEntries().value ?: "?"}")
        }
    }

}