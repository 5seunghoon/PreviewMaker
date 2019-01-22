package com.tistory.deque.previewmaker.kotlin.util.extension

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import com.tistory.deque.previewmaker.kotlin.util.EzLogger

fun Uri.getRealPath(contentResolver: ContentResolver): String? {
    val proj = arrayOf(MediaStore.Images.Media.DATA)

    contentResolver.query(this, proj, null, null, null)?.let { cursor ->
        cursor.moveToNext()

        val path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA))
        EzLogger.d("getRealPath(), from uri: $this, path : $path")
        cursor.close()
        return path
    }

    return null

}

fun String.getUri(contentResolver: ContentResolver): Uri? {
    contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, "_data = '$this'", null, null)?.let { cursor ->

        cursor.moveToNext()
        val id = cursor.getInt(cursor.getColumnIndex("_id"))

        return ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toLong())
    }

    return null
}