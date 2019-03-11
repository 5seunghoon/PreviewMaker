package com.tistory.deque.previewmaker.kotlin.manager

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.tistory.deque.previewmaker.kotlin.util.EzLogger
import java.io.FileNotFoundException
import java.io.IOException
import android.media.ExifInterface
import android.R.attr.orientation
import android.graphics.*
import java.lang.IllegalArgumentException
import java.util.ArrayList
import kotlin.math.roundToInt


object PreviewBitmapManager {
    private const val previewBitmapMaxSize = 2000
    private const val stampBitmapMaxSize = 1000

    var selectedPreviewBitmap: Bitmap? = null
    var selectedStampBitmap: Bitmap? = null
    var blurredPreviewBitmap: Bitmap? = null

    var smallRatePreviewWithCanvas : Double = 1.0 // 캔버스에 프리뷰를 그릴 때 얼마나 축소하는지

    fun resetManager() {
        selectedPreviewBitmap = null
        selectedStampBitmap = null
    }

    fun stampImageUriToBitmap(imageUri: Uri, context: Context): Bitmap? {
        return imageUriToBitmap(stampBitmapMaxSize, imageUri, context, null)
    }

    fun previewImageUriToBitmap(imageUri: Uri, context: Context, rotation: Int?): Bitmap? {
        return imageUriToBitmap(previewBitmapMaxSize, imageUri, context, rotation)
    }

    private fun imageUriToBitmap(maxSize: Int, imageUri: Uri, context: Context, rotation: Int?): Bitmap? {
        EzLogger.d("rotation : $rotation")
        var bitmap: Bitmap? = null
        var resizedBitmap: Bitmap? = null
        val width: Int
        val height: Int
        val rate: Double

        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
                    ?: return null
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

        if (rotation != null && resizedBitmap != null) {
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
            } catch (e: OutOfMemoryError) {
                EzLogger.d("out of memory error ...")
                e.printStackTrace()
            }
        }

        return resizedBitmap
    }

    /**
     * 비트맵을 어느 좌표에 어떤 크기로 그릴지 결정
     * @return previewPosWidth, previewPosHeight, previewWidth, previewHeight
     */
    fun getResizedBitmapElements(previewBitmapWidth: Int, previewBitmapHeight: Int, canvasWidth: Int, canvasHeight: Int): ArrayList<Int> {
        val bitmapRate: Double = previewBitmapWidth.toDouble() / previewBitmapHeight.toDouble()
        val canvasRate: Double = canvasWidth.toDouble() / canvasHeight.toDouble()

        EzLogger.d("""
            bitmapRate: $bitmapRate, canvasRate : $canvasRate,
            previewBitmapWidth : $previewBitmapWidth, previewBitmapHeight : $previewBitmapHeight
            canvasWidth: $canvasWidth, canvasHeight : $canvasHeight
        """.trimIndent())

        val changedPreviewPosWidth: Int
        val changedPreviewPosHeight: Int
        val changedPreviewBitmapWidth: Int
        val changedPreviewBitmapHeight: Int

        val elements = ArrayList<Int>()

        EzLogger.d("getResizedBitmapElements canvas w, h : $canvasWidth, $canvasHeight")

        if (bitmapRate >= canvasRate && previewBitmapWidth >= canvasWidth) { // w > h
            EzLogger.d("getResizedBitmapElements case 1")

            changedPreviewPosWidth = 0
            changedPreviewPosHeight = (canvasHeight - (canvasWidth * (1 / bitmapRate)).toInt()) / 2
            changedPreviewBitmapWidth = canvasWidth
            changedPreviewBitmapHeight = (canvasWidth * (1 / bitmapRate)).toInt()

        } else if (bitmapRate < canvasRate && previewBitmapHeight >= canvasHeight) { // w < h
            EzLogger.d("getResizedBitmapElements case 2")

            changedPreviewPosWidth = (canvasWidth - (canvasHeight * bitmapRate).toInt()) / 2
            changedPreviewPosHeight = 0
            changedPreviewBitmapWidth = (canvasHeight * bitmapRate).toInt()
            changedPreviewBitmapHeight = canvasHeight

        } else {
            EzLogger.d("getResizedBitmapElements case 3")

            changedPreviewPosWidth = (canvasWidth - previewBitmapWidth) / 2
            changedPreviewPosHeight = (canvasHeight - previewBitmapHeight) / 2
            changedPreviewBitmapWidth = previewBitmapWidth
            changedPreviewBitmapHeight = previewBitmapHeight

        }

        elements.add(changedPreviewPosWidth)
        elements.add(changedPreviewPosHeight)
        elements.add(changedPreviewBitmapWidth)
        elements.add(changedPreviewBitmapHeight)

        return elements
    }

    /**
     * 비트맵의 부분을 블러링, 원본 비트맵을 기준으로 블러링함
     * left < right, top < bottom 이 여야함
     *
     * @param left   이하는 원본 비트맵을 기준으로 한 Oval의 값들. originalBlurOvalToResizedBlurOval()를 통해 구해옴
     * @param top
     * @param right
     * @param bottom
     */
    fun blurBitmap(partOvalElements: ArrayList<Double>) {
        val left: Int
        val top: Int
        val right: Int
        val bottom: Int
        val ovalLeft: Int
        val ovalTop: Int
        val ovalRight: Int
        val ovalBottom: Int
        try {
            left = partOvalElements[0].roundToInt()
            top = partOvalElements[1].roundToInt()
            right = partOvalElements[2].roundToInt()
            bottom = partOvalElements[3].roundToInt()
            ovalLeft = partOvalElements[4].roundToInt()
            ovalTop = partOvalElements[5].roundToInt()
            ovalRight = partOvalElements[6].roundToInt()
            ovalBottom = partOvalElements[7].roundToInt()
            BlurManager.setBlurOrigRect(partOvalElements[0], partOvalElements[1], partOvalElements[2], partOvalElements[3])
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            return
        }

        BlurManager.doFastBlur(
                Bitmap.createBitmap(selectedPreviewBitmap
                        ?: return, left, top, (right - left), (bottom - top)),
                1f,
                50
        )?.let {
            blurredPreviewBitmap = roundedBitmapOval(it, left, top, right, bottom, ovalLeft, ovalTop, ovalRight, ovalBottom)
        }
    }

    private fun roundedBitmapOval(sourceBitmap: Bitmap, left: Int, top: Int, right: Int, bottom: Int, ovalLeft: Int, ovalTop: Int, ovalRight: Int, ovalBottom: Int): Bitmap {
        val width = sourceBitmap.width
        val height = sourceBitmap.height
        val outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val ovalRect = RectF((ovalLeft - left).toFloat(), (ovalTop - top).toFloat(), (ovalRight - left).toFloat(), (ovalBottom - top).toFloat())
        EzLogger.d("rounded param : $left , $top , $right , $bottom , $ovalLeft , $ovalTop , $ovalRight , $ovalBottom")

        val path = Path()
        path.addOval(ovalRect, Path.Direction.CCW)

        val canvas = Canvas(outputBitmap)
        canvas.clipPath(path)
        canvas.drawBitmap(sourceBitmap, 0f, 0f, null)

        return outputBitmap
    }


}