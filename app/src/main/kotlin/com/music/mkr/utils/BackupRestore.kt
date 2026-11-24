/**
 * MKR Music Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.music.mkr.utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.music.mkr.BuildConfig
import com.music.mkr.db.InternalDatabase
import com.music.mkr.playback.MusicService
import com.music.mkr.viewmodels.BackupRestoreViewModel.Companion.SETTINGS_FILENAME
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipFile

internal object BackupRestore {
    private const val PENDING = "pending-backup-restore"
    private val supportedEntries = setOf(SETTINGS_FILENAME, InternalDatabase.DB_NAME)

    suspend fun stage(context: Context, input: InputStream) {
        val work = kotlin.io.path.createTempDirectory(context.filesDir.toPath(), "restore-").toFile()
        try {
            val archive = File(work, "backup.zip")
            archive.outputStream().use { input.copyTo(it) }
            val staged = File(work, "staged").apply { check(mkdir()) }
            ZipFile(archive).use { zip ->
                val entries = zip.entries().asSequence().filter { it.name in supportedEntries }.toList()
                require(entries.isNotEmpty() && entries.distinctBy { it.name }.size == entries.size)
                for (entry in entries) {
                    require(!entry.isDirectory)
                    val file = File(staged, entry.name)
                    val crc = java.util.zip.CRC32()
                    java.util.zip.CheckedInputStream(zip.getInputStream(entry), crc).use { source ->
                        file.outputStream().use { target ->
                            source.copyTo(target)
                            target.flush()
                            (target as? FileOutputStream)?.fd?.sync()
                        }
                    }
                    require(file.length() > 0)
                    if (entry.size >= 0) {
                        require(file.length() == entry.size)
                    }
                    if (entry.crc != -1L && entry.crc != 0L) {
                        require(crc.value == entry.crc)
                    }
                }
            }
            File(staged, SETTINGS_FILENAME).takeIf(File::exists)?.let { file ->
                val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
                try {
                    PreferenceDataStoreFactory.create(scope = scope, produceFile = { file }).data.first()
                } finally {
                    scope.cancel()
                }
            }
            File(staged, InternalDatabase.DB_NAME).takeIf(File::exists)?.let { file ->
                SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY).use { db ->
                    val version = db.version
                    require(version in 1..InternalDatabase.SCHEMA_VERSION)
                    val playlistId = if (version == 1) "playlistId" else "id"
                    db.rawQuery("PRAGMA integrity_check", null).use { cursor ->
                        require(cursor.moveToFirst() && cursor.getString(0) == "ok")
                    }
                    db.rawQuery("SELECT id, title FROM song LIMIT 0", null).use { it.count }
                    db.rawQuery("SELECT $playlistId, name FROM playlist LIMIT 0", null).use { it.count }
                }
                for (suffix in listOf("-wal", "-shm", "-journal")) {
                    val sidecar = File(file.path + suffix)
                    if (sidecar.exists()) {
                        sidecar.delete()
                    }
                }
            }
            currentCoroutineContext().ensureActive()
            val pending = File(context.filesDir, PENDING)
            if (pending.exists()) {
                pending.deleteRecursively()
            }
            if (!staged.renameTo(pending)) {
                staged.copyRecursively(pending, overwrite = true)
                staged.deleteRecursively()
            }
        } finally {
            work.deleteRecursively()
        }
    }

    // Apply before Room, DataStore or playback can open the live files.
    fun applyPending(context: Context): Boolean {
        val pending = File(context.filesDir, PENDING)
        if (!pending.isDirectory) return true
        val db = context.getDatabasePath(InternalDatabase.DB_NAME)
        val files = buildMap {
            if (File(pending, SETTINGS_FILENAME).exists()) {
                put(SETTINGS_FILENAME, File(context.filesDir, "datastore/$SETTINGS_FILENAME"))
            }
            if (File(pending, InternalDatabase.DB_NAME).exists()) {
                put(InternalDatabase.DB_NAME, db)
                put("${db.name}-wal", File("${db.path}-wal"))
                put("${db.name}-shm", File("${db.path}-shm"))
                put("${db.name}-journal", File("${db.path}-journal"))
            }
            for (name in listOf(
                MusicService.PERSISTENT_QUEUE_FILE,
                MusicService.PERSISTENT_AUTOMIX_FILE,
                MusicService.PERSISTENT_PLAYER_STATE_FILE,
            )) put(name, File(context.filesDir, name))
        }
        val originals = File(pending, "originals")
        val saved = File(pending, "originals-saved")
        val rollback = File(pending, "rollback")

        fun finish() {
            runCatching {
                pending.deleteRecursively()
            }
        }

        try {
            if (!saved.exists() && !rollback.exists()) {
                originals.mkdirs()
                for ((name, target) in files) {
                    val original = File(originals, name)
                    if (target.exists() && !original.exists()) {
                        runCatching { target.copyTo(original, overwrite = true) }
                    }
                }
                runCatching { saved.createNewFile() }
            }
            if (!rollback.exists()) {
                for ((name, target) in files) {
                    replace(File(pending, name), target)
                }

                // Restore succeeded! Mark onboarding as completed for this version so user is never sent to onboarding
                runCatching {
                    context.getSharedPreferences("app_state", Context.MODE_PRIVATE)
                        .edit()
                        .putBoolean("has_completed_onboarding", true)
                        .putInt("completed_onboarding_version", BuildConfig.VERSION_CODE)
                        .apply()
                }

                finish()
                return true
            }
        } catch (e: Throwable) {
            Log.e("BackupRestore", "Could not replace backup files. Recovering the originals.", e)
            runCatching { rollback.createNewFile() }
        }

        // Rollback on failure
        for ((name, target) in files) {
            val original = File(originals, name)
            if (saved.exists() || original.exists()) {
                runCatching { replace(original, target) }
            }
        }
        finish()
        return false
    }

    private fun replace(source: File, target: File) {
        if (!source.exists()) {
            if (target.exists()) {
                target.delete()
            }
            return
        }
        target.parentFile?.mkdirs()
        val temporary = File(target.parentFile, ".${target.name}.${System.currentTimeMillis()}.tmp")
        try {
            if (temporary.exists()) temporary.delete()
            source.inputStream().buffered().use { input ->
                temporary.outputStream().buffered().use { output ->
                    input.copyTo(output)
                    output.flush()
                    (output as? FileOutputStream)?.fd?.sync()
                }
            }
            if (!temporary.renameTo(target)) {
                target.delete()
                if (!temporary.renameTo(target)) {
                    source.copyTo(target, overwrite = true)
                }
            }
        } finally {
            if (temporary.exists()) {
                temporary.delete()
            }
        }
    }
}
