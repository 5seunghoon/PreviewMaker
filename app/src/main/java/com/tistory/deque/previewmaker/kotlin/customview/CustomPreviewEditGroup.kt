package com.tistory.deque.previewmaker.kotlin.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import com.tistory.deque.previewmaker.R
import com.tistory.deque.previewmaker.kotlin.model.enums.PreviewEditButtonViewStateEnum
import com.tistory.deque.previewmaker.kotlin.manager.PreviewEditButtonViewStateManager
import com.tistory.deque.previewmaker.kotlin.manager.PreviewEditClickStateManager
import com.tistory.deque.previewmaker.kotlin.model.enums.PreviewEditClickStateEnum
import com.tistory.deque.previewmaker.kotlin.model.enums.SeekBarStateEnum
import com.tistory.deque.previewmaker.kotlin.util.EtcConstant
import com.tistory.deque.previewmaker.kotlin.util.EzLogger
import com.tistory.deque.previewmaker.kotlin.util.extension.goneView
import com.tistory.deque.previewmaker.kotlin.util.extension.invisibleView
import com.tistory.deque.previewmaker.kotlin.util.extension.visibleView
import kotlinx.android.synthetic.main.custom_preview_edit_button_group_view.view.*

class CustomPreviewEditGroup : LinearLayout {
    init {
        initView()
    }

