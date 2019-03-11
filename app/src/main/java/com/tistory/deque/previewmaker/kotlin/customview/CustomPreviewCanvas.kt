package com.tistory.deque.previewmaker.kotlin.customview

import android.content.Context
import android.graphics.*
import android.os.AsyncTask
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.tistory.deque.previewmaker.Controler.RetouchingPaintController
import com.tistory.deque.previewmaker.kotlin.manager.*
import com.tistory.deque.previewmaker.kotlin.model.Preview
import com.tistory.deque.previewmaker.kotlin.model.Stamp
import com.tistory.deque.previewmaker.kotlin.model.enums.PreviewEditClickStateEnum
import com.tistory.deque.previewmaker.kotlin.model.enums.StampAnchorEnum
import com.tistory.deque.previewmaker.kotlin.previewedit.KtPreviewEditActivity
import com.tistory.deque.previewmaker.kotlin.util.EzLogger
import java.lang.IllegalArgumentException
import kotlin.math.roundToInt
import android.graphics.Bitmap
import android.content.Intent
import com.tistory.deque.previewmaker.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


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
    var isBlurRoutine: Boolean = false
    var isSaveWithBlur: Boolean = false // 블러 상태에서 확인을 눌렸을 때 저장이 되는데, 그때만 true
    var isSaveRoutine: Boolean = false
    var isSaveReady: Boolean = false

    var saveStartTime: Long = 0

    constructor(context: Context) : super(context)
    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)
    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int) : super(ctx, attrs, defStyleAttr)

    private fun initView() {}

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        this.canvas = canvas

        setBackgroundColor(ContextCompat.getColor(context, R.color.backgroundGray))
        preview?.let {
            if (isSaveRoutine) {
                drawSaveBitmap(it)
                isSaveReady = true
            } else {
                drawBaseBitmap(it)

                if (PreviewEditClickStateManager.isBlurGuide()) {
                    drawBlurGuide()
                }
                if (PreviewEditClickStateManager.isBlur()) {
                    if (isBlurRoutine) drawBlurGuide()
                    else drawBlur()
                }
                if (isStampShown) {
                    drawStamp()
                    if (PreviewEditClickStateManager.isShowGuildLine()) {
                        drawStampGuideLine()
                    }
                }
            }
        } ?: setBackgroundColor(Color.WHITE)
    }

    private fun drawSaveBitmap(preview: Preview) {
        PreviewBitmapManager.selectedPreviewBitmap?.let { previewBitmap ->
            val previewRect = Rect(0, 0, previewBitmap.width, previewBitmap.height)
            EzLogger.d("previewBitmap.width : ${previewBitmap.width}, previewBitmap.height : ${previewBitmap.height}")
            val filterPaint: Paint = RetouachingPaintManager.getPaint(
                    preview.getContrastForFilter(),
                    preview.getBrightnessForFilter(),
                    preview.getSaturationForFilter(),
                    preview.getKelvinForFilter())

            canvas?.drawBitmap(previewBitmap, null, previewRect, filterPaint) ?: run {
                EzLogger.d("canvas null, draw preview bitmap fail")
                return
            }

            if (isSaveWithBlur) {
                val blurRect = Rect(BlurManager.ovalRectFLeftForSave.roundToInt(), BlurManager.ovalRectFTopForSave.roundToInt(),
                        BlurManager.ovalRectFRightForSave.roundToInt(), BlurManager.ovalRectFBottomForSave.roundToInt())

                PreviewBitmapManager.blurredPreviewBitmap?.let { blurBitmap ->
                    canvas?.drawBitmap(blurBitmap, null, blurRect, filterPaint)
                }
            }

            if (isStampShown && !isSaveWithBlur) { //블러를 저장하는 단계일때는 스탬프를 같이 저장하면 안된다.
                stamp?.let { stamp ->
                    val rate = 1.0 / PreviewBitmapManager.smallRatePreviewWithCanvas
                    EzLogger.d("rate : $rate, smallRatePreviewWithCanvas : ${PreviewBitmapManager.smallRatePreviewWithCanvas}")

                    val stampWidth = stamp.width * rate
                    val stampHeight = stamp.height * rate

                    val anchorEnum = stamp.positionAnchorEnum
                    val widthAnchorNum = anchorEnum.value % 3 // 왼쪽이면 0, 중간이면 1, 오른쪽이면 2
                    val heightAnchorNum = anchorEnum.value / 3 // 상단이면 0, 중간이면 1, 하단이면 2

                    val stampPaint = RetouchingPaintController.getPaint(
                            1f,
                            stamp.getAbsoluteBrightness().toFloat(),
                            1f,
                            1f)

                    val stampWidthPos = previewBitmap.width * (stamp.positionWidthPer.toDouble() / 100000.0) - (stampWidth / 2.0) * widthAnchorNum
                    val stampHeightPos = previewBitmap.height * (stamp.positionHeightPer.toDouble() / 100000.0) - (stampHeight / 2.0) * heightAnchorNum
                    EzLogger.d("stamp.positionWidthPer : ${stamp.positionWidthPer}, stamp.positionHeightPer : ${stamp.positionHeightPer}")
                    EzLogger.d("stampWidthPos : $stampWidthPos, stampHeightPos : $stampHeightPos")

                    val stampRect = Rect(stampWidthPos.roundToInt(),
                            stampHeightPos.roundToInt(),
                            (stampWidthPos + stampWidth).roundToInt(),
                            (stampHeightPos + stampHeight).roundToInt())
                    PreviewBitmapManager.selectedStampBitmap?.let { stampBitmap ->
                        canvas?.drawBitmap(stampBitmap, null, stampRect, stampPaint)
                    }
                }
            }
        }
    }

    private fun saveCanvas() {
        saveStartTime = System.currentTimeMillis()
        SaveCanvasAsyncTask().execute()
    }

    private fun drawBlur() {
        PreviewBitmapManager.blurredPreviewBitmap?.let {
            val left: Int
            val top: Int
            val right: Int
            val bottom: Int
            try {
                left = Math.min(BlurManager.guideOvalRectFLeft, BlurManager.guideOvalRectFRight).roundToInt()
                top = Math.min(BlurManager.guideOvalRectFTop, BlurManager.guideOvalRectFBottom).roundToInt()
                right = Math.max(BlurManager.guideOvalRectFLeft, BlurManager.guideOvalRectFRight).roundToInt()
                bottom = Math.max(BlurManager.guideOvalRectFTop, BlurManager.guideOvalRectFBottom).roundToInt()
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                return
            }

            preview?.let { preview ->
                val paint = RetouchingPaintController.getPaint(
                        preview.getContrastForFilter(), preview.getBrightnessForFilter(),
                        preview.getSaturationForFilter(), preview.getKelvinForFilter()
                )
                val blurredBitmapRect = Rect(left, top, right, bottom)
                canvas?.drawBitmap(PreviewBitmapManager.blurredPreviewBitmap
                        ?: return, null, blurredBitmapRect, paint)
                EzLogger.d("drawBlur --------------------------------------------------------------")
                EzLogger.d("${PreviewBitmapManager.blurredPreviewBitmap?.width}, ${PreviewBitmapManager.blurredPreviewBitmap?.height}")
                EzLogger.d("$left, $top, $right, $bottom")
            }
        }
    }

    fun saveStart() {
        if (isBlurRoutine) return
        isSaveRoutine = true
        isSaveReady = false
        activity?.savePreviewStart()
        invalidate()
        saveCanvas()
    }

    private fun saveEnd(fileName: String) {
        isSaveRoutine = false
        if (!isSaveWithBlur) isStampShown = false // 블러를 저장하는 경우가 아닐 때만 스탬프를 안보이게 함
        isSaveWithBlur = false
        preview?.resetFilterValue()
        invalidate()
        activity?.savePreviewEnd(fileName)
    }

    /**
     * 블러의 가이드를 그림
     *
     * 1. 타원을 사용자가 설정하면
     * 5. OK를 눌리면 바로 블러링 연산
     * 2. 그 타원을 포함하는 사각형을 구하고
     * 3. 그 사각형만큼 비트맵을 블러 적용
     * 4. 그 후 그 블러된 비트맵을 pbc.blurBitmap에 저장
     */
    private fun drawBlurGuide() {
        canvas?.drawOval(BlurManager.blurGuideOvalRectF, BlurManager.blurGuidePaint)
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

    fun homeCropListener() {
        activity?.cropSelectedPreview()
    }

    fun homeDeleteListener() {
        activity?.deleteSelectedPreview()
    }

    fun homeStampListener() {
        preview?.editted()
        showStamp()
        PreviewEditClickStateManager.setStampEditState()
        setStampComponent()
        invalidate()
    }

    fun homeSaveListener() {
        saveStart()
    }

    fun homeFilterListener() {
        preview?.editted()
        PreviewEditClickStateManager.setBitmapFilterState()
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

    fun filterFinishListener() {
        PreviewEditClickStateManager.setNoneClickState()
        invalidate()
    }

    fun filterResetListener() {
        previewFilterReset()
        invalidate()
    }

    fun filterBlurListener() {
        PreviewEditClickStateManager.clickBlur()
    }

    fun filterBlurCancelListener() {
        PreviewEditClickStateManager.blurEnd()
        invalidate()
    }

    fun filterBlurOkListener() {
        PreviewEditClickStateManager.blurEnd()
        isSaveWithBlur = true
        saveStart()
        invalidate()
    }

    fun initCanvasAndPreview() {
        isStampShown = false
        preview?.resetFilterValue()
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

            PreviewBitmapManager.getResizedBitmapElements(bitmapWidth, bitmapHeight, canvasWidth, canvasHeight).let { changed ->
                changedPreviewPosWidth = changed[0]
                changedPreviewPosHeight = changed[1]
                changedPreviewWidth = changed[2]
                changedPreviewHeight = changed[3]
            }
            PreviewBitmapManager.smallRatePreviewWithCanvas = changedPreviewWidth.toDouble() / it.width.toDouble()
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

    private fun touchDown(event: MotionEvent) {
        if (PreviewEditClickStateManager.isBlur()) {
            touchDownForBlur(event)
        } // 이하를 if else로 고치지 말것
        if (PreviewEditClickStateManager.isBlurGuide()) {
            touchDownForBlurGuide(event)
        } else {
            touchDownForStamp(event)
        }
    }

    private fun touchDownForStamp(event: MotionEvent) {
        val x: Int
        val y: Int
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
        PreviewEditClickStateManager.restartBlur()
    }

    private fun touchDownForBlurGuide(event: MotionEvent) {
        BlurManager.resetGuideOvalRectF(event.x, event.y)
        invalidate()
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
        PreviewEditClickStateManager.endBlurGuild()
        if (BlurManager.cutOval(changedPreviewWidth, changedPreviewHeight,
                        changedPreviewPosWidth, changedPreviewPosHeight)) {
            makeOvalBlur()
        }
        invalidate()
    }

    private fun makeOvalBlur() {
        activity?.makeOvalBlur(this.width, this.height)
    }

    private fun touchUpForStamp() {
        PreviewEditClickStateManager.clickStampZoomEnd()
    }

    private fun touchMoveForBlurGuide(event: MotionEvent) {
        BlurManager.setGuideOvalRectFRightBottom(event.x, event.y)
        invalidate()
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

    private fun previewFilterReset() {
        preview?.resetFilterValue()
    }

    fun isPreviewNotSelected() = (preview == null)

    inner class SaveCanvasAsyncTask : AsyncTask<Void, Void, String>() {
        val ERROR_IO_EXCEPTION = "ERROR_IO_EXCEPTION"
        val ERROR_TIME_OUT = "ERROR_TIME_OUT"
        val ERROR_PREVIEW_NULL = "ERROR_PREVIEW_NULL"

        override fun doInBackground(vararg params: Void?): String? {
            while (!isSaveReady) {
                //spin ready to save
                //캔버스에 오리지널 크기로 비트맵을 다 그리고 나면 save ready가 true가 됨
                //10초 동안 돌면 그냥 break
                if (saveStartTime + 10000 < System.currentTimeMillis()) {
                    return ERROR_TIME_OUT
                }
            }
            PreviewBitmapManager.selectedPreviewBitmap?.let { previewBitmap ->

                val screenshot = Bitmap.createBitmap(
                        previewBitmap.width,
                        previewBitmap.height,
                        Bitmap.Config.ARGB_8888)

                val canvas = Canvas(screenshot)
                this@CustomPreviewCanvas.draw(canvas)
                val resultUri = preview?.resultImageUri ?: return ERROR_PREVIEW_NULL
                EzLogger.d("save routine async task, preivew : $preview")
                EzLogger.d("save routine async task, resultUri : $resultUri")
                val resultFilePath = resultUri.path
                EzLogger.d("save routine async task, resultFilePath : $resultFilePath")

                val resultFile = File(resultFilePath)
                val fos: FileOutputStream
                try {
                    fos = FileOutputStream(resultFile)
                    screenshot.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                    fos.close()
                    EzLogger.d("Media scan uri : $resultUri")
                    EzLogger.d("Media scan path : $resultFilePath")
                    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                    mediaScanIntent.data = resultUri
                    activity?.sendBroadcast(mediaScanIntent)
                } catch (e: IOException) {
                    e.printStackTrace()
                    return ERROR_IO_EXCEPTION
                }
                preview?.originalImageUri = resultUri
                preview?.saved()

                return resultFile.name
            }

            return null
        }

        override fun onPostExecute(result: String?) {
            saveEnd(result ?: "")
            super.onPostExecute(result)
        }
    }

}