package com.tistory.deque.previewmaker.kotlin.setting

import android.content.Context
import android.widget.SeekBar
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tistory.deque.previewmaker.R
import com.tistory.deque.previewmaker.kotlin.base.BaseKotlinViewModel
import com.tistory.deque.previewmaker.kotlin.manager.SharedPreferencesManager
import com.tistory.deque.previewmaker.kotlin.util.EtcConstant

class KtSettingViewModel : BaseKotlinViewModel() {

    data class PreferencesValue(val previewSizeLimit: Int)

    private val _previewSizeLimitSeekBarEvent = MutableLiveData<Int>()
    val previewSizeLimitSeekBarEvent: LiveData<Int> get() = _previewSizeLimitSeekBarEvent

    private lateinit var prevPreferencesValue: PreferencesValue

    private val previewSizeSeekBarInterval = 100

    val previewSizeLimitSeekBarListener: SeekBar.OnSeekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            _previewSizeLimitSeekBarEvent.value = progress
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}

        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }

    fun generatePreviewSizeLimitSeekBarToReal(seekBarProgress: Int): Int {
        return (seekBarProgress * previewSizeSeekBarInterval + EtcConstant.PREVIEW_BITMAP_SIZE_LIMIT_MIN)
    }

    fun generatePreviewSizeLimitRealToSeekBar(limit: Int): Int {
        return (limit - EtcConstant.PREVIEW_BITMAP_SIZE_LIMIT_MIN) / previewSizeSeekBarInterval
    }

    fun initPreferencesValue(context: Context) {
        prevPreferencesValue = PreferencesValue(SharedPreferencesManager.getPreviewBitmapSizeLimit(context))
    }

    private fun buildNewPreferencesValue(previewSizeLimitSeekBarProgress: Int): PreferencesValue {
        return PreferencesValue(generatePreviewSizeLimitSeekBarToReal(previewSizeLimitSeekBarProgress))
    }

    fun isPreferencesValueChanged(previewSizeLimitSeekBarProgress: Int): Boolean {
        val newPreferencesValue = buildNewPreferencesValue(previewSizeLimitSeekBarProgress)
        return (newPreferencesValue != prevPreferencesValue)
    }

    fun savePreferences(context: Context, previewSizeLimitSeekBarProgress: Int) {
        SharedPreferencesManager.setPreviewBitmapSizeLimit(context, previewSizeLimitSeekBarProgress)
        showSnackbar(R.string.setting_save_success_snackbar_message)
    }
}