/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.playback

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.core.net.toUri
import androidx.media3.datasource.cache.SimpleCache
import com.music.mkr.db.MusicDatabase
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.io.OutputStream

object DownloadDestinationWriter {

    private const val TAG = "DownloadDestination"

    /**
     * Writes completed download bytes from [downloadCache] to the user-selected destination.
     * If a custom SAF folder is selected, it writes to that folder via DocumentsContract.
     * If no custom folder is set, it writes to a user-accessible destination (MediaStore / Music directory).
     * Returns true if the file was successfully written, or false if it failed.
     */
    fun writeCompletedDownload(
        context: Context,
        downloadCache: SimpleCache,
        database: MusicDatabase,
        songId: String,
    ): Boolean {
        return try {
            val fullSong = database.getSongByIdBlocking(songId)
            val song = fullSong?.song
            val artists = fullSong?.artists.orEmpty()
            val format = fullSong?.format

            val artistName = artists.joinToString(", ") { it.name }.ifBlank { "Unknown Artist" }
            val rawTitle = song?.title?.ifBlank { "Track_$songId" } ?: "Track_$songId"

            val safeTitle = rawTitle.replace(Regex("[\\\\/:*?\"<>|]"), "_").trim()
            val safeArtist = artistName.replace(Regex("[\\\\/:*?\"<>|]"), "_").trim()

            val ext = when {
                format?.mimeType?.contains("opus", ignoreCase = true) == true -> "opus"
                format?.mimeType?.contains("flac", ignoreCase = true) == true -> "flac"
                format?.mimeType?.contains("aac", ignoreCase = true) == true -> "m4a"
                format?.mimeType?.contains("mp4", ignoreCase = true) == true -> "m4a"
                else -> "m4a"
            }
            val mimeType = format?.mimeType?.split(";")?.firstOrNull()?.trim() ?: "audio/mp4"
            val fileName = "$safeTitle - $safeArtist.$ext"

            val sp = context.getSharedPreferences("mkr_storage_prefs", Context.MODE_PRIVATE)
            val customFolderUriString = sp.getString("custom_download_uri", null)?.takeIf { it.isNotBlank() }

            if (customFolderUriString != null) {
                writeToCustomFolder(context, downloadCache, songId, customFolderUriString.toUri(), fileName, mimeType)
            } else {
                writeToDefaultDestination(context, downloadCache, songId, fileName, rawTitle, artistName, mimeType)
            }
            Timber.tag(TAG).i("Successfully wrote download for $songId ($fileName)")
            true
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to write download to destination for $songId")
            false
        }
    }

    private fun writeToCustomFolder(
        context: Context,
        downloadCache: SimpleCache,
        songId: String,
        treeUri: Uri,
        fileName: String,
        mimeType: String,
    ) {
        val hasPermission = context.contentResolver.persistedUriPermissions.any {
            it.uri == treeUri && it.isWritePermission
        }
        if (!hasPermission) {
            throw IOException("SAF write permission has been revoked or is missing for $treeUri")
        }

        val docId = DocumentsContract.getTreeDocumentId(treeUri)
        val parentDocUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
        val docUri = DocumentsContract.createDocument(context.contentResolver, parentDocUri, mimeType, fileName)
            ?: throw IOException("Failed to create document $fileName in $treeUri")

        val outputStream = context.contentResolver.openOutputStream(docUri)
            ?: throw IOException("Failed to open output stream for document $docUri")

        outputStream.use { out ->
            if (!copyCachedResource(downloadCache, songId, out)) {
                throw IOException("No cached bytes found in downloadCache for $songId")
            }
        }
    }

    private fun writeToDefaultDestination(
        context: Context,
        downloadCache: SimpleCache,
        songId: String,
        fileName: String,
        title: String,
        artist: String,
        mimeType: String,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Audio.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Audio.Media.TITLE, title)
                put(MediaStore.Audio.Media.ARTIST, artist)
                put(MediaStore.Audio.Media.MIME_TYPE, mimeType)
                put(MediaStore.Audio.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MUSIC}/MKR Music")
                put(MediaStore.Audio.Media.IS_PENDING, 1)
            }
            val uri = context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: throw IOException("Failed to insert MediaStore audio record")

            try {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    if (!copyCachedResource(downloadCache, songId, out)) {
                        throw IOException("No cached bytes found in downloadCache for $songId")
                    }
                } ?: throw IOException("Failed to open output stream for MediaStore uri $uri")

                contentValues.clear()
                contentValues.put(MediaStore.Audio.Media.IS_PENDING, 0)
                context.contentResolver.update(uri, contentValues, null, null)
            } catch (e: Exception) {
                context.contentResolver.delete(uri, null, null)
                throw e
            }
        } else {
            val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
            val mkrDir = musicDir?.resolve("MKR Music") ?: context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            mkrDir?.mkdirs()
            val targetFile = File(mkrDir, fileName)
            targetFile.outputStream().use { out ->
                if (!copyCachedResource(downloadCache, songId, out)) {
                    throw IOException("No cached bytes found in downloadCache for $songId")
                }
            }
        }
    }

    private fun copyCachedResource(
        cache: SimpleCache,
        key: String,
        outputStream: OutputStream,
    ): Boolean {
        val spans = cache.getCachedSpans(key).sortedBy { it.position }
        if (spans.isEmpty()) return false
        for (span in spans) {
            val file = span.file ?: continue
            file.inputStream().use { input ->
                input.copyTo(outputStream)
            }
        }
        outputStream.flush()
        return true
    }
}
