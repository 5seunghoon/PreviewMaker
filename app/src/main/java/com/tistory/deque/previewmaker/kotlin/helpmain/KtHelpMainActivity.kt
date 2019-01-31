package com.tistory.deque.previewmaker.kotlin.helpmain

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.tistory.deque.previewmaker.R

class KtHelpMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kt_help_main)

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