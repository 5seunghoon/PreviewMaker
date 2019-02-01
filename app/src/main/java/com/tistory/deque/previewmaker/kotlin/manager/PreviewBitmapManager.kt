package com.tistory.deque.previewmaker.kotlin.manager

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import com.tistory.deque.previewmaker.Util.Logger
import com.tistory.deque.previewmaker.kotlin.model.Preview
import com.tistory.deque.previewmaker.kotlin.util.EzLogger
import java.io.FileNotFoundException
import java.io.IOException

object PreviewBitmapManager {
    private const val bitmapMaxSize = 2000

    var selectedPreviewBitmap : Bitmap? = null

    fun resetManager(){
        selectedPreviewBitmap = null
    }

    fun imageUriToBitmap(imageUri: Uri, context: Context): Bitmap? {
        val bitmap: Bitmap
        var resizedBitmap: Bitmap? = null
        val width: Int
        val height: Int
        val rate: Double

        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
            width = bitmap.width
            height = bitmap.height
            rate = width.toDouble() / height.toDouble()

            resizedBitmap = if (rate > 1 && width > bitmapMaxSize) { // w > h
                EzLogger.d("RATE : $rate , W : $bitmapMaxSize , H : ${(bitmapMaxSize * (1 / rate)).toInt()}")
                Bitmap.createScaledBitmap(bitmap, bitmapMaxSize, (bitmapMaxSize * (1 / rate)).toInt(), true)
            } else if (rate <= 1 && height > bitmapMaxSize) { // h > w
                EzLogger.d("RATE : $rate , W : ${(bitmapMaxSize * rate).toInt()} , H : $bitmapMaxSize")
                Bitmap.createScaledBitmap(bitmap, (bitmapMaxSize * rate).toInt(), bitmapMaxSize, true)
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

        return resizedBitmap
    }
}