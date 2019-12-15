package com.tistory.deque.previewmaker.kotlin.manager

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.Pair
import com.tistory.deque.previewmaker.kotlin.util.EzLogger
import io.reactivex.Single
import java.util.ArrayList

object BlurManager {
    val blurGuidePaint = Paint().apply {
        color = Color.MAGENTA
        alpha = 80
        isDither = true
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.FILL
    }
    val blurGuideOvalRectF = RectF()

    var guideOvalRectFLeft: Float = 0.0f
    var guideOvalRectFTop: Float = 0.0f
    var guideOvalRectFRight: Float = 0.0f
    var guideOvalRectFBottom: Float = 0.0f
    var guideOvalRectFLeftOrig: Float = 0.0f
    var guideOvalRectFTopOrig: Float = 0.0f
    var guideOvalRectFRightOrig: Float = 0.0f
    var guideOvalRectFBottomOrig: Float = 0.0f

    var ovalRectFLeftForSave:Double = 0.0
    var ovalRectFTopForSave:Double = 0.0
    var ovalRectFRightForSave:Double = 0.0
    var ovalRectFBottomForSave:Double = 0.0

    init {
        resetManager()
    }

    fun resetManager() {
        guideOvalRectFLeft = 0.0f
        guideOvalRectFTop = 0.0f
        guideOvalRectFRight = 0.0f
        guideOvalRectFBottom = 0.0f
        guideOvalRectFLeftOrig = 0.0f
        guideOvalRectFTopOrig = 0.0f
        guideOvalRectFRightOrig = 0.0f
        guideOvalRectFBottomOrig = 0.0f
        ovalRectFLeftForSave = 0.0
        ovalRectFTopForSave = 0.0
        ovalRectFRightForSave = 0.0
        ovalRectFBottomForSave = 0.0
    }

    fun resetGuideOvalRectF(left: Float, top: Float) {
        setGuideOvalRectFLeftTop(left, top)
        setGuideOvalRectFRightBottom(left, top)
    }

    private fun setGuideOvalRectFLeftTop(left: Float, top: Float) {
        guideOvalRectFLeft = left
        guideOvalRectFTop = top
        guideOvalRectFLeftOrig = left
        guideOvalRectFTopOrig = top
    }

    fun setGuideOvalRectFRightBottom(right: Float, bottom: Float) {
        blurGuideOvalRectF.set(guideOvalRectFLeft, guideOvalRectFTop, right, bottom)
        guideOvalRectFRight = right
        guideOvalRectFBottom = bottom
        guideOvalRectFRightOrig = right
        guideOvalRectFBottomOrig = bottom
    }

    /**
     *  @return 불가능한 타원(좌표가 하나라도 음수)일때 false반환
     */
    fun cutOval(changedPreviewWidth: Int, changedPreviewHeight: Int, changedPreviewPosWidth: Int, changedPreviewPosHeight: Int): Boolean {
        //불가능한 타원을 만들면 앱이 터짐
        val left = Math.min(guideOvalRectFLeft.toInt(), guideOvalRectFRight.toInt())
        val top = Math.min(guideOvalRectFTop.toInt(), guideOvalRectFBottom.toInt())
        val right = Math.max(guideOvalRectFLeft.toInt(), guideOvalRectFRight.toInt())
        val bottom = Math.max(guideOvalRectFTop.toInt(), guideOvalRectFBottom.toInt())
        val b_right = changedPreviewPosWidth + changedPreviewWidth
        val b_bottom = changedPreviewPosHeight + changedPreviewHeight //비트맵의 좌표

        EzLogger.d("CUT OVAL, BITMAP : $changedPreviewPosWidth, $changedPreviewPosHeight, $b_right, $b_bottom")
        EzLogger.d("OVAL : $left, $top, $right, $bottom")

        if (left == right || top == bottom) { // 4. 타원이 넓이가 없을때
            guideOvalRectFLeft = 0f
            guideOvalRectFTop = 0f
            guideOvalRectFRight = 1f
            guideOvalRectFBottom = 1f
            EzLogger.d("CASE 4 FALSE : $left, $top, $right, $bottom")
            return false
        }

        if (changedPreviewPosWidth <= left && changedPreviewPosHeight <= top && b_right >= right && b_bottom >= bottom) { // 1. 타원이 비트맵 내부
            guideOvalRectFLeft = left.toFloat()
            guideOvalRectFTop = top.toFloat()
            guideOvalRectFRight = right.toFloat()
            guideOvalRectFBottom = bottom.toFloat()
            EzLogger.d("CASE 1 TRUE : $left, $top, $right, $bottom")
            return true
        } else if (b_right < left || changedPreviewPosWidth > right || b_bottom < top || changedPreviewPosHeight > bottom) { // 2. 타원이 비트맵 바깥
            guideOvalRectFLeft = 0f
            guideOvalRectFTop = 0f
            guideOvalRectFRight = 1f
            guideOvalRectFBottom = 1f
            EzLogger.d("CASE 2 FALSE : $left, $top, $right, $bottom")
            return false
        } else { // 3. 그 외 겹칠 경우
            guideOvalRectFLeft = Math.max(left, changedPreviewPosWidth).toFloat()
            guideOvalRectFTop = Math.max(top, changedPreviewPosHeight).toFloat()
            guideOvalRectFRight = Math.min(right, b_right).toFloat()
            guideOvalRectFBottom = Math.min(bottom, b_bottom).toFloat()
            EzLogger.d("CASE 3 TRUE : $guideOvalRectFLeft, $guideOvalRectFTop, $guideOvalRectFRight, $guideOvalRectFBottom")
            return true
        }
    }

