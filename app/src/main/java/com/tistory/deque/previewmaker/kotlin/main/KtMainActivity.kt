package com.tistory.deque.previewmaker.kotlin.main

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import com.tistory.deque.previewmaker.Activity.CreditActivity
import com.tistory.deque.previewmaker.Activity.HelpMainActivity
import com.tistory.deque.previewmaker.R
import com.tistory.deque.previewmaker.kotlin.KtDbOpenHelper
import com.tistory.deque.previewmaker.kotlin.base.BaseKotlinActivity
import com.tistory.deque.previewmaker.kotlin.util.EzLogger
import kotlinx.android.synthetic.main.activity_kt_main.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class KtMainActivity : BaseKotlinActivity<KtMainViewModel>() {
    override val layoutResourceId: Int
        get() = R.layout.activity_kt_main
    override val viewModel: KtMainViewModel by viewModel()

    private val stampAdapter: KtStampAdapter by inject()

    private var dbOpenHelper: KtDbOpenHelper? = null

    override fun initViewStart() {
        setBackButtonAboveActionBar(true, "심플 프리뷰 메이커")
        dbOpen()
        setRecyclerView()
        return
    }

    override fun initDataBinding() {
        return
    }

    override fun initViewFinal() {
        return
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        when (id) {
            R.id.action_help -> {
                val intent1 = Intent(applicationContext, HelpMainActivity::class.java)
                startActivity(intent1)
                return true
            }
            R.id.action_credit -> {
                val intent2 = Intent(applicationContext, CreditActivity::class.java)
                startActivity(intent2)
                return true
            }
        }
        return false
    }

    private fun dbOpen() {
        EzLogger.d("main activity : db open")
        dbOpenHelper = KtDbOpenHelper.getDbOpenHelper(applicationContext,
                KtDbOpenHelper.DP_OPEN_HELPER_NAME,
                null,
                KtDbOpenHelper.dbVersion)
        dbOpenHelper?.dbOpen() ?: EzLogger.d("db open fail : dbOpenHelper null")
    }

    private fun setRecyclerView(){
        main_stamp_recycler_view.run {
            layoutManager = LinearLayoutManager(applicationContext, RecyclerView.VERTICAL, false)
            adapter = stampAdapter
            setHasFixedSize(true)
        }
    }

}
