package com.tistory.deque.previewmaker.kotlin.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.tistory.deque.previewmaker.R
import com.tistory.deque.previewmaker.kotlin.model.enums.PreviewEditStateEnum
import com.tistory.deque.previewmaker.kotlin.manager.PreviewEditStateManager
import com.tistory.deque.previewmaker.kotlin.util.extension.fadeIn
import com.tistory.deque.previewmaker.kotlin.util.extension.fadeOut
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

    var previewEditStateManager = PreviewEditStateManager

    var previewDeleteListener: () -> Unit = {}
    var previewCropListener: () -> Unit = {}
    var previewSaveListener: () -> Unit = {}

    var stampDeleteListener: () -> Unit = {}
    var stampResetListener: () -> Unit = {}

    var filterResetListener: () -> Unit = {}

    private fun initView() {
        val li = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater
        val v = li?.inflate(R.layout.custom_preview_edit_button_group_view, this, false)
        addView(v)

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

        }
        custom_edit_group_home_crop.setOnClickListener {

        }
        custom_edit_group_home_stamp.setOnClickListener {
            previewEditStateManager.setStampState()
        }
        custom_edit_group_home_filter.setOnClickListener {
            previewEditStateManager.setFilterState()
        }
        custom_edit_group_home_save.setOnClickListener {

        }

        //2. stamp
        custom_edit_group_stamp_delete.setOnClickListener {

        }
        custom_edit_group_stamp_brightness.setOnClickListener {
            previewEditStateManager.clickStampBrightness()
        }
        custom_edit_group_stamp_reset.setOnClickListener {

        }
        custom_edit_group_stamp_finish.setOnClickListener {
            previewEditStateManager.finishEdit()
        }

        // 3. filter
        custom_edit_group_filter_bright_contra.setOnClickListener {
            previewEditStateManager.clickFilterBrightContra()
        }
        custom_edit_group_filter_kelvin_satu.setOnClickListener {
            previewEditStateManager.clickFilterKelvinSatu()
        }
        custom_edit_group_filter_blur.setOnClickListener {
            previewEditStateManager.clickFilterBlur()
        }
        custom_edit_group_filter_reset.setOnClickListener {

        }
        custom_edit_group_filter_finish.setOnClickListener {
            previewEditStateManager.finishEdit()
        }
    }

    private fun layoutChange() {
        when (previewEditStateManager.prevState) {
            PreviewEditStateEnum.HOME -> custom_edit_group_home_layout.fadeOut()
            PreviewEditStateEnum.STAMP -> custom_edit_group_stamp_layout.fadeOut()
            PreviewEditStateEnum.FILTER -> custom_edit_group_filter_layout.fadeOut()
            PreviewEditStateEnum.ONE_SEEK_BAR -> custom_edit_group_handler_layout.fadeOut()
            PreviewEditStateEnum.TWO_SEEK_BAR -> custom_edit_group_handler_layout.fadeOut()
            PreviewEditStateEnum.ONLY_CANCEL_OR_OK -> custom_edit_group_handler_layout.fadeOut()
        }
        when (previewEditStateManager.nowState) {
            PreviewEditStateEnum.HOME -> custom_edit_group_home_layout.fadeIn()
            PreviewEditStateEnum.STAMP -> custom_edit_group_stamp_layout.fadeIn()
            PreviewEditStateEnum.FILTER -> custom_edit_group_filter_layout.fadeIn()
            PreviewEditStateEnum.ONE_SEEK_BAR -> {
                custom_edit_group_seek_bar_layout.visibleView()
                custom_edit_group_seek_bar_first_layout.visibleView()
                custom_edit_group_seek_bar_second_layout.goneView()

                custom_edit_group_only_cancel_or_ok_layout.goneView()

                custom_edit_group_handler_finish.visibleView()

                custom_edit_group_handler_layout.fadeIn()
            }
            PreviewEditStateEnum.TWO_SEEK_BAR -> {
                custom_edit_group_seek_bar_layout.visibleView()
                custom_edit_group_seek_bar_first_layout.visibleView()
                custom_edit_group_seek_bar_second_layout.visibleView()

                custom_edit_group_only_cancel_or_ok_layout.goneView()

                custom_edit_group_handler_finish.visibleView()

                custom_edit_group_handler_layout.fadeIn()
            }
            PreviewEditStateEnum.ONLY_CANCEL_OR_OK -> {
                custom_edit_group_seek_bar_layout.goneView()

                custom_edit_group_only_cancel_or_ok_layout.visibleView()

                custom_edit_group_handler_finish.visibleView()

                custom_edit_group_handler_layout.fadeIn()
            }
        }
    }


}