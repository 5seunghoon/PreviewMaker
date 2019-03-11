package com.tistory.deque.previewmaker.kotlin.model

import android.content.Context
import android.graphics.Bitmap
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import com.tistory.deque.previewmaker.kotlin.manager.PreviewBitmapManager
import com.tistory.deque.previewmaker.kotlin.util.EtcConstant
import com.tistory.deque.previewmaker.kotlin.util.EzLogger
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


data class Preview(
        var originalImageUri: Uri, // 원래 이미지의 Uri. 그런데 저장이나 crop을 하면 바뀐 저장이 된 파일의 uri로 바뀜
        var thumbnailImageUri: Uri,
        var resultImageUri: Uri, // 저장 할 이미지의 Uri. 저장이나 crop을 최소 한번이라도 하면 쓸모 없어짐. 즉, 최초 저장할때만 쓸모있음.
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

    constructor(originalImageURI: Uri, thumbnailImageURI: Uri, rotation: Int) :
            this(originalImageURI, thumbnailImageURI, makeResultImageFile(), true,
                    0, 0, 0, 0) {
        this.rotation = rotation
    }

    fun getBitmap(context: Context): Bitmap? {
        val path = originalImageUri.path
        EzLogger.d("Preview.kt, getBitmap(), originalImageUri : $originalImageUri, path : $path")
        val rotation = ExifInterface(path)
                .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
        return PreviewBitmapManager.previewImageUriToBitmap(this.originalImageUri, context, rotation)
    }

    var rotation: Int? = null

    var brightness: Int
        get() = _brightness + EtcConstant.SeekBarPreviewBrightnessMax / 2   //시크바에 들어갈 값이 리턴됨 (0~512) 실제 brightness 는 -255~+255
        set(value: Int) {
            //0~512를 인자로 받아서 -255~+255로 수정후 저장
            _brightness = value - EtcConstant.SeekBarPreviewBrightnessMax / 2
        }

    var contrast: Int
        get() = _contrast + EtcConstant.SeekBarPreviewContrastMax / 2
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
        get() = _saturation + EtcConstant.SeekBarPreviewSaturationMax / 2
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
        return if(_brightness == 0) 0f else _brightness / 4.0f
    }

    fun getContrastForFilter(): Float {
        //실제 필터를 적용할 때 이용. 0.5~1.5를 리턴
        return if(_contrast == 0) 1f else _contrast.toFloat() / 512.0f + 1.0f
    }

    fun getSaturationForFilter(): Float {
        //0.875~1.125를 리턴
        return if(_saturation == 0) 1f else _saturation.toFloat() / 2048.0f + 1.0f
    }

    fun getKelvinForFilter(): Float {
        //0.875~1.125를 리턴
        return if(_kelvin == 0) 1f else _kelvin.toFloat() / 1024.0f + 1.0f
    }

    fun resetFilterValue() {
        this._brightness = 0
        this._contrast = 0
        this._kelvin = 0
        this._saturation = 0
    }

}