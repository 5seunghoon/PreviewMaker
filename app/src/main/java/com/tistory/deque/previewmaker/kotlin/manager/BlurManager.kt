package com.tistory.deque.previewmaker.kotlin.manager

import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.tistory.deque.previewmaker.Util.Logger
import com.tistory.deque.previewmaker.kotlin.util.EzLogger

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

    private var guideOvalRectFLeft: Float = 0.0f
    private var guideOvalRectFTop: Float = 0.0f
    private var guideOvalRectFRight: Float = 0.0f
    private var guideOvalRectFBottom: Float = 0.0f
    private var guideOvalRectFLeftOrig: Float = 0.0f
    private var guideOvalRectFTopOrig: Float = 0.0f
    private var guideOvalRectFRightOrig: Float = 0.0f
    private var guideOvalRectFBottomOrig: Float = 0.0f

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

}