package com.tistory.deque.previewmaker.kotlin.previewedit

import android.arch.lifecycle.LiveData
import android.content.Context
import android.database.CursorIndexOutOfBoundsException
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.media.ExifInterface
import android.net.Uri
import android.os.AsyncTask
import android.provider.MediaStore
import android.util.AttributeSet
import android.view.View
import com.tistory.deque.previewmaker.Controler.RetouchingPaintController
import com.tistory.deque.previewmaker.R
import com.tistory.deque.previewmaker.kotlin.base.BaseKotlinViewModel
import com.tistory.deque.previewmaker.kotlin.db.KtDbOpenHelper
import com.tistory.deque.previewmaker.kotlin.manager.BlurManager
import com.tistory.deque.previewmaker.kotlin.manager.PreviewBitmapManager
import com.tistory.deque.previewmaker.kotlin.manager.RetouachingPaintManager
import com.tistory.deque.previewmaker.kotlin.model.Preview
import com.tistory.deque.previewmaker.kotlin.model.PreviewListModel
import com.tistory.deque.previewmaker.kotlin.model.Stamp
import com.tistory.deque.previewmaker.kotlin.util.EzLogger
import com.tistory.deque.previewmaker.kotlin.util.SingleLiveEvent
import com.tistory.deque.previewmaker.kotlin.util.extension.getUri
import java.io.File
import kotlin.math.roundToInt

class KtPreviewEditViewModel : BaseKotlinViewModel() {
    private val _startLoadingThumbnailEvent = SingleLiveEvent<Int>()
    val startLoadingThumbnailEvent: LiveData<Int> get() = _startLoadingThumbnailEvent

    private val _previewThumbnailAdapterNotifyDataSet = SingleLiveEvent<Any>()
    val previewThumbnailAdapterNotifyDataSet: LiveData<Any> get() = _previewThumbnailAdapterNotifyDataSet

    private val _loadingFinishEachThumbnailEvent = SingleLiveEvent<Int>()
    val loadingFinishEachThumbnailEvent: LiveData<Int> get() = _loadingFinishEachThumbnailEvent

    private val _finishLoadingThumbnailEvent = SingleLiveEvent<Int>()
    val finishLoadingThumbnailEvent: LiveData<Int> get() = _finishLoadingThumbnailEvent

    private val _startLoadingPreviewToCanvas = SingleLiveEvent<Any>()
    val startLoadingPreviewToCanvas: LiveData<Any> get() = _startLoadingPreviewToCanvas

    private val _finishLoadingPreviewToCanvas = SingleLiveEvent<Any>()
    val finishLoadingPreviewToCanvas: LiveData<Any> get() = _finishLoadingPreviewToCanvas

    private val _startLoadingPreviewToBlur = SingleLiveEvent<Any>()
    val startLoadingPreviewToBlur: LiveData<Any> get() = _startLoadingPreviewToBlur

    private val _finishLoadingPreviewToBlur = SingleLiveEvent<Any>()
    val finishLoadingPreviewToBlur: LiveData<Any> get() = _finishLoadingPreviewToBlur

    var previewListModel: PreviewListModel = PreviewListModel()
    private val previewListSize: Int
        get() = previewPathList.size

    var previewPathList = ArrayList<String>()

    var selectedPreview: Preview? = null
    var selectedPreviewPosition: Int? = null
    var stamp: Stamp? = null

    var dbOpenHelper: KtDbOpenHelper? = null

    fun dbOpen(context: Context) {
        EzLogger.d("main activity : db open")
        dbOpenHelper = KtDbOpenHelper.getDbOpenHelper(context,
                KtDbOpenHelper.DP_OPEN_HELPER_NAME,
                null,
                KtDbOpenHelper.dbVersion)
        dbOpenHelper?.dbOpen() ?: EzLogger.d("db open fail : dbOpenHelper null")
    }

    fun getStamp(stampId: Int, context: Context) {
        dbOpenHelper?.let {
            stamp = it.dbGetStamp(stampId)
            stamp?.let { stamp ->
                EzLogger.d("stamp found : $stamp")
            } ?: run {
                showSnackbar(R.string.snackbar_stamp_not_found)
            }
        }
    }

    fun makePreviewThumbnail(context: Context, previewPathList: ArrayList<String>) {
        EzLogger.d("makePreviewThumbnail")

        this.previewPathList = previewPathList

        _startLoadingThumbnailEvent.value = previewListSize
        val addPreviewThumbnailAsyncTask = AddPreviewThumbnailAsyncTask(context)
        addPreviewThumbnailAsyncTask.execute()

    }

    private fun thumbnailUriFromOriginalUri(context: Context, imagePath: String): Uri? {
        EzLogger.d("imagePath : $imagePath")

        val selectedImageUri = imagePath.getUri(context.contentResolver) ?: return null
        val rowId = (selectedImageUri.lastPathSegment) ?: return null
        val rowIdLong: Long = rowId.toLongOrNull() ?: return null

        EzLogger.d("original uri : $selectedImageUri , row ID : $rowIdLong")

        return imageIdToThumbnail(context, rowIdLong)
    }

