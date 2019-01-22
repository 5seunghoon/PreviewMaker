package com.tistory.deque.previewmaker.kotlin.main

import android.Manifest
import android.arch.lifecycle.Observer
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import android.view.View
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.tistory.deque.previewmaker.Activity.CreditActivity
import com.tistory.deque.previewmaker.Activity.HelpMainActivity
import com.tistory.deque.previewmaker.Activity.PreviewEditActivity
import com.tistory.deque.previewmaker.R
import com.tistory.deque.previewmaker.kotlin.base.BaseKotlinActivity
import com.tistory.deque.previewmaker.kotlin.makestamp.KtMakeStampActivity
import com.tistory.deque.previewmaker.kotlin.model.Stamp
import com.tistory.deque.previewmaker.kotlin.previewedit.KtPreviewEditActivity
import com.tistory.deque.previewmaker.kotlin.util.EtcConstant
import com.tistory.deque.previewmaker.kotlin.util.EzLogger
import com.tistory.deque.previewmaker.kotlin.util.RequestCode
import com.tistory.deque.previewmaker.kotlin.util.extension.galleryAddPic
import kotlinx.android.synthetic.main.activity_kt_main.*
import me.nereo.multi_image_selector.MultiImageSelectorActivity
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import kotlin.collections.ArrayList

class KtMainActivity : BaseKotlinActivity<KtMainViewModel>() {
    override val layoutResourceId: Int
        get() = R.layout.activity_kt_main
    override val viewModel: KtMainViewModel by viewModel()

    private val stampAdapter: KtStampAdapter by inject()

    private var mBackPressedTime: Long = 0

    override fun initViewStart() {
        title = "심플 프리뷰 메이커"
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
            visibleHint()
        })
        viewModel.stampListLiveData.observe(this, Observer { stampList ->
            stampList?.let {
                stampAdapter.copyStampList(it)
                invisibleHint()
            }
        })
        viewModel.imagePickStartEvent.observe(this, Observer {
            startImagePick()
        })
        viewModel.addStampLiveData.observe(this, Observer { stamp ->
            stamp?.let {
                stampAdapter.addStamp(it)
                invisibleHint()
            }
        })
        viewModel.makeStampActivityStartEvent.observe(this, Observer { uri ->
            uri?.let {
                val intent = Intent(applicationContext, KtMakeStampActivity::class.java)
                intent.data = it
                startActivityForResult(intent, RequestCode.REQUEST_MAKE_STAMP_ACTIVITY)
            }
        })
        viewModel.delStampAlertStartEvent.observe(this, Observer { pair ->
            pair?.let { delAlertShow(it.first, it.second) }
        })
        viewModel.delStampFromAdapterEvent.observe(this, Observer { position ->
            position?.let { stampAdapter.delStamp(it) }
        })
        viewModel.galleryAddPicEvent.observe(this, Observer { uri ->
            uri?.let { galleryAddPic(it) }
        })
        viewModel.clickStampEvent.observe(this, Observer { stamp ->
            stamp?.let { clickStamp(it) }
        })
        viewModel.previewGalleryStartEvent.observe(this, Observer {
            startPreviewGallery()
        })
        viewModel.previewEditStartEvent.observe(this, Observer { pathList ->
            pathList?.let { startPreviewEditActivity(it) }
        })
    }

    private fun startPreviewEditActivity(pathList: ArrayList<String>) {
        viewModel.selectedStamp?.let { stamp ->
            val intent = Intent(applicationContext, KtPreviewEditActivity::class.java).apply {
                putStringArrayListExtra(EtcConstant.EXTRA_PREVIEW_LIST_INTENT_KEY, pathList)
                putExtra(EtcConstant.STAMP_INTENT_KEY, stamp)
            }
            startActivity(intent)
        }
    }

    private fun startPreviewGallery() {
        val intent = Intent(applicationContext, MultiImageSelectorActivity::class.java)
        // whether show camera
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, false)
        // max select image amount
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, EtcConstant.MAX_SELECT_IMAGE_ACCOUNT)
        // select mode (MultiImageSelectorActivity.MODE_SINGLE OR MultiImageSelectorActivity.MODE_MULTI)
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_MULTI)
        // default select images (support array list)
        intent.putStringArrayListExtra(MultiImageSelectorActivity.EXTRA_DEFAULT_SELECTED_LIST, ArrayList<String>())
        startActivityForResult(intent, RequestCode.REQUEST_TAKE_PREVIEW_FROM_ALBUM)
    }

    private fun clickStamp(stamp: Stamp) {
        TedPermission.with(applicationContext)
                .setPermissionListener(object : PermissionListener {
                    override fun onPermissionGranted() {
                        viewModel.savePositionAndGetPreview(stamp)
                    }

                    override fun onPermissionDenied(deniedPermissions: ArrayList<String>) {}
                })
                .setRationaleMessage(getString(R.string.tedpermission_select_stamp_rational))
                .setDeniedMessage(getString(R.string.tedpermission_select_stamp_deny_rational))
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                .setGotoSettingButton(true)
                .check()
    }

    private fun delAlertShow(stamp: Stamp, position: Int) {
        val stampDeleteAlert = AlertDialog.Builder(this)
        stampDeleteAlert
                .setMessage("낙관 [${stamp.name}] 을 정말 삭제하시겠습니까?").setCancelable(true)
                .setPositiveButton("YES") { _, _ -> viewModel.deleteStampAndScan(stamp, position) }
                .setNegativeButton("NO") { _, _ -> }
        val delAlert = stampDeleteAlert.create()

        TedPermission.with(applicationContext)
                .setPermissionListener(object : PermissionListener {
                    override fun onPermissionGranted() = delAlert.show()
                    override fun onPermissionDenied(deniedPermissions: ArrayList<String>) {}
                })
                .setRationaleMessage(getString(R.string.tedpermission_del_stamp_rational))
                .setDeniedMessage(getString(R.string.tedpermission_del_stamp_deny_rational))
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                .setGotoSettingButton(true)
                .check()
    }

    private fun invisibleHint() {
        if (stampAdapter.size > 0) {
            main_hint_text_view.run { post { visibility = View.GONE } }
        }
    }

    private fun visibleHint() {
        if (stampAdapter.size <= 0) {
            main_hint_text_view.run { post { visibility = View.VISIBLE } }
        }
    }

    private fun startImagePick() {
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
                        override fun onPermissionGranted() = viewModel.addStamp()
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
        viewModel.onActivityResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() {
        if (System.currentTimeMillis() - mBackPressedTime > 2000) {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.snackbar_main_acti_back_to_exit), Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.snackbar_main_acti_back_to_exit_btn)) { finish() }
                    .show()
            mBackPressedTime = System.currentTimeMillis()
        } else {
            finish()
        }
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
