package com.tistory.deque.previewmaker.kotlin.manager

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import androidx.core.content.ContextCompat
import com.tistory.deque.previewmaker.kotlin.util.EtcConstant
import com.tistory.deque.previewmaker.kotlin.util.EzLogger
import java.io.File
import java.io.IOException

object FilePathManager {
    private const val NO_MEDIA_FILE_NAME = ".nomedia"

    fun getStampDirectory(): File {
        val root: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val storageParentDir = File(root, EtcConstant.PREVIEW_SAVED_DIRECTORY)
        val storageDir = File(root.toString() + "/" + EtcConstant.PREVIEW_SAVED_DIRECTORY, EtcConstant.STAMP_SAVED_DIRECTORY)
        EzLogger.d("storageParentDir : $storageParentDir, storageDir : $storageDir")

        if (!storageParentDir.exists()) {
            storageParentDir.mkdirs()
            storageDir.mkdirs()
        }
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        return storageDir
    }

    fun getPreviewDirectory(): File {
        val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val storageDir = File(root, EtcConstant.PREVIEW_SAVED_DIRECTORY)
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        return storageDir
    }

    fun migrateToStampV2(context: Context) {

    }

    fun makeNoMediaFile(context: Context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            // If get file permission
            val storageDir = getStampDirectory()
            val noMediaFile = File(storageDir, NO_MEDIA_FILE_NAME)
            try {
                if (!noMediaFile.exists()) {
                    noMediaFile.createNewFile()
                    EzLogger.d("no media file create")
                }
            } catch (ignore: IOException) {
                EzLogger.d("no media file create error")
                ignore.printStackTrace()
            }
        }
    }
}