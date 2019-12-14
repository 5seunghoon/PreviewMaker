package com.tistory.deque.previewmaker.kotlin.credit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.tistory.deque.previewmaker.R
import com.tistory.deque.previewmaker.kotlin.opensource.KtOpenSourceActivity
import kotlinx.android.synthetic.main.activity_kt_credit.*

class KtCreditActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kt_credit)

        title = getString(R.string.title_credit_activity)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        credit_version_text_view.run {
            post {
                text = try {
                    packageManager.getPackageInfo(application.packageName, 0).versionName
                } catch (e: Exception) {
                    ""
                }
            }
        }
        credit_app_name_text_view.run { post { text = getString(R.string.app_name) } }

        credit_open_source_text_view.setOnClickListener {
            startActivity(Intent(applicationContext, KtOpenSourceActivity::class.java))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                //뒤로가기 버튼
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        finish()
    }

}
