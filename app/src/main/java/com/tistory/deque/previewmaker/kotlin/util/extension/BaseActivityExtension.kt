package com.tistory.deque.previewmaker.kotlin.util.extension

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import java.io.File

fun AppCompatActivity.galleryAddPic(picPath:String){
    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
    mediaScanIntent.data = Uri.fromFile(File(picPath))
    sendBroadcast(mediaScanIntent)
}

fun AppCompatActivity.galleryAddPic(imageURI: Uri) {
    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
    mediaScanIntent.data = imageURI
    sendBroadcast(mediaScanIntent)
}