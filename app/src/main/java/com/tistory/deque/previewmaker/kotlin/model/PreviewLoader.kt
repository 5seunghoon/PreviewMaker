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
import com.tistory.deque.previewmaker.kotlin.manager.PreviewBitmapManager
import com.tistory.deque.previewmaker.kotlin.util.EzLogger
import com.tistory.deque.previewmaker.kotlin.util.extension.getUri
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.toObservable
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.concurrent.TimeUnit

object PreviewLoader {
    private fun getThumbnailSize(context: Context): Size {
        val thumbnailBitmapSize: Int = context.resources.run {
            (getDimension(R.dimen.thumbnail_item_image_width_height) * displayMetrics.density).toInt()
        }
        return Size(thumbnailBitmapSize, thumbnailBitmapSize)
    }

    fun loadPreview(context: Context, previewPathList: ArrayList<String>): Observable<Preview> {
        return previewPathList
                .toObservable()
                .flatMap { getPreviewObservable(context, it) }
    }

    fun loadPreviewBitmap(context: Context, preview: Preview, stamp: Stamp?): Observable<Bitmap> {
        return Observable.zip(
                Observable.fromCallable {
                    if (PreviewBitmapManager.selectedStampBitmap == null) {
                        PreviewBitmapManager.selectedStampBitmap = PreviewBitmapManager.stampImageUriToBitmap(stamp?.imageUri
                                ?: return@fromCallable null, context)
                    }
                    return@fromCallable PreviewBitmapManager.selectedStampBitmap
                },
                Observable.fromCallable {
                    try {
                        return@fromCallable preview.getBitmap(context)
                    } catch(e: IOException) {
                        error(context.getString(R.string.error_might_file_not_found))
                    }
                },
                BiFunction { _, previewBitmap -> previewBitmap }
        )
    }

    private fun getPreviewObservable(context: Context, previewPath: String): Observable<Preview> {
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
                    thumbnailBitmap = getThumbnailBitmap(context, contentsUri)
            )
        }
    }

    private fun getThumbnailBitmap(context: Context, contentsUri: Uri): Bitmap? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getThumbnailBitmapFromUri(context, contentsUri)
        } else {
            getThumbnailBitmapFromUriUnder29(context, contentsUri)
        }?.copy(Bitmap.Config.RGB_565, true)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getThumbnailBitmapFromUri(context: Context, contentsUri: Uri): Bitmap? {
        EzLogger.d("imageUri : $contentsUri")
        return context.contentResolver.loadThumbnail(contentsUri, getThumbnailSize(context), null)
    }

    private fun getThumbnailBitmapFromUriUnder29(context: Context, contentsUri: Uri): Bitmap? {
        val rowId = (contentsUri.lastPathSegment)?.toLongOrNull() ?: return null
        EzLogger.d("imageUri : $contentsUri, rowId : $rowId")
        return MediaStore.Images.Thumbnails.getThumbnail(context.contentResolver, rowId, MediaStore.Images.Thumbnails.MINI_KIND, null)
    }
}