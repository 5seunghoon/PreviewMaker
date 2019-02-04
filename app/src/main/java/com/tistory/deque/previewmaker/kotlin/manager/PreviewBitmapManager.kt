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
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            return
        }

        doFastBlur(
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

    private fun doFastBlur(sentBitmap: Bitmap, scale: Float, radius: Int): Bitmap? {
        if (radius < 1) {
            return null
        }

        val width = Math.round(sentBitmap.width * scale)
        val height = Math.round(sentBitmap.height * scale)
        val bitmap = Bitmap.createScaledBitmap(sentBitmap, width, height, false)

        val w = bitmap.width
        val h = bitmap.height

        val pix = IntArray(w * h)
        EzLogger.d("$w $h ${pix.size}")
        bitmap.getPixels(pix, 0, w, 0, 0, w, h)

        val wm = w - 1
        val hm = h - 1
        val wh = w * h
        val div = radius + radius + 1

        val r = IntArray(wh)
        val g = IntArray(wh)
        val b = IntArray(wh)
        var rsum: Int
        var gsum: Int
        var bsum: Int
        var x: Int
        var y: Int
        var i: Int
        var p: Int
        var yp: Int
        var yi: Int
        var yw: Int
        val vmin = IntArray(Math.max(w, h))

        var divsum = div + 1 shr 1
        divsum *= divsum
        val dv = IntArray(256 * divsum)
        i = 0
        while (i < 256 * divsum) {
            dv[i] = i / divsum
            i++
        }

        yi = 0
        yw = yi

        val stack = Array(div) { IntArray(3) }
        var stackpointer: Int
        var stackstart: Int
        var sir: IntArray
        var rbs: Int
        val r1 = radius + 1
        var routsum: Int
        var goutsum: Int
        var boutsum: Int
        var rinsum: Int
        var ginsum: Int
        var binsum: Int

        y = 0
        while (y < h) {
            bsum = 0
            gsum = bsum
            rsum = gsum
            boutsum = rsum
            goutsum = boutsum
            routsum = goutsum
            binsum = routsum
            ginsum = binsum
            rinsum = ginsum
            i = -radius
            while (i <= radius) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))]
                sir = stack[i + radius]
                sir[0] = p and 0xff0000 shr 16
                sir[1] = p and 0x00ff00 shr 8
                sir[2] = p and 0x0000ff
                rbs = r1 - Math.abs(i)
                rsum += sir[0] * rbs
                gsum += sir[1] * rbs
                bsum += sir[2] * rbs
                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
                i++
            }
            stackpointer = radius

            x = 0
            while (x < w) {

                r[yi] = dv[rsum]
                g[yi] = dv[gsum]
                b[yi] = dv[bsum]

                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum

                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]

                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm)
                }
                p = pix[yw + vmin[x]]

                sir[0] = p and 0xff0000 shr 16
                sir[1] = p and 0x00ff00 shr 8
                sir[2] = p and 0x0000ff

                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]

                rsum += rinsum
                gsum += ginsum
                bsum += binsum

                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer % div]

                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]

                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]

                yi++
                x++
            }
            yw += w
            y++
        }
        x = 0
        while (x < w) {
            bsum = 0
            gsum = bsum
            rsum = gsum
            boutsum = rsum
            goutsum = boutsum
            routsum = goutsum
            binsum = routsum
            ginsum = binsum
            rinsum = ginsum
            yp = -radius * w
            i = -radius
            while (i <= radius) {
                yi = Math.max(0, yp) + x

                sir = stack[i + radius]

                sir[0] = r[yi]
                sir[1] = g[yi]
                sir[2] = b[yi]

                rbs = r1 - Math.abs(i)

                rsum += r[yi] * rbs
                gsum += g[yi] * rbs
                bsum += b[yi] * rbs

                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }

                if (i < hm) {
                    yp += w
                }
                i++
            }
            yi = x
            stackpointer = radius
            y = 0
            while (y < h) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = -0x1000000 and pix[yi] or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]

                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum

                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]

                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w
                }
                p = x + vmin[y]

                sir[0] = r[p]
                sir[1] = g[p]
                sir[2] = b[p]

                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]

                rsum += rinsum
                gsum += ginsum
                bsum += binsum

                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer]

                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]

                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]

                yi += w
                y++
            }
            x++
        }

        EzLogger.d(w.toString() + " " + h + " " + pix.size)
        bitmap.setPixels(pix, 0, w, 0, 0, w, h)

        return bitmap
    }

}