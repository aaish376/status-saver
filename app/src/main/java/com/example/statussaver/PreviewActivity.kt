package com.example.statussaver

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class PreviewActivity : AppCompatActivity() {

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        val uri = intent.getParcelableExtra<Uri>("uri") ?: run { finish(); return }
        val isVideo = intent.getBooleanExtra("isVideo", false)
        val name = intent.getStringExtra("name") ?: "status"

        val imageView: ImageView = findViewById(R.id.previewImage)
        val videoView: VideoView = findViewById(R.id.previewVideo)
        val fabSave: FloatingActionButton = findViewById(R.id.fabSave)

        if (isVideo) {
            imageView.visibility = View.GONE
            videoView.visibility = View.VISIBLE
            videoView.setVideoURI(uri)
            videoView.setOnPreparedListener {
                it.isLooping = true
                videoView.start()
            }
        } else {
            videoView.visibility = View.GONE
            imageView.visibility = View.VISIBLE
            Glide.with(this).load(uri).into(imageView)
        }

        fabSave.setOnClickListener {
            val destUri = Prefs.getDestUri(this)
            if (destUri == null) {
                Snackbar.make(fabSave, R.string.no_dest_configured, Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val item = StatusItem(name, uri, isVideo, 0, 0)
            lifecycleScope.launch {
                val result = SaveHelper.save(this@PreviewActivity, item, destUri)
                val msg = when (result) {
                    is SaveHelper.SaveResult.Success -> getString(R.string.saved_successfully)
                    is SaveHelper.SaveResult.AlreadyExists -> getString(R.string.already_saved)
                    is SaveHelper.SaveResult.Error -> getString(R.string.save_failed, result.message)
                }
                Snackbar.make(fabSave, msg, Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}
