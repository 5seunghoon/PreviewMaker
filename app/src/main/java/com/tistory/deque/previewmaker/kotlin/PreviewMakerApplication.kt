package com.tistory.deque.previewmaker.kotlin

import android.app.Application
import com.tistory.deque.previewmaker.kotlin.di.diModule
import org.koin.android.ext.android.startKoin

class PreviewMakerApplication:Application(){
    override fun onCreate() {
        super.onCreate()
        startKoin(applicationContext, diModule)
    }
}