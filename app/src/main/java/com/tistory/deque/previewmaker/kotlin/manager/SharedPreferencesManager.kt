package com.tistory.deque.previewmaker.kotlin.manager

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

object SharedPreferencesManager {
    private const val SHARED_PREFERENCES_NAME = "PREVIEW_MAKER_PREFERENCES"

    private const val STAMP_HIDDEN_ENABLED = "STAMP_HIDDEN_ENABLED"

    private var mPref: SharedPreferences? = null

    private fun getPref(context: Context): SharedPreferences {
        mPref = mPref ?: context.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)
        return mPref!!
    }

    fun setStampHiddenEnabled(context: Context, enabled: Boolean) {
        getPref(context).edit().putBoolean(STAMP_HIDDEN_ENABLED, enabled).apply()
    }

    fun getStampHiddenEnabled(context: Context): Boolean {
        return getPref(context).getBoolean(STAMP_HIDDEN_ENABLED, true)
    }
}