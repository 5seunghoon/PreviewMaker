package com.tistory.deque.previewmaker.kotlin.makestamp

import android.app.Activity
import androidx.lifecycle.Observer
import android.content.Context
import android.net.Uri
import com.google.android.material.snackbar.Snackbar
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import com.tistory.deque.previewmaker.R
import com.tistory.deque.previewmaker.kotlin.base.BaseKotlinActivity
import com.tistory.deque.previewmaker.kotlin.manager.SharedPreferencesManager
import com.tistory.deque.previewmaker.kotlin.util.EtcConstant
import com.tistory.deque.previewmaker.kotlin.util.EzLogger
import com.tistory.deque.previewmaker.kotlin.util.extension.galleryAddPic
import kotlinx.android.synthetic.main.activity_kt_make_stamp.*
import org.koin.android.viewmodel.ext.android.viewModel
import java.io.File

class KtMakeStampActivity : BaseKotlinActivity<KtMakeStampViewModel>() {

    override val layoutResourceId: Int
        get() = R.layout.activity_kt_make_stamp
    override val viewModel: KtMakeStampViewModel by viewModel()

    private var backPressedTime: Long = 0

    override fun initViewStart() {
        setBackButtonAboveActionBar(true, getString(R.string.title_make_stamp_activity))
        make_stamp_hidden_checkbox.isChecked = SharedPreferencesManager.getStampHiddenEnabled(applicationContext)
    }

    override fun initDataBinding() {
        viewModel.stampUriEvent.observe(this, Observer { uri ->
            uri?.let {
                make_stamp_image_view.run { post { setImageURI(it) } }
            }
        })
        viewModel.finishActivityWithStampNameEvent.observe(this, Observer { pairNameUri ->
            pairNameUri?.let {
                intent.run {
                    putExtra(EtcConstant.STAMP_NAME_INTENT_KEY, pairNameUri.first)
                    data = pairNameUri.second
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
        })
        viewModel.galleryAddPicEvent.observe(this, Observer { uri ->
            uri?.let { galleryAddPic(it) }
        })
    }

    override fun initViewFinal() {
        viewModel.setImageView(applicationContext, intent)

        make_stamp_submit_button.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(make_stamp_name_edit_text.windowToken, 0)
            val needsHidden = make_stamp_hidden_checkbox.isChecked
            viewModel.clickOkButton(applicationContext, make_stamp_name_edit_text.text.toString(), needsHidden)
        }
        make_stamp_hidden_checkbox.setOnCheckedChangeListener { _, isChecked ->
            SharedPreferencesManager.setStampHiddenEnabled(applicationContext, isChecked)
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

    override fun onBackPressed() {
        if (System.currentTimeMillis() - backPressedTime < 2000) {
            cancelAndFinish()
        } else {
            Snackbar.make(findViewById(android.R.id.content),
                    getString(R.string.snackbar_make_stamp_acti_back_to_exit),
                    Snackbar.LENGTH_LONG)
                    .show()
            backPressedTime = System.currentTimeMillis()
        }
    }

    private fun cancelAndFinish() {
        viewModel.stampUriEvent.value?.let {
            setResult(RESULT_CANCELED, intent)
        }
        finish()
    }

}
