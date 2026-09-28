package com.example.statussaver

import android.net.Uri

data class StatusItem(
    val name: String,
    val uri: Uri,
    val isVideo: Boolean,
    val size: Long,
    val lastModified: Long
)
