package com.tistory.deque.previewmaker.kotlin.makestamp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import com.tistory.deque.previewmaker.R
import com.tistory.deque.previewmaker.kotlin.base.BaseKotlinViewModel
import com.tistory.deque.previewmaker.kotlin.manager.FilePathManager
import com.tistory.deque.previewmaker.kotlin.util.EtcConstant
import com.tistory.deque.previewmaker.kotlin.util.EzLogger
import com.tistory.deque.previewmaker.kotlin.util.SingleLiveEvent
import com.tistory.deque.previewmaker.kotlin.util.extension.getRealPath
import java.io.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


class KtMakeStampViewModel : BaseKotlinViewModel() {

    private val _galleryAddPicEvent = SingleLiveEvent<Uri>()
    val galleryAddPicEvent: LiveData<Uri> get() = _galleryAddPicEvent

    private val _stampUriEvent = MutableLiveData<Uri>()
    val stampUriEvent: LiveData<Uri> get() = _stampUriEvent

    private val _finishActivityWithStampNameEvent = SingleLiveEvent<String>()
    val finishActivityWithStampNameEvent: LiveData<String> get() = _finishActivityWithStampNameEvent

    private var stampSourceUri: Uri? = null

    fun setImageView(context: Context, data: Intent) {
        data.data?.let { sourceUri ->
            this.stampSourceUri = sourceUri
            if (checkStampSizeValid(context.contentResolver)) {
                _stampUriEvent.value = sourceUri
            }
        }
    }

    fun clickOkButton(context: Context, name: String, needsHidden: Boolean) {
        if (isValidStampName(name) && checkStampSizeValid(context.contentResolver)) {
            val isMakingStampSuccess = makeStamp(context, needsHidden)
            if (isMakingStampSuccess) {
                _finishActivityWithStampNameEvent.value = name
            } else {
                showSnackbar(R.string.stamp_error_goto_start)
            }
        }
    }

    private fun isValidStampName(name: String): Boolean {
        return when {
            name.isEmpty() -> {
                showSnackbar(R.string.snackbar_make_stamp_acti_no_name_warn)
                false
            }
            name.length > 10 -> {
                showSnackbar(R.string.snackbar_make_stamp_acti_name_len_warn)
                false
            }
            else -> true
        }
    }

    private fun makeStamp(context: Context, needsHidden: Boolean): Boolean {
        makeStampFile(context, stampSourceUri ?: return false, needsHidden) ?: return false
        return true
    }

    private fun makeStampFile(context: Context, sourceUri: Uri, needsHidden: Boolean): File? {
        if (checkStampSizeValid(context.contentResolver)) {
            val outFile = createEmptyImageFile(needsHidden)
            EzLogger.d("outFile path : file.absolutePath -> ${outFile.absolutePath}")
            val sourceFile = File(sourceUri.getRealPath(context.contentResolver) ?: return null)
            EzLogger.d("sourceFile uri.getRealPath() : ${sourceUri.getRealPath(context.contentResolver)}")

            if (needsHidden) {
                FilePathManager.makeNoMediaFile(context)
            }
            copyAndPasteImage(sourceFile, outFile)
            return outFile
        } else {
            return null
        }
    }

    //sourceFile의 이미지를 outFile로 붙여넣음
    private fun copyAndPasteImage(sourceFile: File, outFile: File) {
        try {
            EzLogger.d("copy and paste iamge : sourcefile : $sourceFile, outfile : $outFile")
            sourceFile.copyTo(outFile, true, 1024)
        } catch (e: IOException) {
            EzLogger.d("File copy fail")
            e.printStackTrace()
            showSnackbar(R.string.snackbar_main_acti_stamp_copy_err)
            return
        } catch (e: Exception) {
            when (e) {
                is IOException, is NoSuchFileException -> {
                    EzLogger.d("File copy fail")
                    e.printStackTrace()
                    showSnackbar(R.string.snackbar_main_acti_stamp_copy_err)
                    return
                }
            }
        }
        _galleryAddPicEvent.value = Uri.fromFile(outFile)
    }

    // 이미지 파일 객체 생성
    private fun createEmptyImageFile(needsHidden: Boolean): File {
        val timeStamp = SimpleDateFormat(EtcConstant.FILE_NAME_FORMAT, Locale.KOREA).format(Date())
        val imageFileName = EtcConstant.FILE_NAME_HEADER_STAMP + timeStamp + EtcConstant.FILE_NAME_IMAGE_FORMAT
        val storageDir = FilePathManager.getStampDirectory(needsHidden)
        EzLogger.d("image file name : $imageFileName")
        EzLogger.d("storageDir : ${storageDir.path}")
        return File(storageDir, imageFileName)
    }

    private fun checkStampSizeValid(contentResolver: ContentResolver): Boolean {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, stampSourceUri)
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