    private fun imageIdToThumbnail(context: Context, imageId: Long): Uri? {
        val projection = arrayOf(MediaStore.Images.Thumbnails.DATA)
        val contentResolver = context.contentResolver

        try {
            contentResolver.query(
                    MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                    projection,
                    MediaStore.Images.Thumbnails.IMAGE_ID + "=?",
                    arrayOf(imageId.toString()),
                    null)
                    ?.use { thumbnailCursor ->
                        return if (thumbnailCursor.moveToFirst()) {
                            Uri.parse(thumbnailCursor.getString(thumbnailCursor.getColumnIndex(projection[0])))
                        } else {
                            MediaStore.Images.Thumbnails.getThumbnail(contentResolver, imageId, MediaStore.Images.Thumbnails.MINI_KIND, null)
                            EzLogger.d("No exist thumbnail, so make it")
                            imageIdToThumbnail(context, imageId)
                        }
                    } ?: return null
        } catch (e: CursorIndexOutOfBoundsException) {
            return null
        }
    }

    private fun loadingFinishEachThumbnail(position: Int) {
        _loadingFinishEachThumbnailEvent.value = position
    }

    private fun loadingFinishAllThumbnail(allThumbnailSize: Int) {
        _finishLoadingThumbnailEvent.value = allThumbnailSize
    }

    private fun finishLoadingPreviewToCanvas(preview: Preview) {
        selectedPreview = preview
        _finishLoadingPreviewToCanvas.call()
    }

    fun previewThumbnailClickListener(context: Context, preview: Preview, position: Int) {
        if (selectedPreviewPosition == null || selectedPreviewPosition != position) {
            selectedPreviewPosition = position
            _startLoadingPreviewToCanvas.call()
            val loadingPreviewToCanvas = LoadingPreviewToCanvas(context, preview)
            loadingPreviewToCanvas.execute()
        }
    }

    fun refreshCanvas(context: Context) {
        val loadingPreviewToCanvas = LoadingPreviewToCanvas(context, selectedPreview ?: return)
        loadingPreviewToCanvas.execute()
    }

    fun deleteSelectedPreview(context: Context) {
        if (previewListModel.size <= 1) return
        selectedPreviewPosition?.let {
            previewListModel.delete(it)
            previewPathList.removeAt(it)
            //_previewThumbnailAdapterRemovePosition.value = it
            _previewThumbnailAdapterNotifyDataSet.call()

            // 보여줄 프리뷰 포지션 변경
            selectedPreviewPosition = if (it == previewListModel.size) { //맨 끝이면 이전꺼 선택
                it - 1
            } else {
                it
            }

            selectedPreview = previewListModel.getPreview(selectedPreviewPosition ?: return)

            _startLoadingPreviewToCanvas.call()
            val loadingPreviewToCanvas = LoadingPreviewToCanvas(context, selectedPreview ?: return)
            loadingPreviewToCanvas.execute()
        }
    }

    fun dbUpdateStamp(id: Int, stamp: Stamp) {
        dbOpenHelper?.dbUpdateStamp(id, stamp)
    }

    fun makeOvalBlur(canvasWidth: Int, canvasHeight: Int) {
        _startLoadingPreviewToBlur.call()
        MakeBlurAsyncTask(canvasWidth, canvasHeight).execute()
    }

    fun finishBlur() {
        _finishLoadingPreviewToBlur.call()
    }

    inner class AddPreviewThumbnailAsyncTask(val context: Context) : AsyncTask<Void, Int, Int>() {

        private var loadingCounter = 0

        override fun onPreExecute() {
            super.onPreExecute()
            previewListModel.initPreviewList()
            _previewThumbnailAdapterNotifyDataSet.call()
        }

        override fun doInBackground(vararg params: Void?): Int {
            previewPathList.forEach { previewPath ->
                EzLogger.d("doInBackground... preview path : $previewPath, make thumbnailUri...")
                val originalUri: Uri = Uri.fromFile(File(previewPath))
                val thumbnailUri: Uri = thumbnailUriFromOriginalUri(context, previewPath)
                        ?: originalUri

                EzLogger.d("Thumbnail parsing success : $thumbnailUri")

                val rotation = ExifInterface(previewPath)
                        .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)

                val preview = Preview(originalUri, thumbnailUri, rotation)
                previewListModel.addPreview(preview)

                publishProgress(loadingCounter)
                loadingCounter++
            }
            return loadingCounter
        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            values[0]?.let {
                loadingFinishEachThumbnail(it)
            }
        }

        override fun onPostExecute(allThumbnailSize: Int) {
            super.onPostExecute(allThumbnailSize)
            loadingFinishAllThumbnail(allThumbnailSize)
        }

    }

    inner class LoadingPreviewToCanvas(val context: Context, val preview: Preview) : AsyncTask<Void, Void, Preview>() {
        override fun doInBackground(vararg params: Void?): Preview {
            EzLogger.d("LoadingPreviewToCanvas doInBackground")
            PreviewBitmapManager.selectedPreviewBitmap = preview.getBitmap(context)
            if (PreviewBitmapManager.selectedStampBitmap == null) {
                stamp?.imageUri?.let { url ->
                    PreviewBitmapManager.selectedStampBitmap =
                            PreviewBitmapManager.stampImageUriToBitmap(url, context)
                }
            }
            return preview
        }

        override fun onPostExecute(preview: Preview) {
            super.onPostExecute(preview)
            finishLoadingPreviewToCanvas(preview)
        }
    }

    inner class MakeBlurAsyncTask(val canvasWidth: Int, val canvasHeight: Int) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            val partOvalElements = BlurManager.resizedBlurOvalToOriginalBlurOval(canvasWidth, canvasHeight)
            PreviewBitmapManager.blurBitmap(partOvalElements)
            return null
        }

        override fun onPostExecute(result: Void?) {
            finishBlur()
        }
    }

}