package com.tistory.deque.previewmaker.kotlin.setting

import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import com.tistory.deque.previewmaker.R
import com.tistory.deque.previewmaker.kotlin.base.BaseKotlinActivity
import com.tistory.deque.previewmaker.kotlin.manager.SharedPreferencesManager
import com.tistory.deque.previewmaker.kotlin.util.EtcConstant.PREVIEW_BITMAP_SIZE_LIMIT_MAX
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

        setting_preview_size_limit_seek_bar.run {
            max = viewModel.generatePreviewSizeLimitRealToSeekBar(PREVIEW_BITMAP_SIZE_LIMIT_MAX)
            progress = viewModel.generatePreviewSizeLimitRealToSeekBar(SharedPreferencesManager.getPreviewBitmapSizeLimit(applicationContext))
            setOnSeekBarChangeListener(viewModel.previewSizeLimitSeekBarListener)
        }

        setting_preview_size_limit_text.text = viewModel.generatePreviewSizeLimitSeekBarToReal(previewSizeLimitSeekBarProgress).toString()
    }

    override fun initDataBinding() {
        viewModel.previewSizeLimitSeekBarEvent.observe(this, Observer {
            setting_preview_size_limit_text.text = viewModel.generatePreviewSizeLimitSeekBarToReal(it).toString()
        })
    }

    override fun initViewFinal() {
        setting_save_button.setOnClickListener {
            viewModel.savePreferences(applicationContext, previewSizeLimitSeekBarProgress)\
        }
    }

    override fun onBackPressed() {
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
                    }
                    .show()
        } else {
            super.onBackPressed()
        }
    }

}