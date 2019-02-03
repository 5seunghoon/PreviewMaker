package com.tistory.deque.previewmaker.kotlin.helppreviewedit

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.tistory.deque.previewmaker.R

class KtHelpPreviewEditActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.title_help_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.activity_kt_help_preview_edit)
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
