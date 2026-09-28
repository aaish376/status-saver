package com.example.statussaver

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

object SaveHelper {

    sealed class SaveResult {
        object Success : SaveResult()
        object AlreadyExists : SaveResult()
        data class Error(val message: String) : SaveResult()
    }

    /**
     * Streams [item] from its SAF source Uri into a new file inside the [destTreeUri]
     * folder, preserving the original file name. Skips the copy if a file with the
     * same name already exists at the destination.
     */
    suspend fun save(context: Context, item: StatusItem, destTreeUri: Uri): SaveResult =
        withContext(Dispatchers.IO) {
            try {
                val destDir = DocumentFile.fromTreeUri(context, destTreeUri)
                    ?: return@withContext SaveResult.Error("Invalid destination folder")

                if (destDir.findFile(item.name) != null) {
                    return@withContext SaveResult.AlreadyExists
                }

                val mime = if (item.isVideo) "video/mp4" else "image/*"
                val newFile = destDir.createFile(mime, item.name)
                    ?: return@withContext SaveResult.Error("Could not create destination file")

                val input = context.contentResolver.openInputStream(item.uri)
                val output = context.contentResolver.openOutputStream(newFile.uri)

                if (input == null || output == null) {
                    input?.close()
                    output?.close()
                    newFile.delete()
                    return@withContext SaveResult.Error("Could not open file streams")
                }

                input.use { i -> output.use { o -> i.copyTo(o) } }
                SaveResult.Success
            } catch (e: IOException) {
                SaveResult.Error(e.message ?: "Unknown I/O error")
            } catch (e: SecurityException) {
                SaveResult.Error("Permission lost — reselect the folder in Settings")
            }
        }
}
