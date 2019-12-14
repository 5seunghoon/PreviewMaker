package com.tistory.deque.previewmaker.kotlin.opensource

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tistory.deque.previewmaker.R

class KtOpenSourceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kt_open_source)
    }

    override fun onBackPressed() {
        finish()
    }
}
