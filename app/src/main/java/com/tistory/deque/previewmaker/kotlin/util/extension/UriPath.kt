package com.tistory.deque.previewmaker.kotlin.util.extension

import android.content.ContentResolver
import android.content.ContentUris
import android.database.CursorIndexOutOfBoundsException
import android.net.Uri
import android.provider.MediaStore
import com.tistory.deque.previewmaker.kotlin.util.EzLogger

/**
 * path -> File : use File(path)
 * File -> uri : use Uri.fromFile(file)
 * uri -> path : use Uri.getRealPath()
 *
 * 만약 path로 부터 file:///storage/emulated/0/Pictures/Preview%20Maker/PREVIEW_20190201151124479.png 과 같은 Uri를 얻어오고 싶을 때 -> Uri.fromFile(File(path))
 * 만약 paht로 부터 content:///media/external/images/media/24323.. 과 같은 Uri를 얻고 싶을 때 -> path.getUri(contentResolver)
 *
 * 만약 Uri가 file://로 시작하는 uri일때 path를 얻고 싶으면 -> Uri.path
 * 만약 Uri가 content://로 시작하는 Uri일때 path를 얻고 싶으면 -> Uri.getRealPath()
 */

fun Uri.getRealPath(contentResolver: ContentResolver): String? {
    val proj = arrayOf(MediaStore.Images.Media.DATA)

    contentResolver.query(this, proj, null, null, null)?.use { cursor ->
        cursor.moveToNext()
        try {
            val path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA))
            EzLogger.d("getRealPath(), from uri: $this, path : $path")
            return path
        } catch (e: CursorIndexOutOfBoundsException) {
            return null
        }
    }

    return null

}

fun String.getUri(contentResolver: ContentResolver): Uri? {
    contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, "_data = '$this'", null, null)?.use { cursor ->
        try {
            cursor.moveToNext()
            val id = cursor.getInt(cursor.getColumnIndex("_id"))
            return ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toLong())
        } catch (e: CursorIndexOutOfBoundsException) {
            return null
        }
    }

    return null
}