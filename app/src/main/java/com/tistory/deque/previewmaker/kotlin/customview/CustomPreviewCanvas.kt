package com.tistory.deque.previewmaker.kotlin.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import com.tistory.deque.previewmaker.R
import com.tistory.deque.previewmaker.kotlin.manager.PreviewBitmapManager
import com.tistory.deque.previewmaker.kotlin.manager.PreviewEditButtonViewStateManager
import com.tistory.deque.previewmaker.kotlin.manager.PreviewEditClickStateManager
import com.tistory.deque.previewmaker.kotlin.model.Preview
import com.tistory.deque.previewmaker.kotlin.model.Stamp
import com.tistory.deque.previewmaker.kotlin.previewedit.KtPreviewEditActivity
import com.tistory.deque.previewmaker.kotlin.previewedit.KtPreviewEditViewModel
import com.tistory.deque.previewmaker.kotlin.util.EzLogger

class CustomPreviewCanvas: View{
    init {
        initView()
    }

    private var activity: KtPreviewEditActivity? = null
    private var canvas: Canvas? = null

    val preview: Preview? get() = activity?.viewModel?.selectedPreview
    val stamp: Stamp? get() = activity?.viewModel?.stamp

    var isStampShow: Boolean = false

    constructor(context: Context) : super(context)
    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)
    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int) : super(ctx, attrs, defStyleAttr)

    private fun initView(){

    }

    fun setComponent(activity: KtPreviewEditActivity){
        EzLogger.d("setComponent viewModel set ")
        this.activity = activity
        PreviewEditButtonViewStateManager.initState()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        this.canvas = canvas

        setBackgroundColor(ContextCompat.getColor(context, R.color.backgroundGray))
        preview?.let {

            drawBaseBitmap(it)

            if(isStampShow) {
                drawStamp()
                if(PreviewEditClickStateManager.isShowGuildLine()) {
                    drawStampGuildLine()
                }
            }
        } ?: setBackgroundColor(Color.WHITE)
    }

    private fun drawStamp(){
        EzLogger.d("custom preview canvas, draw stamp : $stamp")
    }

    private fun drawStampGuildLine(){

    }

    private fun drawBaseBitmap(preview:Preview){
        EzLogger.d("preview : ${preview.originalImageUri}")
    }

}