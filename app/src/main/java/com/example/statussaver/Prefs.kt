package com.example.statussaver

import android.content.Context
import android.net.Uri

/**
 * Stores the two user-configurable folder locations:
 *  - source: where WhatsApp keeps its statuses (a SAF tree URI)
 *  - dest:   where saved statuses should be copied to (a SAF tree URI)
 */
object Prefs {
    private const val PREFS_NAME = "status_saver_prefs"
    private const val KEY_SOURCE_URI = "source_uri"
    private const val KEY_DEST_URI = "dest_uri"

    fun getSourceUri(context: Context): Uri? =
        prefs(context).getString(KEY_SOURCE_URI, null)?.let { Uri.parse(it) }

    fun setSourceUri(context: Context, uri: Uri?) {
        prefs(context).edit().putString(KEY_SOURCE_URI, uri?.toString()).apply()
    }

    fun getDestUri(context: Context): Uri? =
        prefs(context).getString(KEY_DEST_URI, null)?.let { Uri.parse(it) }

    fun setDestUri(context: Context, uri: Uri?) {
        prefs(context).edit().putString(KEY_DEST_URI, uri?.toString()).apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
