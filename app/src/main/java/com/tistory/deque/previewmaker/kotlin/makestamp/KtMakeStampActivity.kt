package com.tistory.deque.previewmaker.kotlin.makestamp

import android.app.Activity
import android.arch.lifecycle.Observer
import android.content.Context
import android.net.Uri
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.tistory.deque.previewmaker.R
import com.tistory.deque.previewmaker.kotlin.base.BaseKotlinActivity
import com.tistory.deque.previewmaker.kotlin.util.EzLogger
import com.tistory.deque.previewmaker.kotlin.util.extension.galleryAddPic
import kotlinx.android.synthetic.main.activity_kt_make_stamp.*
import org.koin.android.viewmodel.ext.android.viewModel
import java.io.File

class KtMakeStampActivity : BaseKotlinActivity<KtMakeStampViewModel>() {
    companion object {
        const val STAMP_NAME_INTENT_KEY = "STAMP_NAME_INTENT_KEY"
    }

    override val layoutResourceId: Int
        get() = R.layout.activity_kt_make_stamp
    override val viewModel: KtMakeStampViewModel by viewModel()

    override fun initViewStart() {
        setBackButtonAboveActionBar(true, "낙관 이름 설정")
    }

    override fun initDataBinding() {
        viewModel.stampUriLiveData.observe(this, Observer { uri ->
            uri?.let {
                make_stamp_image_view.run { post { setImageURI(it) } }
                make_stamp_progress_bar.run { post { visibility = View.GONE } }
            }
        })
        viewModel.finishActivityWithStampNameEvent.observe(this, Observer {name ->
            name?.let {
                intent.run {
                    data = viewModel.stampUriLiveData.value
                    putExtra(STAMP_NAME_INTENT_KEY, it)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
        })
        viewModel.startProgressiveEvent.observe(this, Observer {
            make_stamp_progress_bar.run { post { visibility = View.VISIBLE } }
        })
        viewModel.endProgressiveEvent.observe(this, Observer {
            make_stamp_progress_bar.run { post { visibility = View.GONE } }
        })
        viewModel.galleryAddPicEvent.observe(this, Observer { uri ->
            EzLogger.d("galleryAddPic observe, uri : $uri")
            uri?.let { galleryAddPic(it) }
        })
    }

    override fun initViewFinal() {
        viewModel.setImageView(applicationContext, intent)

        make_stamp_submit_button.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(make_stamp_name_edit_text.windowToken, 0)
            viewModel.checkName(make_stamp_name_edit_text.text.toString())
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                //뒤로가기 버튼
                cancelAndFinish()
                return true
            }
            else -> return false
        }
    }

    private fun cancelAndFinish() {
        viewModel.stampUriLiveData.value?.let {
            deleteFile(it)
            setResult(RESULT_CANCELED, intent)
            finish()
        }
    }

    private fun deleteFile(uri: Uri) {
        val file = File(uri.path!!)
        if (file.delete()) {
            galleryAddPic(uri)
            EzLogger.d("stamp delete success uri : $uri")
        } else {
            EzLogger.d("Stamp delete fail uri : $uri")
        }
    }

}
