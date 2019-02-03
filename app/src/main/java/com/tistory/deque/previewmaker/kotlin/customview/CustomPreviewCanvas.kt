package com.tistory.deque.previewmaker.kotlin.customview

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.tistory.deque.previewmaker.R
import com.tistory.deque.previewmaker.kotlin.manager.PreviewBitmapManager
import com.tistory.deque.previewmaker.kotlin.manager.PreviewEditButtonViewStateManager
import com.tistory.deque.previewmaker.kotlin.manager.PreviewEditClickStateManager
import com.tistory.deque.previewmaker.kotlin.manager.RetouachingPaintManager
import com.tistory.deque.previewmaker.kotlin.model.Preview
import com.tistory.deque.previewmaker.kotlin.model.Stamp
import com.tistory.deque.previewmaker.kotlin.model.enums.PreviewEditClickStateEnum
import com.tistory.deque.previewmaker.kotlin.model.enums.StampAnchorEnum
import com.tistory.deque.previewmaker.kotlin.previewedit.KtPreviewEditActivity
import com.tistory.deque.previewmaker.kotlin.util.EzLogger
import java.io.IOException
import java.lang.IllegalArgumentException
import java.util.ArrayList
import kotlin.math.roundToInt

class CustomPreviewCanvas : View {
    init {
        initView()
    }

    private val stampGuideRectWidth = 5f
    private val stampGuideLineWidth = 2f
    private val stampGuideCircleRadius = 15f

    private var activity: KtPreviewEditActivity? = null
    private var canvas: Canvas? = null

    val preview: Preview? get() = activity?.viewModel?.selectedPreview
    val stamp: Stamp? get() = activity?.viewModel?.stamp

    private var changedPreviewPosWidth: Int = 0
    private var changedPreviewPosHeight: Int = 0
    private var changedPreviewWidth: Int = 0
    private var changedPreviewHeight: Int = 0

    private var stampWidthPos: Int = 0
    private var stampHeightPos: Int = 0
    private var stampRate: Double = 0.0

    private var movePrevX: Int = 0
    private var movePrevY: Int = 0

    var isStampShown: Boolean = false
        private set

