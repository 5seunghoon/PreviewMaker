package com.tistory.deque.previewmaker.kotlin.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.tistory.deque.previewmaker.R
import com.tistory.deque.previewmaker.kotlin.model.enums.PreviewEditButtonViewStateEnum
import com.tistory.deque.previewmaker.kotlin.manager.PreviewEditButtonViewStateManager
import com.tistory.deque.previewmaker.kotlin.manager.PreviewEditClickStateManager
import com.tistory.deque.previewmaker.kotlin.model.enums.PreviewEditClickStateEnum
import com.tistory.deque.previewmaker.kotlin.util.EzLogger
import com.tistory.deque.previewmaker.kotlin.util.extension.goneView
import com.tistory.deque.previewmaker.kotlin.util.extension.visibleView
import kotlinx.android.synthetic.main.custom_preview_edit_button_group_view.view.*

class CustomPreviewEditGroup : LinearLayout {
    init {
        initView()
    }

    constructor(context: Context) : super(context)
    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)
    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int) : super(ctx, attrs, defStyleAttr)

    var homeDeleteListener: () -> Unit = {}
    var homeCropListener: () -> Unit = {}
    var homeStampListener: () -> Unit = {}
    var homeSaveListener: () -> Unit = {}

    var stampDeleteListener: () -> Unit = {}
    var stampResetListener: () -> Unit = {}
    var stampFinishListener: () -> Unit = {}

    var filterResetListener: () -> Unit = {}

    var customPreviewCanvas:CustomPreviewCanvas? = null

    private fun initView() {
        val li = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater
        val v = li?.inflate(R.layout.custom_preview_edit_button_group_view, this, false)
        addView(v)

        PreviewEditButtonViewStateManager.initState()

        initVisibility()
        setClickListener()
    }

    private fun initVisibility() {
        custom_edit_group_home_layout.visibleView()
        custom_edit_group_stamp_layout.goneView()
        custom_edit_group_filter_layout.goneView()
        custom_edit_group_handler_layout.goneView()
    }

    private fun setClickListener() {
        //1. home
        custom_edit_group_home_delete.setOnClickListener {
            homeDeleteListener()
        }
        custom_edit_group_home_crop.setOnClickListener {
            homeCropListener()
        }
        custom_edit_group_home_stamp.setOnClickListener {
            PreviewEditButtonViewStateManager.setStampState()
            customPreviewCanvas?.homeStampListener()
            layoutChange()
        }
        custom_edit_group_home_filter.setOnClickListener {
            PreviewEditButtonViewStateManager.setFilterState()
            customPreviewCanvas?.homeFilterListener()
            layoutChange()
        }
        custom_edit_group_home_save.setOnClickListener {
            homeSaveListener()
        }

        //2. stamp
        custom_edit_group_stamp_delete.setOnClickListener {
            PreviewEditButtonViewStateManager.finishEdit()
            stampDeleteListener()
            customPreviewCanvas?.stampDeleteListener()
            layoutChange()
        }
        custom_edit_group_stamp_brightness.setOnClickListener {
            PreviewEditButtonViewStateManager.clickStampBrightness()
            layoutChange()
        }
        custom_edit_group_stamp_reset.setOnClickListener {
            stampResetListener()
        }
        custom_edit_group_stamp_finish.setOnClickListener {
            PreviewEditButtonViewStateManager.finishEdit()
            stampFinishListener()
            customPreviewCanvas?.stampFinishListener()
            layoutChange()
        }

        // 3. filter
        custom_edit_group_filter_bright_contra.setOnClickListener {
            PreviewEditButtonViewStateManager.clickFilterBrightContra()
            layoutChange()
        }
        custom_edit_group_filter_kelvin_satu.setOnClickListener {
            PreviewEditButtonViewStateManager.clickFilterKelvinSatu()
            layoutChange()
        }
        custom_edit_group_filter_blur.setOnClickListener {
            PreviewEditButtonViewStateManager.clickFilterBlur()
            layoutChange()
        }
        custom_edit_group_filter_reset.setOnClickListener {
            filterResetListener()
        }
        custom_edit_group_filter_finish.setOnClickListener {
            PreviewEditButtonViewStateManager.finishEdit()
            customPreviewCanvas?.filterFinishListener()
            layoutChange()
        }

        // 4.
        custom_edit_group_handler_finish.setOnClickListener {
            PreviewEditButtonViewStateManager.finishEdit()
            layoutChange()
        }
        custom_edit_group_handler_cancel.setOnClickListener {
            PreviewEditButtonViewStateManager.finishEdit()
            layoutChange()
        }
    }

    private fun layoutChange() {
        val ps = PreviewEditButtonViewStateManager.prevState
        val ns = PreviewEditButtonViewStateManager.nowState
        EzLogger.d("ps : $ps, ns : $ns")
        when (ps) {
            PreviewEditButtonViewStateEnum.HOME -> {
                when (ns) {
                    PreviewEditButtonViewStateEnum.STAMP -> {
                        custom_edit_group_stamp_layout.visibleView()
                        custom_edit_group_filter_layout.goneView()
                        custom_edit_group_handler_layout.goneView()
                    }
                    PreviewEditButtonViewStateEnum.FILTER -> {
                        custom_edit_group_filter_layout.visibleView()
                        custom_edit_group_handler_layout.goneView()
                    }
                }
            }
            PreviewEditButtonViewStateEnum.STAMP -> {
                when (ns) {
                    PreviewEditButtonViewStateEnum.ONE_SEEK_BAR -> {
                        custom_edit_group_handler_layout.visibleView()
                        custom_edit_group_seek_bar_layout.visibleView()
                        custom_edit_group_only_cancel_or_ok_layout.goneView()
                        custom_edit_group_seek_bar_first_layout.visibleView()
                        custom_edit_group_seek_bar_second_layout.goneView()
                    }
                    PreviewEditButtonViewStateEnum.HOME -> {
                        custom_edit_group_stamp_layout.goneView()
                        custom_edit_group_filter_layout.goneView()
                        custom_edit_group_handler_layout.goneView()
                    }
                }
            }
            PreviewEditButtonViewStateEnum.FILTER -> {
                when (ns) {
                    PreviewEditButtonViewStateEnum.TWO_SEEK_BAR -> {
                        custom_edit_group_handler_layout.visibleView()
                        custom_edit_group_seek_bar_layout.visibleView()
                        custom_edit_group_only_cancel_or_ok_layout.goneView()
                        custom_edit_group_seek_bar_first_layout.visibleView()
                        custom_edit_group_seek_bar_second_layout.visibleView()
                    }
                    PreviewEditButtonViewStateEnum.ONLY_CANCEL_OR_OK -> {
                        custom_edit_group_handler_layout.visibleView()
                        custom_edit_group_seek_bar_layout.goneView()
                        custom_edit_group_only_cancel_or_ok_layout.visibleView()
                    }
                    PreviewEditButtonViewStateEnum.HOME -> {
                        custom_edit_group_filter_layout.goneView()
                        custom_edit_group_stamp_layout.goneView()
                        custom_edit_group_handler_layout.goneView()
                    }
                }
            }
            PreviewEditButtonViewStateEnum.ONE_SEEK_BAR -> {
                when (ns) {
                    PreviewEditButtonViewStateEnum.STAMP -> {
                        custom_edit_group_handler_layout.goneView()
                    }
                }
            }
            PreviewEditButtonViewStateEnum.TWO_SEEK_BAR -> {
                when (ns) {
                    PreviewEditButtonViewStateEnum.FILTER -> {
                        custom_edit_group_handler_layout.goneView()
                    }
                }
            }
            PreviewEditButtonViewStateEnum.ONLY_CANCEL_OR_OK -> {
                when (ns) {
                    PreviewEditButtonViewStateEnum.FILTER -> {
                        custom_edit_group_handler_layout.goneView()
                    }
                }
            }
        }
    }

    fun setSyncClickState() {
        EzLogger.d("setSyncClickState, nowState : ${PreviewEditClickStateManager.nowState}")
        if (PreviewEditClickStateManager.nowState == PreviewEditClickStateEnum.STATE_STAMP_EDIT
                || PreviewEditClickStateManager.nowState == PreviewEditClickStateEnum.STATE_STAMP_ZOOM) {
            PreviewEditButtonViewStateManager.forceChangeStampState()
        }
        layoutChange()
    }

}