    /**
     * 자르기 전 타원의 좌표
     */
    private fun getGuideOvalRectFOrig(): ArrayList<Float> {
        val result = ArrayList<Float>()
        val leftOrig = Math.min(guideOvalRectFLeftOrig, guideOvalRectFRightOrig)
        val topOrig = Math.min(guideOvalRectFTopOrig, guideOvalRectFBottomOrig)
        val rightOrig = Math.max(guideOvalRectFLeftOrig, guideOvalRectFRightOrig)
        val bottomOrig = Math.max(guideOvalRectFTopOrig, guideOvalRectFBottomOrig)

        guideOvalRectFLeftOrig = leftOrig
        guideOvalRectFTopOrig = topOrig
        guideOvalRectFRightOrig = rightOrig
        guideOvalRectFBottomOrig = bottomOrig

        result.add(guideOvalRectFLeftOrig)
        result.add(guideOvalRectFTopOrig)
        result.add(guideOvalRectFRightOrig)
        result.add(guideOvalRectFBottomOrig)

        return result
    }

    /**
     * 리사이징된 비트맵의 타원에서 원래 비트맵의 타원을 계산해냄
     * @return partLeft, partTop, partRight, partBottom, ovalOrigLeft, ovalOrigTop, ovalOrigRight, ovalOrigBottom
     * 잘린 사각형의 좌상 좌표, 우하 좌표, 잘리기 전 타원의 원본 사이즈의 좌상 좌표, 우하 좌표
     */
    fun resizedBlurOvalToOriginalBlurOval(canvasWidth: Int, canvasHeight: Int): java.util.ArrayList<Double> {
        val left = Math.min(guideOvalRectFLeft, guideOvalRectFRight)
        val top = Math.min(guideOvalRectFTop, guideOvalRectFBottom)
        val right = Math.max(guideOvalRectFLeft, guideOvalRectFRight)
        val bottom = Math.max(guideOvalRectFTop, guideOvalRectFBottom)

        val ovalLeft = Math.min(getGuideOvalRectFOrig()[0], getGuideOvalRectFOrig()[2])
        val ovalTop = Math.min(getGuideOvalRectFOrig()[1], getGuideOvalRectFOrig()[3])
        val ovalRight = Math.max(getGuideOvalRectFOrig()[0], getGuideOvalRectFOrig()[2])
        val ovalBottom = Math.max(getGuideOvalRectFOrig()[1], getGuideOvalRectFOrig()[3])

        EzLogger.d("""
            left = $left
            top = $top
            right = $right
            bottom = $bottom
            ovalLeft = $ovalLeft
            ovalTop = $ovalTop
            ovalRight = $ovalRight
            ovalBottom = $ovalBottom
        """.trimIndent())

        val pbw = PreviewBitmapManager.selectedPreviewBitmap?.width  ?: 0
        val pbh =  PreviewBitmapManager.selectedPreviewBitmap?.height ?: 0
        val elements = PreviewBitmapManager.getResizedBitmapElements(pbw, pbh, canvasWidth, canvasHeight)
        val results = java.util.ArrayList<Double>()

        val pwPos = elements[0].toDouble()
        val phPos = elements[1].toDouble()
        val pw = elements[2].toDouble()
        val ph = elements[3].toDouble()

        val widthRate = pbw / pw
        val heightRate = pbh / ph

        val partLeft = widthRate * (left - pwPos)
        val partTop = heightRate * (top - phPos)
        val partRight = widthRate * (right - pwPos)
        val partBottom = heightRate * (bottom - phPos)

        val partOvalLeft = widthRate * (ovalLeft - pwPos)
        val partOvalTop = heightRate * (ovalTop - phPos)
        val partOvalRight = widthRate * (ovalRight - pwPos)
        val partOvalBottom = heightRate * (ovalBottom - phPos)

        results.add(partLeft)
        results.add(partTop)
        results.add(partRight)
        results.add(partBottom)

        results.add(partOvalLeft)
        results.add(partOvalTop)
        results.add(partOvalRight)
        results.add(partOvalBottom)

        EzLogger.d("resizedBlurOvalToOriginalBlurOval results : $results")

        return results
    }

    fun setBlurOrigRect(left: Double, top: Double, right: Double, bottom: Double) {
        ovalRectFLeftForSave = left
        ovalRectFTopForSave = top
        ovalRectFRightForSave = right
        ovalRectFBottomForSave = bottom
    }

    fun doFastBlur(sentBitmap: Bitmap, scale: Float, radius: Int): Bitmap? {
        if (radius < 1) {
            return null
        }

        val width = Math.round(sentBitmap.width * scale)
        val height = Math.round(sentBitmap.height * scale)
        val bitmap = Bitmap.createScaledBitmap(sentBitmap, width, height, false).let {
            if (it.isMutable) it
            else it.copy(it.config, true)
        }

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