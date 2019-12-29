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
    data class PreferencesValue(val previewSizeLimit: Int)

    override val layoutResourceId: Int = R.layout.activity_kt_setting
    override val viewModel: KtSettingViewModel by viewModel()

    private lateinit var prevPreferencesValue: PreferencesValue

    private val previewSizeSeekBarInterval = 100

    override fun initViewStart() {
        prevPreferencesValue = PreferencesValue(SharedPreferencesManager.getPreviewBitmapSizeLimit(applicationContext))
        title = resources.getString(R.string.setting_title)

        setting_preview_size_limit_seek_bar.run {
            max = PREVIEW_BITMAP_SIZE_LIMIT_MAX / previewSizeSeekBarInterval
            progress = prevPreferencesValue.previewSizeLimit / previewSizeSeekBarInterval
            setOnSeekBarChangeListener(viewModel.previewSizeLimitSeekBarListener)
        }

        setting_preview_size_limit_text.text = generatePreviewSizeLimitPxText(setting_preview_size_limit_seek_bar.progress)

        setting_save_button.setOnClickListener {
            savePreferences(buildNewPreferencesValue())
            finish()
        }
    }

    override fun initDataBinding() {
        viewModel.previewSizeLimitSeekBarEvent.observe(this, Observer {
            setting_preview_size_limit_text.text = generatePreviewSizeLimitPxText(it)
        })
    }

    override fun initViewFinal() {
    }

    private fun generatePreviewSizeLimitPxText(seekBarValue: Int): String {
        return (seekBarValue * previewSizeSeekBarInterval).toString()
    }

    override fun onBackPressed() {
        val newPreferencesValue = buildNewPreferencesValue()
        if (newPreferencesValue != prevPreferencesValue) {
            AlertDialog.Builder(this, R.style.AppTheme_Dialog)
                    .setMessage(R.string.setting_save_dialog_message)
                    .setPositiveButton(R.string.setting_save_dialog_positive_text) { dialog, _ ->
                        dialog.dismiss()
                        savePreferences(newPreferencesValue)
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

    private fun buildNewPreferencesValue(): PreferencesValue {
        return PreferencesValue(setting_preview_size_limit_seek_bar.progress * previewSizeSeekBarInterval)
    }

    private fun savePreferences(newPreferencesValue: PreferencesValue) {
        SharedPreferencesManager.setPreviewBitmapSizeLimit(applicationContext, newPreferencesValue.previewSizeLimit)
        viewModel.showSnackbar(R.string.setting_save_success_snackbar_message)
    }
}