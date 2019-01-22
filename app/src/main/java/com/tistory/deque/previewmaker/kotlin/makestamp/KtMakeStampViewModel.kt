package com.tistory.deque.previewmaker.kotlin.makestamp

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.tistory.deque.previewmaker.R
import com.tistory.deque.previewmaker.kotlin.base.BaseKotlinViewModel
import com.tistory.deque.previewmaker.kotlin.util.EzLogger
import com.tistory.deque.previewmaker.kotlin.util.SingleLiveEvent
import com.tistory.deque.previewmaker.kotlin.util.extension.getRealPath
import kotlinx.coroutines.*
import java.io.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class KtMakeStampViewModel : BaseKotlinViewModel() {

    companion object {
        const val FILE_NAME_FORMAT = "yyyyMMddHHmmssSSS"
        const val FILE_NAME_HEADER_STAMP = "STAMP_"
        const val FILE_NAME_HEADER_PREVIEW = "PREVIEW_"
        const val FILE_NAME_IMAGE_FORMAT = ".png"

        const val MAIN_DIRECTORY = "Pictures"
        const val PREVIEW_SAVED_DIRECTORY = "Preview" + " " + "Maker"
        const val STAMP_SAVED_DIRECTORY = "Stamp"
    }

    private val _galleryAddPicEvent = SingleLiveEvent<Uri>()
    val galleryAddPicEvent: LiveData<Uri> get() = _galleryAddPicEvent

    private val _startProgressiveEvent = SingleLiveEvent<Any>()
    val startProgressiveEvent: LiveData<Any> get() = _startProgressiveEvent

    private val _endProgressiveEvent = SingleLiveEvent<Any>()
    val endProgressiveEvent: LiveData<Any> get() = _endProgressiveEvent

    private val _stampUriLiveData = MutableLiveData<Uri>()
    val stampUriLiveData: LiveData<Uri> get() = _stampUriLiveData

    private val _finishActivityWithStampNameEvent = SingleLiveEvent<String>()
    val finishActivityWithStampNameEvent: LiveData<String> get() = _finishActivityWithStampNameEvent

    fun setImageView(context: Context, data: Intent) {
        //_stampUriLiveData.value = data.data

        //start progressive

        _startProgressiveEvent.call()
        data.data?.let { sourceUri ->
            //GlobalScope.launch {
                makeStampFile(context, sourceUri) ?.let { outFile ->
                    _stampUriLiveData.value = Uri.fromFile(outFile)
                } ?: showSnackbar("낙관 생성 실패. 다시 시도해주세요")
                _endProgressiveEvent.call()
            //}
        }

    }

    fun checkName(name: String) {
        when {
            name.isEmpty() -> showSnackbar(R.string.snackbar_make_stamp_acti_no_name_warn)
            name.length > 10 -> showSnackbar(R.string.snackbar_make_stamp_acti_name_len_warn)
            else -> _finishActivityWithStampNameEvent.value = name
        }
    }

    private fun makeStampFile(context: Context, sourceUri: Uri): File? {
        if(checkStampSizeValid(context.contentResolver, sourceUri)) {
            val outFile = createImageFile()
            EzLogger.d("outFile path : file.absolutePath -> ${outFile.absolutePath}")
            val sourceFile = File(sourceUri.getRealPath(context.contentResolver) ?: return null)
            EzLogger.d("sourceFile uri.getRealPath() : ${sourceUri.getRealPath(context.contentResolver)}")
            copyAndPasteImage(sourceFile, outFile)
            return outFile
        }
        return null
    }

    private fun copyAndPasteImage(sourceFile: File, outFile: File) {
        try {
            EzLogger.d("copy and paste iamge : sourcefile : $sourceFile, outfile : $outFile")
            sourceFile.copyTo(outFile, true, 1024)
        } catch (e:IOException) {
            EzLogger.d("File copy fail")
            e.printStackTrace()
            showSnackbar(R.string.snackbar_main_acti_stamp_copy_err)
            return
        } catch (e:Exception) {
            when(e){
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


    //sourceFile의 이미지를 outFile로 붙여넣음
    private fun copyAndPasteImage2(sourceFile: File, outFile: File) {
        EzLogger.d("copy and paste iamge : sourcefile : $sourceFile, outfile : $outFile")
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
        _galleryAddPicEvent.value = Uri.fromFile(outFile)
    }

    // 이미지 파일 객체 생성
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