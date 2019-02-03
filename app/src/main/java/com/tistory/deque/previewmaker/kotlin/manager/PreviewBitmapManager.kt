package com.tistory.deque.previewmaker.kotlin.manager

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import com.tistory.deque.previewmaker.kotlin.util.EzLogger
import java.io.FileNotFoundException
import java.io.IOException
import android.media.ExifInterface
import android.R.attr.orientation
import android.graphics.Matrix


object PreviewBitmapManager {
    private const val previewBitmapMaxSize = 2000
    private const val stampBitmapMaxSize = 1000

    var selectedPreviewBitmap : Bitmap? = null
    var selectedStampBitmap : Bitmap? = null

    fun resetManager(){
        selectedPreviewBitmap = null
        selectedStampBitmap = null
    }

    fun stampImageUriToBitmap(imageUri: Uri, context: Context): Bitmap? {
        return imageUriToBitmap(stampBitmapMaxSize, imageUri, context, null)
    }

    fun previewImageUriToBitmap(imageUri: Uri, context: Context, rotation: Int?): Bitmap? {
        return imageUriToBitmap(previewBitmapMaxSize, imageUri, context, rotation)
    }

    private fun imageUriToBitmap(maxSize: Int, imageUri: Uri, context: Context, rotation:Int?): Bitmap? {
        EzLogger.d("rotation : $rotation")
        var bitmap: Bitmap? = null
        var resizedBitmap: Bitmap? = null
        val width: Int
        val height: Int
        val rate: Double

        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri) ?: return null
            width = bitmap.width
            height = bitmap.height
            rate = width.toDouble() / height.toDouble()

            resizedBitmap = if (rate > 1 && width > maxSize) { // w > h
                EzLogger.d("RATE : $rate , W : $maxSize , H : ${(maxSize * (1 / rate)).toInt()}")
                Bitmap.createScaledBitmap(bitmap, maxSize, (maxSize * (1 / rate)).toInt(), true)
            } else if (rate <= 1 && height > maxSize) { // h > w
                EzLogger.d("RATE : $rate , W : ${(maxSize * rate).toInt()} , H : $maxSize")
                Bitmap.createScaledBitmap(bitmap, (maxSize * rate).toInt(), maxSize, true)
            } else {
                bitmap
            }
            EzLogger.d("URI -> Bitmap success : URI : $imageUri")
        } catch (e: FileNotFoundException) {
            EzLogger.d("URI -> Bitmap : URI File not found$imageUri")
            e.printStackTrace()
        } catch (e: IOException) {
            EzLogger.d("URI -> Bitmap : IOException$imageUri")
            e.printStackTrace()
        }

        if(rotation != null && resizedBitmap != null){
            try {
                val matrix = Matrix()
                when (rotation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> {
                        matrix.postRotate(90f)
                        resizedBitmap = Bitmap.createBitmap(resizedBitmap, 0, 0, resizedBitmap.width, resizedBitmap.height, matrix, true)
                    }
                    ExifInterface.ORIENTATION_ROTATE_180 -> {
                        matrix.postRotate(180f)
                        resizedBitmap = Bitmap.createBitmap(resizedBitmap, 0, 0, resizedBitmap.width, resizedBitmap.height, matrix, true)
                    }
                    ExifInterface.ORIENTATION_ROTATE_270 -> {
                        matrix.postRotate(270f)
                        resizedBitmap = Bitmap.createBitmap(resizedBitmap, 0, 0, resizedBitmap.width, resizedBitmap.height, matrix, true)
                    }
                }
            } catch (e: OutOfMemoryError){
                EzLogger.d("out of memory error ...")
                e.printStackTrace()
            }
        }

        return resizedBitmap
    }
}