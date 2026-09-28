package com.example.statussaver

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var tvSourcePath: TextView
    private lateinit var tvDestPath: TextView

    private val pickSourceLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            Prefs.setSourceUri(this, it)
            updateLabels()
        }
    }

    private val pickDestLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            Prefs.setDestUri(this, it)
            updateLabels()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        tvSourcePath = findViewById(R.id.tvSourcePath)
        tvDestPath = findViewById(R.id.tvDestPath)

        val btnChooseSource: Button = findViewById(R.id.btnChooseSource)
        val btnChooseDest: Button = findViewById(R.id.btnChooseDest)

        btnChooseSource.setOnClickListener { launchSourcePicker() }
        btnChooseDest.setOnClickListener { pickDestLauncher.launch(null) }

        updateLabels()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    /**
     * Best-effort: opens the system folder picker already pointed at
     * Android/media/com.whatsapp/WhatsApp/Media/.Statuses so the user usually
     * just has to tap "Use this folder". Falls back to the plain picker root
     * if the hint isn't honoured by the device's picker.
     */
    private fun launchSourcePicker() {
        val initialUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                DocumentsContract.buildDocumentUri(
                    "com.android.externalstorage.documents",
                    "primary:Android/media/com.whatsapp/WhatsApp/Media/.Statuses"
                )
            } catch (e: Exception) {
                null
            }
        } else null

        pickSourceLauncher.launch(initialUri)
    }

    private fun updateLabels() {
        val source = Prefs.getSourceUri(this)
        val dest = Prefs.getDestUri(this)
        tvSourcePath.text = source?.let { readablePath(it) } ?: getString(R.string.not_set)
        tvDestPath.text = dest?.let { readablePath(it) } ?: getString(R.string.not_set)
    }

    private fun readablePath(uri: Uri): String {
        val docId = try {
            DocumentsContract.getTreeDocumentId(uri)
        } catch (e: Exception) {
            uri.toString()
        }
        return docId.replace("primary:", "Internal Storage/")
    }
}
