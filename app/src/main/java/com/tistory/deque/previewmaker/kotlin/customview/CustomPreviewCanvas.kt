package com.tistory.deque.previewmaker.kotlin.customview

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import com.tistory.deque.previewmaker.R
import com.tistory.deque.previewmaker.Util.Logger
import com.tistory.deque.previewmaker.kotlin.manager.PreviewBitmapManager
import com.tistory.deque.previewmaker.kotlin.manager.PreviewEditButtonViewStateManager
import com.tistory.deque.previewmaker.kotlin.manager.PreviewEditClickStateManager
import com.tistory.deque.previewmaker.kotlin.manager.RetouachingPaintManager
import com.tistory.deque.previewmaker.kotlin.model.Preview
import com.tistory.deque.previewmaker.kotlin.model.Stamp
import com.tistory.deque.previewmaker.kotlin.model.enums.StampAnchorEnum
import com.tistory.deque.previewmaker.kotlin.previewedit.KtPreviewEditActivity
import com.tistory.deque.previewmaker.kotlin.util.EzLogger
import java.io.IOException
import java.util.ArrayList

class CustomPreviewCanvas : View {
    init {
        initView()
    }

    private var activity: KtPreviewEditActivity? = null
    private var canvas: Canvas? = null

    val preview: Preview? get() = activity?.viewModel?.selectedPreview
    val stamp: Stamp? get() = activity?.viewModel?.stamp

    private var changedPreviewPosWidth: Int = 0
    private var changedPreviewPosHeight: Int = 0
    private var changedPreviewBitmapWidth: Int = 0
    private var changedPreviewBitmapHeight: Int = 0

    private var stampWidthPos: Int = 0
    private var stampHeightPos: Int = 0
    private var stampRate: Double = 0.0


    var isStampShow: Boolean = false
        private set

    constructor(context: Context) : super(context)
    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)
    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int) : super(ctx, attrs, defStyleAttr)

    private fun initView() {

    }

    fun setComponent(activity: KtPreviewEditActivity) {
        EzLogger.d("setComponent viewModel set ")
        this.activity = activity
        PreviewEditButtonViewStateManager.initState()
    }

    fun showStamp() {
        isStampShow = true
    }

    fun hideStamp() {
        isStampShow = false
    }

    fun homeStampListener() {
        showStamp()
        setStampComponent()
        invalidate()
    }

    private fun setStampComponent(){
        stamp?.let { stamp ->
            if(stamp.width <= 0 ) stamp.width = PreviewBitmapManager.selectedStampBitmap?.width ?: -1
            if(stamp.height <= 0) stamp.height = PreviewBitmapManager.selectedStampBitmap?.height ?: -1

            EzLogger.d("setStampComponent, stamp : $stamp")

            stampRate = stamp.width.toDouble() / stamp.height.toDouble()

            //get correct position from anchor
            val heightAnchor: Int = stamp.positionAnchorEnum.value / 3
            val widthAnchor = stamp.positionAnchorEnum.value % 3

            stampWidthPos = (stamp.positionWidthPer * changedPreviewBitmapWidth / 100000.0f
                    - stamp.width * widthAnchor + changedPreviewPosWidth).toInt()
            stampHeightPos = (stamp.positionHeightPer * changedPreviewBitmapHeight / 100000.0f
                    - stamp.height * heightAnchor / 2.0f + changedPreviewPosHeight).toInt()

            EzLogger.d("""stamp.positionWidthPer : ${stamp.positionWidthPer}
                | changedPreviewBitmapWidth : $changedPreviewBitmapWidth
                | stamp.width : ${stamp.width}
                | widthAnchor : $widthAnchor
                | changedPreviewPosWidth : $changedPreviewPosWidth
            """.trimMargin())


            EzLogger.d("stampWidth : ${stamp.width}, stampWidthPos : $stampWidthPos, " +
                    "stampHeight : ${stamp.height}, stampHeightPos : $stampHeightPos")
        } ?: EzLogger.d("setStampComponent stamp null")
    }


    fun stampDeleteListener() {
        hideStamp()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        this.canvas = canvas

        setBackgroundColor(ContextCompat.getColor(context, R.color.backgroundGray))
        preview?.let {

            drawBaseBitmap(it)

            if (isStampShow) {
                drawStamp()
                if (PreviewEditClickStateManager.isShowGuildLine()) {
                    drawStampGuildLine()
                }
            }
        } ?: setBackgroundColor(Color.WHITE)
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
                changedPreviewBitmapWidth = changed[2]
                changedPreviewBitmapHeight = changed[3]
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
                    changedPreviewPosWidth + changedPreviewBitmapWidth,
                    changedPreviewPosHeight + changedPreviewBitmapHeight)
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
            val paintContrastBrightness = RetouachingPaintManager.getPaint(1f, it.getAbsoluteBrightness().toFloat(), 1f, 1f)
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
    private fun drawStampGuildLine() {

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
}