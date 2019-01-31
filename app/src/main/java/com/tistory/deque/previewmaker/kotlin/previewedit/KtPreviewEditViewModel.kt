package com.tistory.deque.previewmaker.kotlin.previewedit

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.database.CursorIndexOutOfBoundsException
import android.net.Uri
import android.os.AsyncTask
import android.provider.MediaStore
import com.tistory.deque.previewmaker.Util.Logger
import com.tistory.deque.previewmaker.kotlin.base.BaseKotlinViewModel
import com.tistory.deque.previewmaker.kotlin.model.Preview
import com.tistory.deque.previewmaker.kotlin.model.PreviewListModel
import com.tistory.deque.previewmaker.kotlin.util.EzLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class KtPreviewEditViewModel : BaseKotlinViewModel() {
    val _addPreviewThumbnailEvent = MutableLiveData<Preview>()
    val addPreviewThumbnailEvent: LiveData<Preview> get() = _addPreviewThumbnailEvent

    var previewListModel: PreviewListModel = PreviewListModel()

    var previewPathList = ArrayList<String>()

    fun makePreviewThumbnail(context: Context, previewPathList: ArrayList<String>) {
        EzLogger.d("makePreviewThumbnail")

        this.previewPathList = previewPathList

        val addPreviewThumbnailAsyncTask = AddPreviewThumbnailAsyncTask(context)
        addPreviewThumbnailAsyncTask.execute()

    }

    private fun thumbnailUriFromOriginalUri(context: Context, imagePath: String): Uri? {
        EzLogger.d("imagePath : $imagePath")

        val selectedImageUri = Uri.fromFile(File(imagePath))
        val rowId = (selectedImageUri.lastPathSegment) ?: return null

        EzLogger.d("original uri : $selectedImageUri , row ID : $rowId")

        return imageIdToThumbnail(context, rowId)
    }

    private fun imageIdToThumbnail(context: Context, imageId: String): Uri? {
        val projection = arrayOf(MediaStore.Images.Thumbnails.DATA)
        val contentResolver = context.contentResolver

        try {
            contentResolver.query(
                    MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                    projection,
                    MediaStore.Images.Thumbnails.IMAGE_ID + "=?",
                    arrayOf(imageId),
                    null)
                    ?.use { thumbnailCursor ->
                        return if (thumbnailCursor.moveToFirst()) {
                            Uri.parse(thumbnailCursor.getString(thumbnailCursor.getColumnIndex(projection[0])))
                        } else {
                            MediaStore.Images.Thumbnails.getThumbnail(contentResolver, imageId.toLong(), MediaStore.Images.Thumbnails.MINI_KIND, null)
                            EzLogger.d("No exist thumbnail, so make it")
                            imageIdToThumbnail(context, imageId)
                        }
                    } ?: return null
        } catch (e: CursorIndexOutOfBoundsException) {
            return null
        }
    }

    inner class AddPreviewThumbnailAsyncTask(val context: Context): AsyncTask<Void, Int, Boolean>() {

        private var loadingCounter = 0

        override fun doInBackground(vararg params: Void?): Boolean {
            previewPathList.forEach { previewPath ->
                EzLogger.d("doInBackground... preview path : $previewPath, make thumbnailUri...")
                val originalUri: Uri = Uri.fromFile(File(previewPath))
                val thumbnailUri:Uri = thumbnailUriFromOriginalUri(context, previewPath) ?: originalUri

                EzLogger.d( "Thumbnail parsing success : $thumbnailUri")

                val preview = Preview(originalUri, thumbnailUri)
                previewListModel.addPreview(preview)

                loadingCounter ++

                publishProgress()
            }
            return true
        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            _addPreviewThumbnailEvent.postValue(previewListModel.getPreview(loadingCounter - 1))
        }

    }

}