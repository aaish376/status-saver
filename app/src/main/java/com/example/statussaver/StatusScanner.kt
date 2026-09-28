package com.example.statussaver

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object StatusScanner {

    private val imageExt = setOf("jpg", "jpeg", "png", "webp")
    private val videoExt = setOf("mp4", "3gp", "mkv")

    /**
     * Lists the contents of [treeUri] (the folder the user picked, e.g. the WhatsApp
     * .Statuses folder) and splits them into images / videos. Runs off the main thread
     * since listFiles() on a SAF tree does I/O.
     */
    suspend fun scan(context: Context, treeUri: Uri): Pair<List<StatusItem>, List<StatusItem>> =
        withContext(Dispatchers.IO) {
            val images = mutableListOf<StatusItem>()
            val videos = mutableListOf<StatusItem>()

            val root = DocumentFile.fromTreeUri(context, treeUri)
            val files = root?.listFiles() ?: emptyArray()

            for (file in files) {
                if (!file.isFile) continue
                val name = file.name ?: continue
                val ext = name.substringAfterLast('.', "").lowercase()

                val item = StatusItem(
                    name = name,
                    uri = file.uri,
                    isVideo = ext in videoExt,
                    size = file.length(),
                    lastModified = file.lastModified()
                )

                when (ext) {
                    in imageExt -> images.add(item)
                    in videoExt -> videos.add(item)
                }
            }

            images.sortByDescending { it.lastModified }
            videos.sortByDescending { it.lastModified }

            Pair(images, videos)
        }
}
