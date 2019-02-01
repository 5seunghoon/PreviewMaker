package com.tistory.deque.previewmaker.kotlin.base

import android.app.Dialog
import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem

/**
 * BaseKotlinActivity<ActivitySbsMainBinding>
 * 와 같이 상속 받을 때, ActivitySbsMainBinding 과 같은 파일이 자동생성되지 않는다면
 * 1. 해당 엑티비티의 레이아웃이 <layout></layout> 으로 감싸져 있는지 확인
 * 2. 클린 빌드 후 다시 빌드 수행
 * 3. 이름 확인 : sbs_main_activity => ActivitySbsMainBinding
 */
abstract class BaseKotlinActivity<T : BaseKotlinViewModel> : AppCompatActivity() {

    /**
     * setContentView로 호출할 Layout의 리소스 id.
     * ex) R.layout.activity_sbs_main
     */
    abstract val layoutResourceId: Int

    /**
     * viewModel 로 쓰일 변수.
     */
    abstract val viewModel: T

    /**
     * 레이아웃을 띄운 직후 호출.
     * 뷰나 액티비티의 속성 등을 초기화.
     * ex) 리사이클러뷰, 툴바, 드로어뷰..
     */
    abstract fun initViewStart()

    /**
     * 두번째로 호출.
     * 데이터 바인딩 및 rxjava 설정.
     * ex) rxjava observe, databinding observe..
     */
    abstract fun initDataBinding()

    /**
     * 가장 마지막에 호출. 바인딩 이후에 할 일을 여기에 구현.
     * 그 외에 설정할 것이 있으면 이곳에서 설정.
     * 클릭 리스너도 이곳에서 설정.
     */
    abstract fun initViewFinal()

    private var mLoadingDialog: Dialog? = null

    private var isSetBackButtonValid = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(layoutResourceId)

        baseObserving()
        initViewStart()
        initDataBinding()
        initViewFinal()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (isSetBackButtonValid) {
            when (item?.itemId) {
                android.R.id.home -> {
                    finish()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun baseObserving() {
        viewModel.observeSnackbarMessage(this) {
            Snackbar.make(findViewById(android.R.id.content), it, Snackbar.LENGTH_LONG).show()
        }
        viewModel.observeSnackbarMessageStr(this) {
            Snackbar.make(findViewById(android.R.id.content), it, Snackbar.LENGTH_LONG).show()
        }
    }


    /**
     * 기본 툴바를 썼을 때 뒤로가기 버튼을 넣는 코드
     * 버튼 색깔은 values/style.xml 에서 colorControlNormal 을 수정하면 됨
     */
    fun setBackButtonAboveActionBar(titleShow: Boolean, titleString: String?) {
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            if (!titleShow) setDisplayShowTitleEnabled(false)
            else titleString?.let { title = it }
        }
        isSetBackButtonValid = true
    }

}