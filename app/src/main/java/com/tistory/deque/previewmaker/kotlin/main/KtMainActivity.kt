package com.tistory.deque.previewmaker.kotlin.main

import android.Manifest
import androidx.lifecycle.Observer
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.MediaStore
import com.google.android.material.snackbar.Snackbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.tistory.deque.previewmaker.R
import com.tistory.deque.previewmaker.kotlin.base.BaseKotlinActivity
import com.tistory.deque.previewmaker.kotlin.credit.KtCreditActivity
import com.tistory.deque.previewmaker.kotlin.helpmain.KtHelpMainActivity
import com.tistory.deque.previewmaker.kotlin.makestamp.KtMakeStampActivity
import com.tistory.deque.previewmaker.kotlin.manager.FilePathManager
import com.tistory.deque.previewmaker.kotlin.model.Stamp
import com.tistory.deque.previewmaker.kotlin.previewedit.KtPreviewEditActivity
import com.tistory.deque.previewmaker.kotlin.setting.KtSettingActivity
import com.tistory.deque.previewmaker.kotlin.util.EtcConstant
import com.tistory.deque.previewmaker.kotlin.util.EzLogger
import com.tistory.deque.previewmaker.kotlin.util.RequestCode
import com.tistory.deque.previewmaker.kotlin.util.extension.galleryAddPic
import kotlinx.android.synthetic.main.activity_kt_main.*
import me.nereo.multi_image_selector.MultiImageSelectorActivity
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import java.lang.StringBuilder
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

    override fun initViewFinal() {
        main_stamp_add_fab.setOnClickListener {
            TedPermission.with(applicationContext)
                    .setPermissionListener(object : PermissionListener {
                        override fun onPermissionGranted() = viewModel.addStampToDb()

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        viewModel.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
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
                toolboxClickListener = object: ToolboxClickListener {
                    override fun helpClickListener() {
                        startActivity(Intent(applicationContext, KtHelpMainActivity::class.java))
                    }

                    override fun creditClickListener() {
                        startActivity(Intent(applicationContext, KtCreditActivity::class.java))
                    }

                    override fun optionClickListener() {
                        startActivity(Intent(applicationContext, KtSettingActivity::class.java))
                    }
                }
            }
            setHasFixedSize(true)
        }
    }

    private fun startPreviewEditActivity(pathList: ArrayList<String>) {
        viewModel.selectedStamp?.let { stamp ->
            val intent = Intent(applicationContext, KtPreviewEditActivity::class.java).apply {
                putStringArrayListExtra(EtcConstant.PREVIEW_LIST_INTENT_KEY, pathList)
                putExtra(EtcConstant.STAMP_ID_INTENT_KEY, stamp.id)
                data = stamp.imageUri
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
                    override fun onPermissionGranted() = viewModel.savePositionAndGetPreview(stamp)

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
        val deleteAlert = AlertDialog.Builder(this, R.style.AppTheme_Dialog).apply {
            setMessage(
                    StringBuilder().apply {
                        append(applicationContext.resources.getString(R.string.main_stamp_delete_alert_first))
                        append(stamp.name)
                        append(applicationContext.resources.getString(R.string.main_stamp_delete_alert_second))
                    }.toString()
            )
            setPositiveButton("YES") { _, _ -> viewModel.deleteStampAndScan(context, stamp, position) }
            setNegativeButton("NO") { _, _ -> }
        }.create()

        TedPermission.with(applicationContext)
                .setPermissionListener(object : PermissionListener {
                    override fun onPermissionGranted() = deleteAlert.show()
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
            main_hint_text_view.run {
                post {
                    visibility = View.GONE
                    main_stamp_recycler_view.layoutParams = main_stamp_recycler_view.layoutParams.apply {
                        height = ViewGroup.LayoutParams.MATCH_PARENT
                    }
                }
            }
        }
    }

    private fun visibleHint() {
        if (stampAdapter.size <= 0) {
            main_hint_text_view.run {
                post {
                    visibility = View.VISIBLE
                    main_stamp_recycler_view.layoutParams = main_stamp_recycler_view.layoutParams.apply {
                        height = ViewGroup.LayoutParams.WRAP_CONTENT
                    }
                }
            }
        }
    }

    private fun startImagePick() {
        EzLogger.d("start image pick event observe")
        val permissionCheck = ContextCompat.checkSelfPermission(applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        EzLogger.d("start Activity : album intent")
        startActivityForResult(intent, RequestCode.REQUEST_TAKE_STAMP_FROM_ALBUM)
    }

}
