package com.tistory.deque.previewmaker.kotlin.model

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.tistory.deque.previewmaker.Model_Global.SeekBarListener
import com.tistory.deque.previewmaker.kotlin.manager.PreviewBitmapManager
import com.tistory.deque.previewmaker.kotlin.util.EtcConstant
import com.tistory.deque.previewmaker.kotlin.util.EzLogger
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class Preview(
        var originalImageUri: Uri,
        var thumbnailImageUri: Uri,
        var resultImageUri: Uri,
        var isSaved: Boolean,

        var _brightness: Int,
        var _contrast: Int,
        var _kelvin: Int,
        var _saturation: Int) {

    companion object {
        private fun makeResultImageFile(): Uri {

            val timeStamp = SimpleDateFormat(EtcConstant.FILE_NAME_FORMAT, Locale.KOREA).format(Date())
            val imageFileName = EtcConstant.FILE_NAME_HEADER_PREVIEW + timeStamp + EtcConstant.FILE_NAME_IMAGE_FORMAT

            val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val storageDir = File(root, EtcConstant.PREVIEW_SAVED_DIRECTORY)
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }
            val imageFile = File(storageDir, imageFileName)
            val resultUri = Uri.fromFile(imageFile)

            EzLogger.d("storageDir : $storageDir")
            EzLogger.d("imageFile absolute path: ${imageFile.absolutePath}")
            EzLogger.d("image file name : $imageFileName")
            EzLogger.d("image file uri : $resultUri")

            return resultUri
        }
    }

    constructor(originalImageURI: Uri, thumbnailImageURI: Uri) :
            this(originalImageURI, thumbnailImageURI, makeResultImageFile(), true,
                    0, 0, 0, 0)

    fun getBitmap(context: Context) = PreviewBitmapManager.previewImageUriToBitmap(this.originalImageUri, context)

    var brightness: Int
        get() = _brightness + EtcConstant.SeekBarPreviewBrightnessMax / 2   //시크바에 들어갈 값이 리턴됨 (0~512) 실제 brightness 는 -255~+255
        set(value: Int) {
            //0~512를 인자로 받아서 -255~+255로 수정후 저장
            _brightness = value - EtcConstant.SeekBarPreviewBrightnessMax / 2
        }

    var contrast: Int
        get() = (_contrast.toFloat() / 512.0f + 1.0f).toInt() //실제 필터를 적용할 때 이용. 0.5~1.5를 리턴
        set(value: Int) {
            //0~512를 인자로 받아서 -255~+255로 수정후 저장
            _contrast = value - EtcConstant.SeekBarPreviewContrastMax / 2
        }

    var kelvin: Int
        get() = _kelvin + EtcConstant.SeekBarPreviewKelvinMax / 2
        set(value: Int) {
            _kelvin = value - EtcConstant.SeekBarPreviewKelvinMax / 2
        }

    var saturation: Int
        get() = (_saturation.toFloat() / 2048.0f + 1.0f).toInt()
        set(value: Int) {
            _saturation = value - EtcConstant.SeekBarPreviewSaturationMax / 2
        }

    fun saved() {
        isSaved = true
    }

    fun editted() {
        isSaved = false
    }

    fun getBrightnessForFilter(): Float {
        //실제 필터를 적용할 때 이용. -256~+256을 -64 ~ +64로 바꿔서 리턴
        return _brightness / 4.0f
    }

    fun getContrastForFilter(): Float {
        //실제 필터를 적용할 때 이용. 0.5~1.5를 리턴
        return _contrast.toFloat() / 512.0f + 1.0f
    }

    fun getSaturationForFilter(): Float {
        //0.875~1.125를 리턴
        return _saturation.toFloat() / 2048.0f + 1.0f
    }

    fun getKelvinForFilter(): Float {
        //0.875~1.125를 리턴
        return _kelvin.toFloat() / 1024.0f + 1.0f
    }

    fun resetFilterValue() {
        this._brightness = 0
        this._contrast = 0
        this._kelvin = 0
        this._saturation = 0
    }
}