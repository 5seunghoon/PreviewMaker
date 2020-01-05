package com.tistory.deque.previewmaker.kotlin.setting

import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import com.tistory.deque.previewmaker.R
import com.tistory.deque.previewmaker.kotlin.base.BaseKotlinActivity
import com.tistory.deque.previewmaker.kotlin.manager.SharedPreferencesManager
import com.tistory.deque.previewmaker.kotlin.util.EtcConstant.PREVIEW_BITMAP_SIZE_LIMIT_MAX
import com.tistory.deque.previewmaker.kotlin.util.extension.setStatusBarColor
import kotlinx.android.synthetic.main.activity_kt_setting.*
import org.koin.android.viewmodel.ext.android.viewModel

class KtSettingActivity : BaseKotlinActivity<KtSettingViewModel>() {

    override val layoutResourceId: Int = R.layout.activity_kt_setting
    override val viewModel: KtSettingViewModel by viewModel()

    private val previewSizeLimitSeekBarProgress: Int
        get() = setting_preview_size_limit_seek_bar.progress

    override fun initViewStart() {
        viewModel.initPreferencesValue(applicationContext)

        title = resources.getString(R.string.setting_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.elevation = 0f
        setStatusBarColor()

        setting_preview_size_limit_seek_bar.run {
            max = viewModel.transformPreviewSizeLimitRealToSeekBar(PREVIEW_BITMAP_SIZE_LIMIT_MAX)
            progress = viewModel.transformPreviewSizeLimitRealToSeekBar(SharedPreferencesManager.getPreviewBitmapSizeLimit(applicationContext))
            setOnSeekBarChangeListener(viewModel.previewSizeLimitSeekBarListener)
        }

        setting_preview_size_limit_text.text = viewModel.transformPreviewSizeLimitSeekBarToReal(previewSizeLimitSeekBarProgress).toString()
    }

    override fun initDataBinding() {
        viewModel.previewSizeLimitSeekBarEvent.observe(this, Observer {
            setting_preview_size_limit_text.text = viewModel.transformPreviewSizeLimitSeekBarToReal(it).toString()
        })
    }

    override fun initViewFinal() {
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.setting_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                confirmSaveAndFinish()
                return true
            }
            R.id.setting_toolbar_save -> {
                viewModel.savePreferences(applicationContext, previewSizeLimitSeekBarProgress)
                return true
            }
        }
        return false
    }

    override fun onBackPressed() {
        confirmSaveAndFinish()
    }

    private fun confirmSaveAndFinish() {
        if (viewModel.isPreferencesValueChanged(previewSizeLimitSeekBarProgress)) {
            AlertDialog.Builder(this, R.style.AppTheme_Dialog)
                    .setMessage(R.string.setting_save_dialog_message)
                    .setPositiveButton(R.string.setting_save_dialog_positive_text) { dialog, _ ->
                        dialog.dismiss()
                        viewModel.savePreferences(applicationContext, previewSizeLimitSeekBarProgress)
                        finish()
                    }
                    .setNegativeButton(R.string.setting_save_dialog_negative_text) { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }
                    .show()
        } else {
            finish()
        }
    }
}