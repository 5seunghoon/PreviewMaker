package com.tistory.deque.previewmaker.kotlin.main

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.tistory.deque.previewmaker.R
import com.tistory.deque.previewmaker.kotlin.db.KtDbOpenHelper
import com.tistory.deque.previewmaker.kotlin.base.BaseKotlinViewModel
import com.tistory.deque.previewmaker.kotlin.model.Stamp
import com.tistory.deque.previewmaker.kotlin.util.EtcConstant
import com.tistory.deque.previewmaker.kotlin.util.EzLogger
import com.tistory.deque.previewmaker.kotlin.util.RequestCode
import com.tistory.deque.previewmaker.kotlin.util.SingleLiveEvent
import me.nereo.multi_image_selector.MultiImageSelectorActivity
import java.io.File
import kotlin.collections.ArrayList

class KtMainViewModel : BaseKotlinViewModel() {
    private val _stampListLiveData = MutableLiveData<ArrayList<Stamp>>()
    val stampListLiveData: LiveData<ArrayList<Stamp>> get() = _stampListLiveData

    private val _addStampLiveData = SingleLiveEvent<Stamp>()
    val addStampLiveData: LiveData<Stamp> get() = _addStampLiveData

    private val _imagePickStartEvent = SingleLiveEvent<Any>()
    val imagePickStartEvent: LiveData<Any> get() = _imagePickStartEvent

    private val _invisibleHintEvent = SingleLiveEvent<Any>()
    val invisibleHintEvent: LiveData<Any> get() = _invisibleHintEvent

    private val _visibleHintEvent = SingleLiveEvent<Any>()
    val visibleHintEvent: LiveData<Any> get() = _visibleHintEvent

    private val _makeStampActivityStartEvent = SingleLiveEvent<Uri>()
    val makeStampActivityStartEvent: LiveData<Uri> get() = _makeStampActivityStartEvent

    private val _delStampAlertStartEvent = SingleLiveEvent<Pair<Stamp, Int>>()
    val delStampAlertStartEvent: LiveData<Pair<Stamp, Int>> get() = _delStampAlertStartEvent

    private val _delStampFromAdapterEvent = SingleLiveEvent<Int>()
    val delStampFromAdapterEvent: LiveData<Int> get() = _delStampFromAdapterEvent

    private val _clickStampEvent = SingleLiveEvent<Stamp>()
    val clickStampEvent: LiveData<Stamp> get() = _clickStampEvent

    private val _galleryAddPicEvent = SingleLiveEvent<Uri>()
    val galleryAddPicEvent: LiveData<Uri> get() = _galleryAddPicEvent

    private val _previewGalleryStartEvent = SingleLiveEvent<Any>()
    val previewGalleryStartEvent: LiveData<Any> get() = _previewGalleryStartEvent

    private val _previewEditStartEvent = SingleLiveEvent<ArrayList<String>>()
    val previewEditStartEvent: LiveData<ArrayList<String>> get() = _previewEditStartEvent

    private var mStampClickTime: Long = 0
    private var dbOpenHelper: KtDbOpenHelper? = null

    var selectedStamp: Stamp? = null

    fun dbOpen(context: Context) {
        EzLogger.d("main activity : db open")
        dbOpenHelper = KtDbOpenHelper.getDbOpenHelper(context,
                KtDbOpenHelper.DP_OPEN_HELPER_NAME,
                null,
                KtDbOpenHelper.dbVersion)
        dbOpenHelper?.dbOpen() ?: EzLogger.d("db open fail : dbOpenHelper null")
    }

    fun getAllStampFromDb() {
        val stampList = dbOpenHelper?.dbGetAll()

        if (stampList != null && stampList.size > 0) {
            _stampListLiveData.value = dbOpenHelper?.dbGetAll()
        }

    }

    fun stampClickListener(stamp: Stamp, position: Int) {
        if(System.currentTimeMillis() - mStampClickTime < 1000){
            return
        }
        mStampClickTime = System.currentTimeMillis()
        _clickStampEvent.value = stamp
    }

    fun stampDeleteListener(stamp: Stamp, position: Int) {
        _delStampAlertStartEvent.value = Pair(stamp, position)
    }

    fun addStampToDb() {
        _imagePickStartEvent.call()
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RequestCode.REQUEST_TAKE_STAMP_FROM_ALBUM -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.let { uri ->
                        _makeStampActivityStartEvent.value = uri
                    }
                }
            }

            RequestCode.REQUEST_MAKE_STAMP_ACTIVITY -> {
                if (resultCode == Activity.RESULT_OK) {
                    addStampToDb(data ?: return)
                }
            }

            RequestCode.REQUEST_TAKE_PREVIEW_FROM_ALBUM -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.let {
                        val path = it.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT)
                        _previewEditStartEvent.value = path
                    }

                }
            }

        }
    }

    private fun addStampToDb(intent: Intent) {
        dbOpenHelper?.let {
            it.dbInsertStamp(intent.getStringExtra(EtcConstant.STAMP_NAME_INTENT_KEY) ?: return, intent.data
                    ?: return)

            it.db?.rawQuery("SELECT MAX(_id) FROM ${KtDbOpenHelper.TABLE_NAME_STAMPS}", null)?.use { cursor ->
                cursor.moveToFirst()
                val maxId = cursor.getInt(0)
                val newStamp = Stamp(maxId,
                        intent.data ?: return,
                        intent.getStringExtra(EtcConstant.STAMP_NAME_INTENT_KEY) ?: return)
                EzLogger.d("new stamp : $newStamp")
                _addStampLiveData.value = newStamp
            }
        }
    }

    fun deleteStampAndScan(context: Context, stamp: Stamp, position: Int) {
        EzLogger.d("delete position : $position, stamp : $stamp")

        val stampFile = File(stamp.imageUri.path)
        if (stampFile.exists()) {
            if(stampFile.delete()){
                _galleryAddPicEvent.value = stamp.imageUri
            }
        }
        dbOpenHelper?.dbDeleteStamp(stamp.id) // db에서 삭제
        _delStampFromAdapterEvent.value = position // 어뎁터에서 삭제
        _visibleHintEvent.call() // 힌트 보여줄지 체크하고 힌트 보여줌
        showSnackbar("[${stamp.name}] ${context.resources.getString(R.string.main_stamp_delete_success_snackbar)}")
    }

    fun savePositionAndGetPreview(stamp: Stamp) {
        selectedStamp = stamp
        _previewGalleryStartEvent.call()
    }
}