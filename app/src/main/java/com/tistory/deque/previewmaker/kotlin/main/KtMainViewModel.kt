package com.tistory.deque.previewmaker.kotlin.main

import android.app.Activity
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.tistory.deque.previewmaker.R
import com.tistory.deque.previewmaker.kotlin.KtDbOpenHelper
import com.tistory.deque.previewmaker.kotlin.base.BaseKotlinViewModel
import com.tistory.deque.previewmaker.kotlin.makestamp.KtMakeStampActivity
import com.tistory.deque.previewmaker.kotlin.model.Stamp
import com.tistory.deque.previewmaker.kotlin.util.EzLogger
import com.tistory.deque.previewmaker.kotlin.util.RequestCode
import com.tistory.deque.previewmaker.kotlin.util.SingleLiveEvent
import com.tistory.deque.previewmaker.kotlin.util.extension.getRealPath
import com.tistory.deque.previewmaker.kotlin.util.extension.getUri
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

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

    private var dbOpenHelper: KtDbOpenHelper? = null

    private var mCropSourceUri: Uri? = null
    private var mCropEndURI: Uri? = null //  mCropSourceUri = 자를 uri, mCropEndURI = 자르고 난뒤 uri

    private var patedStampUri: Uri? = null

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
            _invisibleHintEvent.call()
        }

    }

    fun stampClickListener(stamp: Stamp) {

    }

    fun stampDeleteListener(stamp: Stamp) {

    }

    fun addStamp() {
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
                    addStampToListAndDB(data ?: return)
                }
            }

        }
    }

    private fun addStampToListAndDB(intent: Intent) {
        dbOpenHelper?.let {
            it.dbInsertStamp(intent.getStringExtra(KtMakeStampActivity.STAMP_NAME_INTENT_KEY), intent.data
                    ?: return)

            it.db?.rawQuery("SELECT MAX(_id) FROM ${KtDbOpenHelper.TABLE_NAME_STAMPS}", null)?.use { cursor ->
                cursor.moveToFirst()
                val maxId = cursor.getInt(0)

                val newStamp = Stamp(maxId, intent.data ?: return, intent.getStringExtra(KtMakeStampActivity.STAMP_NAME_INTENT_KEY))
                EzLogger.d("new stamp : $newStamp")
                _addStampLiveData.value = newStamp
            }
        }
    }
}