package com.tistory.deque.previewmaker.kotlin.makestamp

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Intent
import android.net.Uri
import android.support.design.widget.Snackbar
import com.tistory.deque.previewmaker.R
import com.tistory.deque.previewmaker.kotlin.base.BaseKotlinViewModel
import com.tistory.deque.previewmaker.kotlin.util.SingleLiveEvent

class KtMakeStampViewModel : BaseKotlinViewModel() {

    private val _stampUriLiveData = MutableLiveData<Uri>()
    val stampUriLiveData: LiveData<Uri> get() = _stampUriLiveData

    private val _finishActivityWithStampNameEvent = SingleLiveEvent<String>()
    val finishActivityWithStampNameEvent: LiveData<String> get() = _finishActivityWithStampNameEvent

    fun setImageUri(data: Intent) {
        _stampUriLiveData.value = data.data
    }

    fun checkName(name: String) {
        when {
            name.isEmpty() -> showSnackbar(R.string.snackbar_make_stamp_acti_no_name_warn)
            name.length > 10 -> showSnackbar(R.string.snackbar_make_stamp_acti_name_len_warn)
            else -> _finishActivityWithStampNameEvent.value = name
        }
    }
}