    constructor(context: Context) : super(context)
    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)
    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int) : super(ctx, attrs, defStyleAttr)

    private fun initView() {

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        this.canvas = canvas

        setBackgroundColor(ContextCompat.getColor(context, R.color.backgroundGray))
        preview?.let {

            drawBaseBitmap(it)

            if (isStampShown) {
                drawStamp()
                if (PreviewEditClickStateManager.isShowGuildLine()) {
                    drawStampGuideLine()
                }
            }
        } ?: setBackgroundColor(Color.WHITE)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> touchDown(event)
            MotionEvent.ACTION_MOVE -> touchMove(event)
            MotionEvent.ACTION_UP -> touchUp(event)
        }
        return true
    }

    fun setComponent(activity: KtPreviewEditActivity) {
        EzLogger.d("setComponent viewModel set ")
        this.activity = activity
        PreviewEditButtonViewStateManager.initState()
    }

    fun showStamp() {
        isStampShown = true
    }

    fun hideStamp() {
        isStampShown = false
    }

    fun homeStampListener() {
        showStamp()
        PreviewEditClickStateManager.setStampEditState()
        setStampComponent()
        invalidate()
    }

    fun stampDeleteListener() {
        PreviewEditClickStateManager.setNoneClickState()
        hideStamp()
        invalidate()
    }

    fun stampResetListener() {
        stampReset()
    }

    fun stampFinishListener() {
        PreviewEditClickStateManager.setNoneClickState()
        finishStampEdit()
        invalidate()
    }

    fun homeFilterListener() {
        PreviewEditClickStateManager.setBitmapFilterState()
        invalidate()
    }

    fun filterFinishListener() {
        PreviewEditClickStateManager.setNoneClickState()
        invalidate()
    }

    fun filterResetListener() {
        previewFilterReset()
        invalidate()
    }

    private fun setStampComponent() {
        stamp?.let { stamp ->
            if (stamp.width <= 0) stamp.width = PreviewBitmapManager.selectedStampBitmap?.width
                    ?: -1
            if (stamp.height <= 0) stamp.height = PreviewBitmapManager.selectedStampBitmap?.height
                    ?: -1

            EzLogger.d("setStampComponent, stamp : $stamp")

            stampRate = stamp.width.toDouble() / stamp.height.toDouble()

            //get correct position from anchor
            val heightAnchor: Int = stamp.positionAnchorEnum.value / 3
            val widthAnchor = stamp.positionAnchorEnum.value % 3

            stampWidthPos = (stamp.positionWidthPer * changedPreviewWidth / 100000.0f
                    - stamp.width * widthAnchor / 2.0f + changedPreviewPosWidth).toInt()
            stampHeightPos = (stamp.positionHeightPer * changedPreviewHeight / 100000.0f
                    - stamp.height * heightAnchor / 2.0f + changedPreviewPosHeight).toInt()

            EzLogger.d("""stamp.positionWidthPer : ${stamp.positionWidthPer}
                | changedPreviewWidth : $changedPreviewWidth
                | stamp.width : ${stamp.width}
                | widthAnchor : $widthAnchor
                | changedPreviewPosWidth : $changedPreviewPosWidth
            """.trimMargin())


            EzLogger.d("stampWidth : ${stamp.width}, stampWidthPos : $stampWidthPos, " +
                    "stampHeight : ${stamp.height}, stampHeightPos : $stampHeightPos")
        } ?: EzLogger.d("setStampComponent stamp null")
    }

    private fun drawBaseBitmap(preview: Preview) {
        EzLogger.d("preview : ${preview.originalImageUri}")

        PreviewBitmapManager.selectedPreviewBitmap?.let {
            val bitmapWidth = it.width
            val bitmapHeight = it.height
            val canvasWidth = this.width
            val canvasHeight = this.height

            getResizedBitmapElements(bitmapWidth, bitmapHeight, canvasWidth, canvasHeight).let { changed ->
                changedPreviewPosWidth = changed[0]
                changedPreviewPosHeight = changed[1]
                changedPreviewWidth = changed[2]
                changedPreviewHeight = changed[3]
            }
            val paintPreviewContrastBrightness = RetouachingPaintManager.getPaint(
                    preview.getContrastForFilter(),
                    preview.getBrightnessForFilter(),
                    preview.getSaturationForFilter(),
                    preview.getKelvinForFilter()
            )
            val previewBitmapRect = Rect(
                    changedPreviewPosWidth,
                    changedPreviewPosHeight,
                    changedPreviewPosWidth + changedPreviewWidth,
                    changedPreviewPosHeight + changedPreviewHeight)
            EzLogger.d("previewBitmapRect : $previewBitmapRect")

            canvas?.drawBitmap(it,
                    null,
                    previewBitmapRect,
                    paintPreviewContrastBrightness)
        }
    }

    private fun drawStamp() {
        EzLogger.d("custom preview canvas, draw stamp : $stamp")
        stamp?.let {
            val paintContrastBrightness =
                    RetouachingPaintManager.getPaint(
                            1f,
                            it.getAbsoluteBrightness().toFloat(),
                            1f,
                            1f)
            val rect = Rect(stampWidthPos, stampHeightPos, stampWidthPos + it.width, stampHeightPos + it.height)

            canvas?.drawBitmap(
                    PreviewBitmapManager.selectedStampBitmap ?: return,
                    null,
                    rect,
                    paintContrastBrightness)

            EzLogger.d("drawStamp, rect : $rect")

        }
    }

    fun stampUriToBitmap(imageUri: Uri, context: Context): Bitmap? {
        try {
            return MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
        } catch (e: IOException) {
            EzLogger.d("URI -> Bitmap : IOException$imageUri")
            e.printStackTrace()
            return null
        }

    }

    private fun drawStampGuideLine() {
        stamp?.let { stamp ->
            val xStart = stampWidthPos
            val xEnd = stampWidthPos + stamp.width
            val xLength = stamp.width
            val yStart = stampHeightPos
            val yEnd = stampHeightPos + stamp.height
            val yLength = stamp.height

            val guideRectPaint = Paint().apply {
                strokeWidth = stampGuideRectWidth
                color = Color.WHITE
                style = Paint.Style.STROKE
            }
            canvas?.drawRect(xStart.toFloat(), yStart.toFloat(), xEnd.toFloat(), yEnd.toFloat(), guideRectPaint)

            val guideLine = Paint().apply {
                strokeWidth = stampGuideLineWidth
                color = Color.WHITE
            }
            canvas?.run {
                drawLine(xStart.toFloat(), yLength / 3.0f + yStart, xEnd.toFloat(), yLength / 3.0f + yStart, guideLine)
                drawLine(xStart.toFloat(), yLength * 2 / 3.0f + yStart, xEnd.toFloat(), yLength * 2 / 3.0f + yStart, guideLine)
                drawLine(xLength / 3.0f + xStart, yStart.toFloat(), xLength / 3.0f + xStart, yEnd.toFloat(), guideLine)
                drawLine(xLength * 2 / 3.0f + xStart, yStart.toFloat(), xLength * 2 / 3.0f + xStart, yEnd.toFloat(), guideLine)
            }

            val guideCircle = Paint().apply {
                color = Color.WHITE
            }
            canvas?.run {
                drawCircle(xStart.toFloat(), yStart.toFloat(), stampGuideCircleRadius, guideCircle)
                drawCircle(xStart.toFloat(), yEnd.toFloat(), stampGuideCircleRadius, guideCircle)
                drawCircle(xEnd.toFloat(), yStart.toFloat(), stampGuideCircleRadius, guideCircle)
                drawCircle(xEnd.toFloat(), yEnd.toFloat(), stampGuideCircleRadius, guideCircle)
            }
        }
    }

    /**
     * 비트맵을 어느 좌표에 어떤 크기로 그릴지 결정
     *
     * @return previewPosWidth, previewPosHeight, previewWidth, previewHeight
     */
    private fun getResizedBitmapElements(previewBitmapWidth: Int, previewBitmapHeight: Int, canvasWidth: Int, canvasHeight: Int): ArrayList<Int> {
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

    private fun touchDown(event: MotionEvent) {
        if (PreviewEditClickStateManager.isBlur()) {
            touchDownForBlur(event)
        }
        if (PreviewEditClickStateManager.isBlurGuide()) {
            touchDownForBlurGuide(event)
        } else {
            touchDownForStamp(event)
        }
    }

    private fun touchDownForStamp(event: MotionEvent) {
        var x = 0
        var y = 0
        try {
            x = event.x.roundToInt()
            y = event.y.roundToInt()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            return
        }
        movePrevX = x
        movePrevY = y

        EzLogger.d("""touchDownForStamp,
                | prevX : $movePrevX, prevY : $movePrevY
                | newX : $x, newY : $y
            """.trimMargin())

        if (isTouchInStamp(x, y) && isStampShown) {
            clickStamp()
        } else if (isTouchStampZoom(x, y) && isStampShown) {
            clickStampToZoom()
        }
    }

    private fun isTouchInStamp(x: Int, y: Int): Boolean {
        stamp?.let { stamp ->
            return isInBoxWithWidth(x, y, stampWidthPos, stampHeightPos, stamp.width, stamp.height)
        } ?: return false
    }

    private fun isTouchStampZoom(x: Int, y: Int): Boolean {
        stamp?.let { stamp ->
            val radius = (stampGuideCircleRadius + 25).toInt()
            val xStart = stampWidthPos //x start
            val xEnd = stampWidthPos + stamp.width //x end
            val yStart = stampHeightPos // y start
            val yEnd = stampHeightPos + stamp.height // y end

            return (isInBoxWithRadius(x, y, xStart, yStart, radius)
                    || isInBoxWithRadius(x, y, xStart, yEnd, radius)
                    || isInBoxWithRadius(x, y, xEnd, yStart, radius)
                    || isInBoxWithRadius(x, y, xEnd, yEnd, radius))

        } ?: return false
    }

    private fun isInBoxWithWidth(x: Int, y: Int, x1: Int, y1: Int, xWidth: Int, yWidth: Int): Boolean {
        return (x > x1) && (x < x1 + xWidth) && (y > y1) && (y < y1 + yWidth)
    }

    private fun isInBoxWithRadius(x: Int, y: Int, xCenter: Int, yCenter: Int, radius: Int): Boolean {
        return (x - xCenter) * (x - xCenter) + (y - yCenter) * (y - yCenter) < radius * radius
    }

    private fun clickStamp() {
        EzLogger.d("clickStamp")
        if (PreviewEditClickStateManager.clickStamp()) {
            activity?.setSyncClickState()
        }
        invalidate()
    }

    private fun clickStampToZoom() {
        EzLogger.d("clickStampToZoom")
        if (PreviewEditClickStateManager.clickStampZoomStart()) {
            activity?.setSyncClickState()
        }
        invalidate()
    }

    private fun touchDownForBlur(event: MotionEvent) {

    }

    private fun touchDownForBlurGuide(event: MotionEvent) {

    }

    private fun touchMove(event: MotionEvent) {
        if (PreviewEditClickStateManager.isBlurGuide()) {
            touchMoveForBlurGuide(event)
        } else {
            touchMoveForStamp(event)
        }
    }

    private fun touchUp(event: MotionEvent) {
        if (PreviewEditClickStateManager.isBlurGuide()) {
            touchUpForBlurGuide(event)
        } else {
            touchUpForStamp()
        }
    }

    private fun touchUpForBlurGuide(event: MotionEvent) {

    }

    private fun touchUpForStamp() {
        PreviewEditClickStateManager.clickStampZoomEnd()
    }

    private fun touchMoveForBlurGuide(event: MotionEvent) {

    }

    private fun touchMoveForStamp(event: MotionEvent) {
        var x: Int = 0
        var y: Int = 0
        try {
            x = event.x.roundToInt()
            y = event.y.roundToInt()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            return
        }

        when (PreviewEditClickStateManager.nowState) {
            PreviewEditClickStateEnum.STATE_STAMP_EDIT -> stampMove(x, y)
            PreviewEditClickStateEnum.STATE_STAMP_ZOOM -> stampZoom(x, y)
        }

        movePrevX = x
        movePrevY = y
    }

    private fun stampMove(x: Int, y: Int) {
        val deltaX = x - movePrevX
        val deltaY = y - movePrevY

        if ((stampWidthPos + (stamp?.width ?: 0) + deltaX >= 0)
                && (stampWidthPos + deltaX <= this.width)) stampWidthPos += deltaX
        if ((stampHeightPos + (stamp?.height ?: 0) + deltaY >= 0)
                && (stampHeightPos + deltaY <= this.height)) stampHeightPos += deltaY

        invalidate()
    }

    private fun stampZoom(x: Int, y: Int) {
        stamp?.let { stamp ->
            val stampCenterX = (stampWidthPos + stamp.width / 2.0f).toDouble()
            val stampCenterY = (stampHeightPos + stamp.height / 2.0f).toDouble()

            val nowDist = Math.sqrt(
                    Math.pow(stampCenterX - x, 2.0) + Math.pow(stampCenterY - y, 2.0))

            if (stampRate == 0.0) stamp.width.toDouble() / stamp.height.toDouble()
            val newHeight = (2.0f * nowDist) / Math.sqrt((Math.pow(stampRate, 2.0) + 1))
            val newWidth = newHeight * stampRate

            try {
                stampWidthPos = (stampCenterX - newWidth / 2.0f).roundToInt()
                stampHeightPos = (stampCenterY - newHeight / 2.0f).roundToInt()
                stamp.width = (stampCenterX + newWidth / 2.0f).roundToInt() - stampWidthPos
                stamp.height = (stampCenterY + newHeight / 2.0f).roundToInt() - stampHeightPos
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
            invalidate()
        }
    }

    private fun finishStampEdit() {
        stamp?.let { stamp ->
            val id = stamp.id
            stamp.positionWidthPer =
                    (((stamp.width / 2.0f) + stampWidthPos - changedPreviewPosWidth)
                            * 100000.0f / changedPreviewWidth).roundToInt()
            stamp.positionHeightPer =
                    (((stamp.height / 2.0f) + stampHeightPos - changedPreviewPosHeight)
                            * 100000.0f / changedPreviewHeight).roundToInt()

            val widthAnchor = when (stamp.positionWidthPer) {
                in Int.MIN_VALUE..33333 -> 0
                in 33334..66666 -> 1
                else -> 2
            }
            val heightAnchor = when (stamp.positionHeightPer) {
                in Int.MIN_VALUE..33333 -> 0
                in 33334..66666 -> 1
                else -> 2
            }
            stamp.positionAnchorEnum = StampAnchorEnum.valueToEnum(widthAnchor + heightAnchor * 3)

            // 앵커를 고려해서 위치를 다시 계산
            stamp.positionWidthPer =
                    (((stamp.width / 2.0f) * widthAnchor + stampWidthPos - changedPreviewPosWidth)
                            * 100000.0f / changedPreviewWidth).roundToInt()
            stamp.positionHeightPer =
                    (((stamp.height / 2.0f) * heightAnchor + stampHeightPos - changedPreviewPosHeight)
                            * 100000.0f / changedPreviewHeight).roundToInt()

            EzLogger.d("""
                finishStampEdit()
                stamp : $stamp
                widthAnchor : $widthAnchor, heightAnchor : $heightAnchor, anchor : ${stamp.positionAnchorEnum}
            """.trimIndent())

            activity?.stampUpdate(id, stamp)
        }
    }

    private fun stampReset() {
        stamp?.let { stamp ->
            stamp.width = PreviewBitmapManager.selectedStampBitmap?.width ?: stamp.width
            stamp.height = PreviewBitmapManager.selectedStampBitmap?.height ?: stamp.height
            stamp.positionWidthPer = 50000
            stamp.positionHeightPer = 50000
            stampWidthPos = (changedPreviewWidth / 2.0f - stamp.width / 2.0f).roundToInt() + changedPreviewPosWidth
            stampHeightPos = (changedPreviewHeight / 2.0f - stamp.height / 2.0f).roundToInt() + changedPreviewPosHeight
            stamp.resetBrightness()
            invalidate()
        }
    }

    fun stampBrightnessSeekBarListener(progress: Int) {
        stamp?.let { stamp ->
            stamp.brightness = progress
            invalidate()
        }
    }

    fun previewBrightnessSeekBarListener(progress: Int) {
        preview?.let {
            it.brightness = progress
            invalidate()
        }
    }

    fun previewContrastSeekBarListener(progress: Int) {
        preview?.let {
            it.contrast = progress
            invalidate()
        }
    }

    fun previewSaturationSeekBarListener(progress: Int) {
        preview?.let {
            it.saturation = progress
            invalidate()
        }
    }

    fun previewKelvinSeekBarListener(progress: Int) {
        preview?.let {
            it.kelvin = progress
            invalidate()
        }
    }

    private fun previewFilterReset(){
        preview?.resetFilterValue()
    }


}