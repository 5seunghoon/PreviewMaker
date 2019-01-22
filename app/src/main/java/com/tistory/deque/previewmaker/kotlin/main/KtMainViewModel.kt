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

    companion object {
        const val FILE_NAME_FORMAT = "yyyyMMddHHmmssSSS"
        const val FILE_NAME_HEADER_STAMP = "STAMP_"
        const val FILE_NAME_HEADER_PREVIEW = "PREVIEW_"
        const val FILE_NAME_IMAGE_FORMAT = ".png"

        const val MAIN_DIRECTORY = "Pictures"
        const val PREVIEW_SAVED_DIRECTORY = "Preview" + " " + "Maker"
        const val STAMP_SAVED_DIRECTORY = "Stamp"
    }


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

    private val _galleryAddPicEvent = SingleLiveEvent<String>()
    val galleryAddPicEvent: LiveData<String> get() = _galleryAddPicEvent

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

    fun onActivityResult(context: Context, requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RequestCode.REQUEST_TAKE_STAMP_FROM_ALBUM -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.let { uri ->
                        _makeStampActivityStartEvent.value = uri

                        /*
                        EzLogger.d("stamp original uri : $uri")
                        if (checkStampSizeValid(context.contentResolver, uri)) {
                            val outFile = createImageFile()
                            EzLogger.d("outFile path : file.absolutePath -> ${outFile.absolutePath}")
                            val sourceFile = File(uri.getRealPath(context.contentResolver) ?: return)
                            EzLogger.d("sourceFile uri.getRealPath() : ${uri.getRealPath(context.contentResolver)}")
                            copyAndPasteImage(sourceFile, outFile)
                            _makeStampActivityStartEvent.value = Uri.fromFile(outFile)
                        }*/
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

    private fun copyAndPasteImage(sourceFile: File, outFile: File) {
        if (sourceFile.exists()) {
            try {
                val inFis = FileInputStream(sourceFile)
                val outFis = FileOutputStream(outFile)
                var readcount: Int = 0
                val buffer = ByteArray(1024)

                while (true) {
                    readcount = inFis.read(buffer, 0, 24)
                    if (readcount == -1) break
                    outFis.write(buffer, 0, readcount)
                }
                inFis.close()
                outFis.close()
            } catch (e: IOException) {
                EzLogger.d("File copy fail")
                e.printStackTrace()
                showSnackbar(R.string.snackbar_main_acti_stamp_copy_err)
            }
        } else {
            EzLogger.d("In file not exist")
            showSnackbar(R.string.snackbar_main_stamp_source_not_exist)
        }
        _galleryAddPicEvent.value = outFile.absolutePath
    }

    private fun createImageFile(): File {
        EzLogger.d("createImageFile func")
        val timeStamp = SimpleDateFormat(FILE_NAME_FORMAT, Locale.KOREA).format(Date())
        val imageFileName = FILE_NAME_HEADER_STAMP + timeStamp + FILE_NAME_IMAGE_FORMAT
        EzLogger.d("image file name : $imageFileName")


        val root: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val storageParentDir = File(root, PREVIEW_SAVED_DIRECTORY)
        val storageDir = File(root.toString() + "/" + PREVIEW_SAVED_DIRECTORY, STAMP_SAVED_DIRECTORY)
        EzLogger.d("storageParentDir : $storageParentDir, storageDir : $storageDir")

        if (!storageParentDir.exists()) {
            storageParentDir.mkdirs()
            storageDir.mkdirs()
        }
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        val imageFile = File(storageDir, imageFileName)
        EzLogger.d("imageFil.absolutePath : ${imageFile.absolutePath}")

        return imageFile
    }

    private fun checkStampSizeValid(contentResolver: ContentResolver, stampBaseUri: Uri): Boolean {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, stampBaseUri)
            if (bitmap.height >= 2000 || bitmap.width >= 2000) {
                EzLogger.d("SIZE OVER")
                showSnackbar(R.string.snackbar_main_acti_stamp_size_over_err)
                return false
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
        return true
    }
}