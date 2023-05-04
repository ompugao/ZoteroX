package com.mickstarify.zotero.SyncSetup

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
//import com.google.firebase.analytics.FirebaseAnalytics
import com.mickstarify.zotero.R
import com.mickstarify.zotero.SyncSetup.ZoteroAPISetup.ZoteroAPISetup
import com.mickstarify.zotero.databinding.ActivitySyncSetupBinding
import com.mickstarify.zotero.databinding.ContentDialogProgressBinding

class SyncSetupView : AppCompatActivity(), SyncSetupContract.View {
    override fun createAlertDialog(title: String, message: String) {
        Log.e("zotero", "got error $title - $message")
        val alert = MaterialAlertDialogBuilder(this)
        alert.setIcon(R.drawable.ic_error_black_24dp)
        alert.setTitle(title)
        alert.setMessage(message)
        alert.setPositiveButton("Ok") { _, _ -> }
        alert.show()
    }

    override fun createAPIKeyDialog(onKeySubmit: (String) -> Unit) {
        val dialogBuilder = MaterialAlertDialogBuilder(this)
            .setTitle("Enter your API Key")

        val textBox: EditText = EditText(this)
        textBox.inputType = InputType.TYPE_CLASS_TEXT
        dialogBuilder.setView(textBox)
            .setPositiveButton("Submit", { _, _ ->
                onKeySubmit(textBox.text.toString())
            })
            .setNegativeButton("Cancel", { _, _ -> })

        dialogBuilder.show()
        textBox.requestFocus()
    }

//    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun showHowToZoteroSyncDialog(onProceed: () -> Unit) {
        val dialog = MaterialAlertDialogBuilder(this)
        dialog.setTitle("How to access")
        dialog.setMessage(getString(R.string.description_login_with_zotero_account))
        dialog.setPositiveButton(
            "Got it!"
        ) { _, _ -> onProceed() }
        dialog.show()
    }

    override fun createUnsupportedAlert() {
        val alert = MaterialAlertDialogBuilder(this@SyncSetupView)
        alert.setIcon(R.drawable.ic_error_black_24dp)
        alert.setTitle("Unsupported Syncing Option")
        alert.setMessage(
            "Sorry I have not yet implemented this syncing option yet!\n" +
                    "Currently there is only support for the Zotero API"
        )
        alert.setPositiveButton("Ok") { _, _ ->
            val button_proceed = findViewById<Button>(R.id.btn_sync_proceed)
            button_proceed.isEnabled = false
        }
        alert.show()
    }

    override fun startZoteroAPIActivity() {
        val intent = Intent(this, ZoteroAPISetup::class.java)
        startActivity(intent)
    }

    var selected_provider = SyncOption.Unset

    override fun initUI() {
        val btnProceed = mBinding.btnSyncProceed
        val txtDescription = mBinding.txtDescription

        mBinding.edtApikey.addTextChangedListener (object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                presenter.setApiKey(p0.toString())
            }
        })

        mBinding.radiogroupCloudproviders.addOnButtonCheckedListener {  group, checkId, isChecked ->
            when (checkId) {
                R.id.radio_zotero -> {
                    selected_provider = SyncOption.ZoteroAPI

                    txtDescription.text = getString(R.string.description_login_with_zotero_account)

                    hideApiKeyEditView()
                }
                R.id.radio_zotero_manual_apikey -> {
                    selected_provider = SyncOption.ZoteroAPIManual

                    txtDescription.text = getString(R.string.description_login_with_apikey)
                    showApiKeyEditView()
                }
                else -> throw Exception("Error, not sure what Radiobox was pressed")
            }
            btnProceed.isEnabled = true

        }

        btnProceed.setOnClickListener {
            if (selected_provider != SyncOption.Unset) {
                presenter.selectSyncSetup(selected_provider)
            }
        }

    }

    private lateinit var presenter: SyncSetupPresenter

    private lateinit var mBinding: ActivitySyncSetupBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_sync_setup)

        presenter = SyncSetupPresenter(this, this)
    }

    override fun onResume() {
        if (presenter.hasSyncSetup()) {
            finish()
        }
        super.onResume()
    }

    fun showApiKeyEditView() {
        mBinding.edtApikey.visibility = View.VISIBLE
    }

    fun hideApiKeyEditView() {
        mBinding.edtApikey.visibility = View.GONE
    }

    private var progressDialog: AlertDialog? = null
    override fun showLoadingAlertDialog(message: String) {
        val dialogBuilder = MaterialAlertDialogBuilder(this)

        val binding = ContentDialogProgressBinding.inflate(layoutInflater)
        binding.txtContent.text = message

        progressDialog = dialogBuilder?.setView(binding.root)
            .setCancelable(false)
            .show()
    }

    override fun hideLoadingAlertDialog() {
        progressDialog?.dismiss()
        progressDialog = null
    }


}
