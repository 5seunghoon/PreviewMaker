package com.tistory.deque.previewmaker.kotlin.setting

import android.widget.SeekBar
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tistory.deque.previewmaker.kotlin.base.BaseKotlinViewModel

class KtSettingViewModel : BaseKotlinViewModel() {

    private val _previewSizeLimitSeekBarEvent = MutableLiveData<Int>()
    val previewSizeLimitSeekBarEvent: LiveData<Int> get() = _previewSizeLimitSeekBarEvent


    val previewSizeLimitSeekBarListener: SeekBar.OnSeekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            _previewSizeLimitSeekBarEvent.value = progress
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}

        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }

}