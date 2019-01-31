package com.tistory.deque.previewmaker.kotlin.previewedit

import android.arch.lifecycle.Observer
import android.net.Uri
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.tistory.deque.previewmaker.R
import com.tistory.deque.previewmaker.kotlin.base.BaseKotlinActivity
import com.tistory.deque.previewmaker.kotlin.util.EtcConstant
import kotlinx.android.synthetic.main.activity_kt_preview_edit.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class KtPreviewEditActivity : BaseKotlinActivity<KtPreviewEditViewModel>() {

    override val layoutResourceId: Int
        get() = R.layout.activity_kt_preview_edit
    override val viewModel: KtPreviewEditViewModel by viewModel()

    var stampId: Int = -1
    var stampImageUri: Uri? = null
    var previewPathList: ArrayList<String> = ArrayList()

    private val previewThumbnailAdapter: PreviewThumbnailAdapter by inject()

    override fun initViewStart() {
        setBackButtonAboveActionBar(true, "프리뷰 편집")
        setPreviewThumbnailRecyclerView()
        addComponentFromIntent()
    }

    private fun addComponentFromIntent() {
        intent.run {
            stampId = getIntExtra(EtcConstant.STAMP_ID_INTENT_KEY, -1)
            stampImageUri = data
            previewPathList = getStringArrayListExtra(EtcConstant.PREVIEW_LIST_INTENT_KEY)
        }
    }

    private fun setPreviewThumbnailRecyclerView() {
        preview_edit_thumbnail_recycler_view.run {
            adapter = previewThumbnailAdapter.apply {
                previewListModel = viewModel.previewListModel
            }
            layoutManager = LinearLayoutManager(applicationContext, RecyclerView.HORIZONTAL, false)
            setHasFixedSize(true)
        }
    }

    override fun initDataBinding() {
        viewModel.addPreviewThumbnailEvent.observe(this, Observer {preview ->
            preview?.let {
                previewThumbnailAdapter.notifyDataSetChanged()
            }
        })
    }

    override fun initViewFinal() {
        viewModel.makePreviewThumbnail(applicationContext, previewPathList)
    }
}
