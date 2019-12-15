package com.tistory.deque.previewmaker.kotlin.model

import android.content.Context
import android.graphics.Bitmap
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.annotation.RequiresApi
import com.tistory.deque.previewmaker.R
import com.tistory.deque.previewmaker.kotlin.util.EzLogger
import com.tistory.deque.previewmaker.kotlin.util.extension.getUri
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.toObservable
import java.io.File
import java.io.FileNotFoundException
import java.util.concurrent.TimeUnit

class PreviewLoader(private val context: Context) {

    companion object {
        private const val THUMBNAIL_LOADING_DELAY_MILLISECONDS = 20L
    }

    private val thumbnailBitmapSize: Int = context.resources.run {
        (getDimension(R.dimen.thumbnail_item_image_width_height) * displayMetrics.density).toInt()
    }
    private val thumbnailSize = Size(thumbnailBitmapSize, thumbnailBitmapSize)

    fun loadPreview(previewPathList: ArrayList<String>): Observable<Preview> {
        return Observable.zip(
                previewPathList.toObservable().flatMap { getPreviewObservable(it) },
                Observable.interval(THUMBNAIL_LOADING_DELAY_MILLISECONDS, TimeUnit.MILLISECONDS), // Time interval between each preview
                BiFunction { t1: Preview, _: Long -> t1 }
        )
    }

    private fun getPreviewObservable(previewPath: String): Observable<Preview> {
        return Observable.fromCallable {
            val originalUri: Uri = Uri.fromFile(File(previewPath))
            val contentsUri = previewPath.getUri(context.contentResolver) ?: originalUri

            val rotation = try {
                ExifInterface(previewPath).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
            } catch (e: FileNotFoundException) {
                ExifInterface.ORIENTATION_UNDEFINED
            }

            return@fromCallable Preview(
                    originalUri,
                    contentsUri = contentsUri,
                    rotation = rotation,
                    thumbnailBitmap = getThumbnailBitmap(contentsUri)
            )
        }
    }

    private fun getThumbnailBitmap(contentsUri: Uri): Bitmap? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getThumbnailBitmapFromUri(contentsUri)
        } else {
            getThumbnailBitmapFromUriUnder29(contentsUri)
        }?.copy(Bitmap.Config.RGB_565, true)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getThumbnailBitmapFromUri(contentsUri: Uri): Bitmap? {
        EzLogger.d("imageUri : $contentsUri")
        return context.contentResolver.loadThumbnail(contentsUri, thumbnailSize, null)
    }

    private fun getThumbnailBitmapFromUriUnder29(contentsUri: Uri): Bitmap? {
        val rowId = (contentsUri.lastPathSegment)?.toLongOrNull() ?: return null
        EzLogger.d("imageUri : $contentsUri, rowId : $rowId")
        return MediaStore.Images.Thumbnails.getThumbnail(context.contentResolver, rowId, MediaStore.Images.Thumbnails.MINI_KIND, null)
    }
}