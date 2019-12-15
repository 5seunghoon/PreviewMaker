package com.tistory.deque.previewmaker.kotlin.helpmain

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.tistory.deque.previewmaker.R
import com.tistory.deque.previewmaker.kotlin.util.extension.setStatusBarColor

class KtHelpMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kt_help_main)
        setStatusBarColor()

        title = getString(R.string.title_help_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
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
