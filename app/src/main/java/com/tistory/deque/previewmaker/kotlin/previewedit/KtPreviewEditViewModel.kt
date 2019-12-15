package com.tistory.deque.previewmaker.kotlin.previewedit

import android.app.AlertDialog
import androidx.lifecycle.LiveData
import android.content.Context
import android.net.Uri
import androidx.core.content.ContextCompat
import com.tistory.deque.previewmaker.R
import com.tistory.deque.previewmaker.kotlin.base.BaseKotlinViewModel
import com.tistory.deque.previewmaker.kotlin.db.KtDbOpenHelper
import com.tistory.deque.previewmaker.kotlin.manager.PreviewBitmapManager
import com.tistory.deque.previewmaker.kotlin.manager.PreviewEditButtonViewStateManager
import com.tistory.deque.previewmaker.kotlin.model.Preview
import com.tistory.deque.previewmaker.kotlin.model.PreviewAdapterModel
import com.tistory.deque.previewmaker.kotlin.model.PreviewLoader
import com.tistory.deque.previewmaker.kotlin.model.Stamp
import com.tistory.deque.previewmaker.kotlin.util.EtcConstant
import com.tistory.deque.previewmaker.kotlin.util.EzLogger
import com.tistory.deque.previewmaker.kotlin.util.SingleLiveEvent
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.model.AspectRatio
import com.yalantis.ucrop.view.CropImageView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class KtPreviewEditViewModel : BaseKotlinViewModel() {
    private val _startLoadingThumbnailEvent = SingleLiveEvent<Int>()
    val startLoadingThumbnailEvent: LiveData<Int> get() = _startLoadingThumbnailEvent

    private val _previewThumbnailAdapterNotifyDataSet = SingleLiveEvent<Any>()
    val previewThumbnailAdapterNotifyDataSet: LiveData<Any> get() = _previewThumbnailAdapterNotifyDataSet

    private val _loadingFinishEachThumbnailEvent = SingleLiveEvent<Unit>()
    val loadingFinishEachThumbnailEvent: LiveData<Unit> get() = _loadingFinishEachThumbnailEvent

    private val _finishLoadingThumbnailEvent = SingleLiveEvent<Unit>()
    val finishLoadingThumbnailEvent: LiveData<Unit> get() = _finishLoadingThumbnailEvent

    private val _startLoadingPreviewToCanvas = SingleLiveEvent<Any>()
    val startLoadingPreviewToCanvas: LiveData<Any> get() = _startLoadingPreviewToCanvas

    private val _finishLoadingPreviewToCanvas = SingleLiveEvent<Any>()
    val finishLoadingPreviewToCanvas: LiveData<Any> get() = _finishLoadingPreviewToCanvas

    private val _initCanvasAndPreviewEvent = SingleLiveEvent<Any>()
    val initCanvasAndPreviewEvent: LiveData<Any> get() = _initCanvasAndPreviewEvent

    private val _startLoadingPreviewToBlur = SingleLiveEvent<Any>()
    val startLoadingPreviewToBlur: LiveData<Any> get() = _startLoadingPreviewToBlur

    private val _finishLoadingPreviewToBlur = SingleLiveEvent<Any>()
    val finishLoadingPreviewToBlur: LiveData<Any> get() = _finishLoadingPreviewToBlur

    private val _startSavePreviewEvent = SingleLiveEvent<Any>()
    val startSavePreviewEvent: LiveData<Any> get() = _startSavePreviewEvent

    var previewAdapterModel: PreviewAdapterModel = PreviewAdapterModel()

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

    fun previewThumbnailClickListener(activity: KtPreviewEditActivity, preview: Preview, position: Int) {

        fun changePreview() {
            selectedPreviewPosition = position
            _startLoadingPreviewToCanvas.call()
            loadPreviewToCanvas(activity, preview)
            /*
            val loadingPreviewToCanvas = LoadingPreviewToCanvas(activity, preview)
            loadingPreviewToCanvas.execute()*/
        }

        if (selectedPreviewPosition == null || selectedPreviewPosition != position &&
                PreviewEditButtonViewStateManager.isHomeState()) { // 홈 스테이트 상태에서만 프리뷰 변경 가능

            selectedPreview?.let {
                if (!it.isSaved) {
                    AlertDialog.Builder(activity).apply {
                        setMessage(R.string.snackbar_preview_edit_acti_clk_new_preview)
                        setPositiveButton("YES") { _, _ -> _startSavePreviewEvent.call() }
                        setNegativeButton("NO") { _, _ ->
                            it.saved()
                            it.resetFilterValue()
                            changePreview()
                        }
                        setNeutralButton("CANCLE") { _, _ -> }
                    }.create().apply {
                        setCanceledOnTouchOutside(false)
                    }.show()
                } else {
                    changePreview()
                }
            } ?: run {
                changePreview() // 프리뷰가 null이란 말은 아무것도 클릭 안한 초기 상태라는 뜻.
            }
        }
    }

    fun refreshCanvas(context: Context) {
        loadPreviewToCanvas(context, selectedPreview ?: return)
    }

    fun deleteSelectedPreview(context: Context) {
        if (previewAdapterModel.size <= 1) return
        selectedPreviewPosition?.let {
            previewAdapterModel.delete(it)
            //_previewThumbnailAdapterRemovePosition.value = it
            _previewThumbnailAdapterNotifyDataSet.call()

            // 보여줄 프리뷰 포지션 변경
            selectedPreviewPosition = if (it == previewAdapterModel.size) { //맨 끝이면 이전꺼 선택
                it - 1
            } else {
                it
            }

            selectedPreview = previewAdapterModel.getPreview(selectedPreviewPosition ?: return)

            _startLoadingPreviewToCanvas.call()
            loadPreviewToCanvas(context, selectedPreview ?: return)
        }
    }

    fun cropSelectedPreviewEnd(resultUri: Uri, context: Context) {
        selectedPreview?.let {
            it.originalImageUri = resultUri
            it.saved()
            _startLoadingPreviewToCanvas.call()
            refreshCanvas(context)
        }
    }

    fun cropSelectedPreview(activity: KtPreviewEditActivity) {
        selectedPreview?.let {
            val options: UCrop.Options = setCropViewOption(activity)

            UCrop.of(it.originalImageUri, it.resultImageUri)
                    .withOptions(options)
                    .start(activity)

            EzLogger.d("ucrop start, originalImageUri : ${it.originalImageUri}, resultImageUri: ${it.resultImageUri}")
        }
    }

    private fun setCropViewOption(context: Context): UCrop.Options {
        val options = UCrop.Options()
        options.setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary))
        options.setActiveWidgetColor(ContextCompat.getColor(context, R.color.colorAccent))
        options.setToolbarWidgetColor(ContextCompat.getColor(context, R.color.black))
        options.setStatusBarColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
        options.setFreeStyleCropEnabled(true)

        options.setAspectRatioOptions(1,
                AspectRatio("16:9", 16f, 9f),
                AspectRatio("3:2", 3f, 2f),
                AspectRatio("ORIGINAL", CropImageView.DEFAULT_ASPECT_RATIO, CropImageView.DEFAULT_ASPECT_RATIO),
                AspectRatio("1:1", 1f, 1f),
                AspectRatio("2:3", 2f, 3f),
                AspectRatio("9:16", 9f, 16f)
        )

        return options
    }

    fun dbUpdateStamp(id: Int, stamp: Stamp) {
        dbOpenHelper?.dbUpdateStamp(id, stamp)
    }

    fun makeOvalBlur(canvasWidth: Int, canvasHeight: Int) {
        _startLoadingPreviewToBlur.call()
        blurPreviewBitmap(canvasWidth, canvasHeight)
    }

    fun showSaveEndSnackbar(fileName: String) {
        showSnackbar("저장 완료\n저장 경로:${EtcConstant.PREVIEW_SAVED_DIRECTORY}/$fileName")
    }

    fun loadPreviewThumbnail(applicationContext: Context, previewPathList: java.util.ArrayList<String>) {
        _startLoadingThumbnailEvent.value = previewPathList.size

        addDisposable(PreviewLoader.loadPreview(applicationContext, previewPathList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onNext = {
                            previewAdapterModel.addPreview(it)
                            _loadingFinishEachThumbnailEvent.call()
                            EzLogger.d("Thumbnail parsing success : $it")
                        },
                        onError = {
                            EzLogger.d("Load preview error : $it")
                        },
                        onComplete = {
                            _finishLoadingThumbnailEvent.call()
                        }
                )
        )
    }

    private fun loadPreviewToCanvas(applicationContext: Context, preview: Preview) {
        addDisposable(PreviewLoader.loadPreviewBitmap(applicationContext, preview, stamp)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onNext = { previewBitmap ->
                            PreviewBitmapManager.selectedPreviewBitmap = previewBitmap
                            selectedPreview = preview
                            _initCanvasAndPreviewEvent.call()
                            _finishLoadingPreviewToCanvas.call()
                        },
                        onError = {
                            EzLogger.d("Load preview to canvas error : $it")
                        }
                ))
    }

    private fun blurPreviewBitmap(canvasWidth: Int, canvasHeight: Int) {
        addDisposable(PreviewBitmapManager.blurringObservable(canvasWidth, canvasHeight)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onSuccess = {
                            _finishLoadingPreviewToBlur.call()
                        },
                        onError = {
                            EzLogger.d("Blur error : $it")
                        }
                ))
    }
}