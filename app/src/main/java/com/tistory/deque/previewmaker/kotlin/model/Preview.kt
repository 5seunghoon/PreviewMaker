package com.tistory.deque.previewmaker.kotlin.model

import android.content.Context
import android.net.Uri
import android.os.Environment
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

        var brightness: Int,
        var contrast: Int,
        var kelvin: Int,
        var saturation: Int){

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
                    0,0,0,0)

    fun getBitmap(context:Context) = PreviewBitmapManager.imageUriToBitmap(this.originalImageUri, context)

    fun saved() {
        isSaved = true
    }

    fun editted() {
        isSaved = false
    }
}