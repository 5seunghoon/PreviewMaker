package com.tistory.deque.previewmaker.kotlin.helppreviewedit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.tistory.deque.previewmaker.R
import com.tistory.deque.previewmaker.kotlin.util.extension.setStatusBarColor

class KtHelpPreviewEditActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.title_help_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.activity_kt_help_preview_edit)
        setStatusBarColor()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                //뒤로가기 버튼
                finish()
                return true
            }
        }
        return false
    }

    override fun onBackPressed() {
        finish()
    }
}
