package com.tistory.deque.previewmaker.kotlin.previewedit

import androidx.lifecycle.Observer
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import com.google.android.material.snackbar.Snackbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.tistory.deque.previewmaker.R
import com.tistory.deque.previewmaker.kotlin.base.BaseKotlinActivity
import com.tistory.deque.previewmaker.kotlin.helppreviewedit.KtHelpPreviewEditActivity
import com.tistory.deque.previewmaker.kotlin.manager.BlurManager
import com.tistory.deque.previewmaker.kotlin.manager.PreviewBitmapManager
import com.tistory.deque.previewmaker.kotlin.manager.PreviewEditClickStateManager
import com.tistory.deque.previewmaker.kotlin.manager.SharedPreferencesManager
import com.tistory.deque.previewmaker.kotlin.model.Preview
import com.tistory.deque.previewmaker.kotlin.model.Stamp
import com.tistory.deque.previewmaker.kotlin.util.EtcConstant
import com.tistory.deque.previewmaker.kotlin.util.EzLogger
import com.tistory.deque.previewmaker.kotlin.util.extension.fadeIn
import com.tistory.deque.previewmaker.kotlin.util.extension.fadeOut
import kotlinx.android.synthetic.main.activity_kt_preview_edit.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import com.yalantis.ucrop.UCrop



class KtPreviewEditActivity : BaseKotlinActivity<KtPreviewEditViewModel>() {

    override val layoutResourceId: Int
        get() = R.layout.activity_kt_preview_edit
    override val viewModel: KtPreviewEditViewModel by viewModel()

    var stampId: Int = -1
    var stampImageUri: Uri? = null
    var previewPathList: ArrayList<String> = ArrayList()

    private val previewThumbnailAdapter: PreviewThumbnailAdapter by inject()

    private var backPressedTime: Long = 0

    override fun initViewStart() {
        setBackButtonAboveActionBar(true, "프리뷰 편집")
        preview_edit_loading_progress_bar_layout.run { post { visibility = View.GONE } }
        viewModel.dbOpen(applicationContext)
        addComponentFromIntent()
        setPreviewThumbnailRecyclerView()
        setCustomPreviewCanvas()
        setCustomEditGroup()
    }

    private fun addComponentFromIntent() {
        intent.run {
            stampId = getIntExtra(EtcConstant.STAMP_ID_INTENT_KEY, -1)
            stampImageUri = data
            previewPathList = getStringArrayListExtra(EtcConstant.PREVIEW_LIST_INTENT_KEY)
        }
        viewModel.getStamp(stampId, applicationContext)
    }

    private fun setPreviewThumbnailRecyclerView() {
        preview_edit_thumbnail_recycler_view.run {
            adapter = previewThumbnailAdapter.apply {
                previewAdapterModel = viewModel.previewAdapterModel
                previewThumbnailClickListener = this@KtPreviewEditActivity::previewThumbnailClickListener
                previewThumbnailHelpClickListener = this@KtPreviewEditActivity::previewThumbnailHelpClickListener
            }
            layoutManager = LinearLayoutManager(applicationContext, RecyclerView.HORIZONTAL, false)
            setHasFixedSize(true)
        }
    }

    private fun setCustomPreviewCanvas() {
        preview_edit_custom_preview_canvas.run {
            setComponent(this@KtPreviewEditActivity)
        }
    }

    private fun setCustomEditGroup() {
        preview_edit_custom_edit_group.customPreviewCanvas = preview_edit_custom_preview_canvas
    }

    private fun previewThumbnailHelpClickListener() {
        startActivity(Intent(applicationContext, KtHelpPreviewEditActivity::class.java))
    }

    private fun previewThumbnailClickListener(preview: Preview, position: Int) {
        viewModel.previewThumbnailClickListener(this, preview, position)
    }

