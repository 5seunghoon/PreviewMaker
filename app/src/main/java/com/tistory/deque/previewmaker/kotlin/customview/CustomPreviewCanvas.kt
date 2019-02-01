package com.tistory.deque.previewmaker.kotlin.customview

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.tistory.deque.previewmaker.kotlin.manager.PreviewEditStateManager
import com.tistory.deque.previewmaker.kotlin.model.Preview

class CustomPreviewCanvas: View{
    init {
        initView()
    }

    var activity: Activity? = null
    var preview: Preview? = null

    constructor(context: Context) : super(context)
    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)
    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int) : super(ctx, attrs, defStyleAttr)

    private fun initView(){

    }

    fun setComponent(activity: Activity, preview: Preview){
        this.activity = activity
        this.preview = preview

        PreviewEditStateManager.initState()
    }

}