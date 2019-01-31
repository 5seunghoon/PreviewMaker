package com.tistory.deque.previewmaker.kotlin.previewedit

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.tistory.deque.previewmaker.R
import com.tistory.deque.previewmaker.kotlin.base.BaseKotlinActivity
import kotlinx.android.synthetic.main.activity_kt_preview_edit.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class KtPreviewEditActivity : BaseKotlinActivity<KtPreviewEditViewModel>() {
    override val layoutResourceId: Int
        get() = R.layout.activity_kt_preview_edit
    override val viewModel: KtPreviewEditViewModel by viewModel()

    private val previewThumbnailAdapter: PreviewThumbnailAdapter by inject()

    override fun initViewStart() {
        setBackButtonAboveActionBar(true, "프리뷰 편집")
        setPreviewThumbnailRecycerView()
    }

    private fun setPreviewThumbnailRecycerView() {
        preview_edit_thumbnail_recycler_view.run {

        }
    }

    override fun initDataBinding() {
        return
    }

    override fun initViewFinal() {
        return
    }
}