    override fun initDataBinding() {
        viewModel.startLoadingThumbnailEvent.observe(this, Observer { size ->
            preview_edit_thumbnail_loading_progress_bar.run {
                post {
                    visibility = View.VISIBLE
                    max = size ?: EtcConstant.MAX_SELECT_IMAGE_ACCOUNT
                }
            }
        })
        viewModel.loadingFinishEachThumbnailEvent.observe(this, Observer {
            EzLogger.d("loadingFinishEachThumbnailEvent observe")
            previewThumbnailAdapter.notifyDataSetChanged()
            preview_edit_thumbnail_loading_progress_bar.run { post { ++progress } }
        })
        viewModel.finishLoadingThumbnailEvent.observe(this, Observer {
            previewThumbnailAdapter.notifyDataSetChanged()
            preview_edit_thumbnail_loading_progress_bar.run { post { visibility = View.GONE } }
        })
        viewModel.startLoadingPreviewToCanvas.observe(this, Observer {
            preview_edit_hint_text_view.run { post { visibility = View.GONE } }
            mainLoadingProgressBarStart()
        })
        viewModel.finishLoadingPreviewToCanvas.observe(this, Observer {
            preview_edit_custom_preview_canvas.run { post { invalidate() } }
            mainLoadingProgressBarStop()
        })
        viewModel.initCanvasAndPreviewEvent.observe(this, Observer {
            preview_edit_custom_preview_canvas.initCanvasAndPreview()
        })
        viewModel.startLoadingPreviewToBlur.observe(this, Observer {
            preview_edit_custom_preview_canvas.isBlurRoutine = true
            mainLoadingProgressBarStart()
        })
        viewModel.finishLoadingPreviewToBlur.observe(this, Observer {
            preview_edit_custom_preview_canvas.isBlurRoutine = false
            preview_edit_custom_preview_canvas.run { post { invalidate() } }
            mainLoadingProgressBarStop()
        })
        viewModel.previewThumbnailAdapterNotifyDataSet.observe(this, Observer {
            previewThumbnailAdapter.notifyDataSetChanged()
        })
        viewModel.startSavePreviewEvent.observe(this, Observer {
            preview_edit_custom_preview_canvas.saveStart()
        })
    }

    override fun initViewFinal() {
        viewModel.loadPreviewThumbnail(applicationContext, previewPathList)
    }

    override fun onBackPressed() {
        if (System.currentTimeMillis() - backPressedTime < 2000) {
            finish()
        } else {
            Snackbar.make(findViewById(android.R.id.content),
                    getString(R.string.snackbar_preview_edit_acti_back_to_exit),
                    Snackbar.LENGTH_LONG)
                    .show()
            backPressedTime = System.currentTimeMillis()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            data?.let {
                val resultUri = UCrop.getOutput(it) ?: return@let

                val cropAspectRatio = UCrop.getOutputCropAspectRatio(it)
                if (cropAspectRatio > 0f) {
                    SharedPreferencesManager.setPreviewWidthOverHeightRatio(applicationContext, cropAspectRatio)
                }

                val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                mediaScanIntent.data = resultUri
                sendBroadcast(mediaScanIntent)

                viewModel.cropSelectedPreviewEnd(resultUri, this)
            }
            EzLogger.d("cropFail : data null")
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data ?: return)
            EzLogger.d("cropError : $cropError")
        } else {
            EzLogger.d("cropCancel")
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun mainLoadingProgressBarStart() {
        preview_edit_loading_progress_bar_layout.fadeIn()
    }

    private fun mainLoadingProgressBarStop() {
        preview_edit_loading_progress_bar_layout.fadeOut()
    }

    override fun onDestroy() {
        PreviewEditClickStateManager.initState()
        PreviewBitmapManager.resetManager()
        BlurManager.resetManager()
        viewModel.reset()
        super.onDestroy()
    }

    fun setSyncClickState() {
        preview_edit_custom_edit_group.setSyncClickState()
    }

    fun stampUpdate(id: Int, stamp: Stamp) {
        viewModel.dbUpdateStamp(id, stamp)
    }

    fun makeOvalBlur(canvasWidth: Int, canvasHeight: Int) {
        viewModel.makeOvalBlur(canvasWidth, canvasHeight)
    }

    fun savePreviewStart() {
        mainLoadingProgressBarStart()
    }

    fun savePreviewEnd(fileName: String) {
        viewModel.refreshCanvas(this)
        viewModel.showSaveEndSnackbar(this, fileName)
    }

    fun deleteSelectedPreview() {
        viewModel.deleteSelectedPreview(this)
    }

    fun cropSelectedPreview() {
        viewModel.cropSelectedPreview(this)
    }
}
