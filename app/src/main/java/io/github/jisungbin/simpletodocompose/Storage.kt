package io.github.jisungbin.simpletodocompose

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object Storage {
    @Suppress("DEPRECATION")
    val sdcard: String = Environment.getExternalStorageDirectory().absolutePath

    private fun String.parsePath() = if (contains(sdcard)) this else "$sdcard/$this"

    fun write(path: String, content: String) {
        val file = File(path.parsePath())
        if (!file.exists()) {
            File(file.path.substringBeforeLast("/")).mkdirs()
            file.createNewFile()
        }
        FileOutputStream(file).use { it.write(content.toByteArray()) }
    }

    fun read(path: String, default: String?): String? {
        return try {
            val file = File(path.parsePath())
            if (file.exists()) {
                FileInputStream(file).bufferedReader().use { it.readText() }
            } else {
                default
            }
        } catch (ignored: Exception) {
            default
        }
    }

    fun append(path: String, prefix: String, appendContent: String) {
        val preContent = read(path, "")
        val newContent = "$preContent$prefix$appendContent"
        write(path, newContent)
    }

    fun delete(path: String) {
        File(path.parsePath()).delete()
    }

    fun deleteAll(path: String) {
        File(path.parsePath()).deleteRecursively()
    }

    fun fileList(path: String) = File(path.parsePath()).listFiles()?.toList() ?: emptyList()

    @RequiresApi(Build.VERSION_CODES.R)
    fun isStorageManagerPermissionGranted() = Environment.isExternalStorageManager()

    @RequiresApi(Build.VERSION_CODES.R)
    fun requestStorageManagePermission(activity: Activity) {
        val intent = Intent(
            Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
            "package:${activity.packageName}".toUri()
        )
        activity.startActivity(intent)
    }
}