    constructor(context: Context) : super(context)
    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)
    constructor(ctx: Context, attrs: AttributeSet, defStyleAttr: Int) : super(ctx, attrs, defStyleAttr)

    var homeCropListener: () -> Unit = {}
    var homeSaveListener: () -> Unit = {}

    var customPreviewCanvas: CustomPreviewCanvas? = null

    val stampBrightnessSeekBarListener = SeekBarListener(SeekBarStateEnum.STATE_STAMP_BRIGHTNESS)
    val previewBrightnessSeekBarListener = SeekBarListener(SeekBarStateEnum.STATE_PREVIEW_BRIGHTNESS)
    val previewContrastSeekBarListener = SeekBarListener(SeekBarStateEnum.STATE_PREVIEW_CONTRAST)
    val previewSaturationSeekBarListener = SeekBarListener(SeekBarStateEnum.STATE_PREVIEW_SATURATION)
    val previewKelvinSeekBarListener = SeekBarListener(SeekBarStateEnum.STATE_PREVIEW_KELVIN)


    private fun initView() {
        val li = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater
        val v = li?.inflate(R.layout.custom_preview_edit_button_group_view, this, false)
        addView(v)

        PreviewEditButtonViewStateManager.initState()

        initVisibility()
        setClickListener()
        setSeekBar()
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
            customPreviewCanvas?.stampDeleteListener()
            layoutChange()
        }
        custom_edit_group_stamp_brightness.setOnClickListener {
            PreviewEditButtonViewStateManager.clickStampBrightness()

            custom_edit_group_seek_bar_first.run {
                setOnSeekBarChangeListener(stampBrightnessSeekBarListener)
                max = EtcConstant.SeekBarStampBrightnessMax
                progress = customPreviewCanvas?.stamp?.brightness ?: (max / 2)
            }
            custom_edit_group_seek_bar_first_hint_text_view.run { post { text = "밝기" } }
            setStampSeekBarText(custom_edit_group_seek_bar_first.progress, SeekBarStateEnum.STATE_STAMP_BRIGHTNESS)

            layoutChange()
        }
        custom_edit_group_stamp_reset.setOnClickListener {
            customPreviewCanvas?.stampResetListener()
        }
        custom_edit_group_stamp_finish.setOnClickListener {
            PreviewEditButtonViewStateManager.finishEdit()
            customPreviewCanvas?.stampFinishListener()
            layoutChange()
        }

        // 3. filter
        custom_edit_group_filter_bright_contra.setOnClickListener {
            PreviewEditButtonViewStateManager.clickFilterBrightContra()

            custom_edit_group_seek_bar_first.run {
                setOnSeekBarChangeListener(previewBrightnessSeekBarListener)
                max = EtcConstant.SeekBarPreviewBrightnessMax
                progress = customPreviewCanvas?.preview?.brightness ?: (max / 2)
            }
            custom_edit_group_seek_bar_first_hint_text_view.run { post { text = "밝기" } }
            setStampSeekBarText(custom_edit_group_seek_bar_first.progress, SeekBarStateEnum.STATE_PREVIEW_BRIGHTNESS)

            custom_edit_group_seek_bar_second.run {
                setOnSeekBarChangeListener(previewContrastSeekBarListener)
                max = EtcConstant.SeekBarPreviewContrastMax
                progress = customPreviewCanvas?.preview?.contrast ?: (max / 2)
            }
            custom_edit_group_seek_bar_second_hint_text_view.run { post { text = "대비" } }
            setStampSeekBarText(custom_edit_group_seek_bar_second.progress, SeekBarStateEnum.STATE_PREVIEW_CONTRAST)

            layoutChange()
        }
        custom_edit_group_filter_kelvin_satu.setOnClickListener {
            PreviewEditButtonViewStateManager.clickFilterKelvinSatu()

            custom_edit_group_seek_bar_first.run {
                setOnSeekBarChangeListener(previewSaturationSeekBarListener)
                max = EtcConstant.SeekBarPreviewBrightnessMax
                progress = customPreviewCanvas?.preview?.saturation ?: (max / 2)
            }
            custom_edit_group_seek_bar_first_hint_text_view.run { post { text = "색조" } }
            setStampSeekBarText(custom_edit_group_seek_bar_first.progress, SeekBarStateEnum.STATE_PREVIEW_SATURATION)

            custom_edit_group_seek_bar_second.run {
                setOnSeekBarChangeListener(previewKelvinSeekBarListener)
                max = EtcConstant.SeekBarPreviewContrastMax
                progress = customPreviewCanvas?.preview?.kelvin ?: (max / 2)
            }
            custom_edit_group_seek_bar_second_hint_text_view.run { post { text = "색온도" } }
            setStampSeekBarText(custom_edit_group_seek_bar_second.progress, SeekBarStateEnum.STATE_PREVIEW_KELVIN)

            layoutChange()
        }
        custom_edit_group_filter_blur.setOnClickListener {
            PreviewEditButtonViewStateManager.clickFilterBlur()
            layoutChange()
        }
        custom_edit_group_filter_reset.setOnClickListener {
            customPreviewCanvas?.filterResetListener()
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

    private fun setSeekBar() {
        custom_edit_group_seek_bar_first.max = EtcConstant.SeekBarPreviewBrightnessMax
        custom_edit_group_seek_bar_second.max = EtcConstant.SeekBarPreviewContrastMax
    }

    private fun layoutChange() {
        val ps = PreviewEditButtonViewStateManager.prevState
        val ns = PreviewEditButtonViewStateManager.nowState
        EzLogger.d("ps : $ps, ns : $ns")
        if(ps == ns) return

        allLayoutInvisible()
        when(ns){
            PreviewEditButtonViewStateEnum.HOME -> custom_edit_group_home_layout.visibleView()
            PreviewEditButtonViewStateEnum.STAMP -> custom_edit_group_stamp_layout.visibleView()
            PreviewEditButtonViewStateEnum.FILTER -> custom_edit_group_filter_layout.visibleView()
            PreviewEditButtonViewStateEnum.ONE_SEEK_BAR -> {
                custom_edit_group_handler_layout.visibleView()
                custom_edit_group_seek_bar_layout.visibleView()
                custom_edit_group_seek_bar_first_layout.visibleView()
            }
            PreviewEditButtonViewStateEnum.TWO_SEEK_BAR -> {
                custom_edit_group_handler_layout.visibleView()
                custom_edit_group_seek_bar_layout.visibleView()
                custom_edit_group_seek_bar_first_layout.visibleView()
                custom_edit_group_seek_bar_second_layout.visibleView()
            }
            PreviewEditButtonViewStateEnum.ONLY_CANCEL_OR_OK -> {
                custom_edit_group_handler_layout.visibleView()
                custom_edit_group_only_cancel_or_ok_layout.visibleView()
            }
        }
    }

    private fun allLayoutInvisible(){
        ArrayList<View>().apply {
            add(custom_edit_group_home_layout)
            add(custom_edit_group_stamp_layout)
            add(custom_edit_group_filter_layout)
            add(custom_edit_group_handler_layout)
            add(custom_edit_group_seek_bar_layout)
            add(custom_edit_group_seek_bar_first_layout)
            add(custom_edit_group_seek_bar_second_layout)
            add(custom_edit_group_only_cancel_or_ok_layout)
        }.forEach {
            it.invisibleView()
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

    inner class SeekBarListener(val seekBarStateEnum: SeekBarStateEnum) : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            when (seekBarStateEnum) {
                SeekBarStateEnum.STATE_STAMP_BRIGHTNESS -> customPreviewCanvas?.stampBrightnessSeekBarListener(progress)
                SeekBarStateEnum.STATE_PREVIEW_BRIGHTNESS -> customPreviewCanvas?.previewBrightnessSeekBarListener(progress)
                SeekBarStateEnum.STATE_PREVIEW_CONTRAST -> customPreviewCanvas?.previewContrastSeekBarListener(progress)
                SeekBarStateEnum.STATE_PREVIEW_SATURATION -> customPreviewCanvas?.previewSaturationSeekBarListener(progress)
                SeekBarStateEnum.STATE_PREVIEW_KELVIN -> customPreviewCanvas?.previewKelvinSeekBarListener(progress)
            }
            setStampSeekBarText(progress, seekBarStateEnum)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }

    fun setStampSeekBarText(value: Int, seekBarState: SeekBarStateEnum) {
        val resultProgressValue: Int
        when (seekBarState) {
            SeekBarStateEnum.STATE_STAMP_BRIGHTNESS -> {
                resultProgressValue = ((value - EtcConstant.SeekBarStampBrightnessMax / 2f) / (EtcConstant.SeekBarStampBrightnessMax / 2f) * 100f).toInt()
                custom_edit_group_seek_bar_first_value_text_view.text = String.format("%3d%%", resultProgressValue)
            }
            SeekBarStateEnum.STATE_STAMP_CONTRAST -> {
            }
            SeekBarStateEnum.STATE_PREVIEW_BRIGHTNESS -> {
                resultProgressValue = ((value - EtcConstant.SeekBarPreviewBrightnessMax / 2f) / (EtcConstant.SeekBarPreviewBrightnessMax / 2f) * 100f).toInt()
                custom_edit_group_seek_bar_first_value_text_view.text = String.format("%3d%%", resultProgressValue)
            }
            SeekBarStateEnum.STATE_PREVIEW_CONTRAST -> {
                resultProgressValue = ((value - EtcConstant.SeekBarPreviewContrastMax / 2f) / (EtcConstant.SeekBarPreviewContrastMax / 2f) * 100f).toInt()
                custom_edit_group_seek_bar_second_value_text_view.text = String.format("%3d%%", resultProgressValue)
            }
            SeekBarStateEnum.STATE_PREVIEW_SATURATION -> {
                resultProgressValue = ((value - EtcConstant.SeekBarPreviewSaturationMax / 2f) / (EtcConstant.SeekBarPreviewSaturationMax / 2f) * 100f).toInt()
                custom_edit_group_seek_bar_first_value_text_view.text = String.format("%3d%%", resultProgressValue)
            }
            SeekBarStateEnum.STATE_PREVIEW_KELVIN -> {
                resultProgressValue = ((value - EtcConstant.SeekBarPreviewKelvinMax / 2f) / (EtcConstant.SeekBarPreviewKelvinMax / 2f) * 100f).toInt()
                custom_edit_group_seek_bar_second_value_text_view.text = String.format("%3d%%", resultProgressValue)
            }
        }
    }

}