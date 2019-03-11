package com.tistory.deque.previewmaker.kotlin.util

import android.util.Log

object EzLogger {
    private val LOGGER_TAG = "[LOGGER TAG]"
    private val printlog = true

    fun d(message: String) {
        if (printlog) {
            Log.d(LOGGER_TAG, message)
        }
    }

}