package com.tistory.deque.previewmaker.kotlin.util.extension

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.tistory.deque.previewmaker.R
import java.io.File

fun Context.galleryAddPic(picPath:String){
    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
    mediaScanIntent.data = Uri.fromFile(File(picPath))
    sendBroadcast(mediaScanIntent)
}

fun Context.galleryAddPic(imageURI: Uri) {
    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
    mediaScanIntent.data = imageURI
    sendBroadcast(mediaScanIntent)
}

fun AppCompatActivity.setStatusBarColor() {
    window.statusBarColor = getColor(R.color.status_bar_color)
    if (window.statusBarColor.isBright()) {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    } else {
        window.decorView.systemUiVisibility != View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }
}
