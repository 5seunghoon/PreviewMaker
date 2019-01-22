package com.tistory.deque.previewmaker.kotlin.main

import android.Manifest
import android.arch.lifecycle.Observer
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import android.view.View
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.tistory.deque.previewmaker.Activity.CreditActivity
import com.tistory.deque.previewmaker.Activity.HelpMainActivity
import com.tistory.deque.previewmaker.Activity.MakeStampActivity
import com.tistory.deque.previewmaker.R
import com.tistory.deque.previewmaker.Util.Logger
import com.tistory.deque.previewmaker.kotlin.KtDbOpenHelper
import com.tistory.deque.previewmaker.kotlin.base.BaseKotlinActivity
import com.tistory.deque.previewmaker.kotlin.util.EzLogger
import com.tistory.deque.previewmaker.kotlin.util.RequestCode
import kotlinx.android.synthetic.main.activity_kt_main.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import java.io.File
import java.util.ArrayList

class KtMainActivity : BaseKotlinActivity<KtMainViewModel>() {
    override val layoutResourceId: Int
        get() = R.layout.activity_kt_main
    override val viewModel: KtMainViewModel by viewModel()

    private val stampAdapter: KtStampAdapter by inject()

    private var dbOpenHelper: KtDbOpenHelper? = null

    override fun initViewStart() {
        setBackButtonAboveActionBar(true, "심플 프리뷰 메이커")
        viewModel.dbOpen(applicationContext)
        viewModel.getAllStampFromDb()
        setRecyclerView()
        return
    }

    override fun initDataBinding() {
        viewModel.invisibleHintEvent.observe(this, Observer {
            invisibleHint()
        })
        viewModel.visibleHintEvent.observe(this, Observer {
            main_hint_text_view.run { post { visibility = View.VISIBLE } }
        })
        viewModel.stampListLiveData.observe(this, Observer { stampList ->
            stampList?.let { stampAdapter.copyStampList(it) }
        })
        viewModel.imagePickStartEvent.observe(this, Observer {
            startImagePick()
        })
        viewModel.addStampLiveData.observe(this, Observer {stamp ->
            stamp?.let {
                invisibleHint()
                stampAdapter.addStamp(it)
            }
        })
        viewModel.makeStampActivityStartEvent.observe(this, Observer {uri ->
            uri?.let {
                val intent = Intent(applicationContext, MakeStampActivity::class.java)
                intent.data = it
                startActivityForResult(intent, RequestCode.REQUEST_MAKE_STAMP_ACTIVITY)
            }
        })
        viewModel.galleryAddPicEvent.observe(this, Observer { path ->
            path?.let {
                val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                mediaScanIntent.data = Uri.fromFile(File(it))
                sendBroadcast(mediaScanIntent)
            }
        })
    }

    private fun invisibleHint() = main_hint_text_view.run { post { visibility = View.GONE } }


    private fun startImagePick(){
        EzLogger.d("start image pick event observe")
        val permissionCheck = ContextCompat.checkSelfPermission(applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            EzLogger.d("permission not granted")
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        } else {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            intent.type = MediaStore.Images.Media.CONTENT_TYPE
            EzLogger.d("start Activity : album intent")
            startActivityForResult(intent, RequestCode.REQUEST_TAKE_STAMP_FROM_ALBUM)
        }
    }

    override fun initViewFinal() {
        main_stamp_add_fab.setOnClickListener {
            TedPermission.with(applicationContext)
                    .setPermissionListener(object : PermissionListener {
                        override fun onPermissionGranted() {
                            viewModel.addStamp()
                        }

                        override fun onPermissionDenied(deniedPermissions: ArrayList<String>) {}
                    })
                    .setRationaleMessage(getString(R.string.tedpermission_add_stamp_rational))
                    .setDeniedMessage(getString(R.string.tedpermission_add_stamp_deny_rational))
                    .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                    .setGotoSettingButton(true)
                    .check()
        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        viewModel.onActivityResult(applicationContext, requestCode, resultCode, data)
    }

    private fun setRecyclerView() {
        main_stamp_recycler_view.run {
            layoutManager = LinearLayoutManager(applicationContext, RecyclerView.VERTICAL, false)
            adapter = stampAdapter.apply {
                stampClickListener = viewModel::stampClickListener
                stampDeleteListener = viewModel::stampDeleteListener
            }
            setHasFixedSize(true)
        }
    }


}
