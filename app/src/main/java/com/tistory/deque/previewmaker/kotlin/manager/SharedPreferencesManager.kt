package com.tistory.deque.previewmaker.kotlin.manager

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.tistory.deque.previewmaker.kotlin.util.EtcConstant

object SharedPreferencesManager {
    private const val SHARED_PREFERENCES_NAME = "PREVIEW_MAKER_PREFERENCES"

    private const val STAMP_HIDDEN_ENABLED = "STAMP_HIDDEN_ENABLED"
    private const val PREVIEW_WIDTH_OVER_HEIGHT_RATIO = "PREVIEW_WIDTH_OVER_HEIGHT_RATIO" // width/height ratio
    private const val PREVIEW_BITMAP_SIZE_LIMIT = "PREVIEW_BITMAP_SIZE_LIMIT"

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

    fun setPreviewWidthOverHeightRatio(context: Context, widthOverHeight: Float) {
        getPref(context).edit().putFloat(PREVIEW_WIDTH_OVER_HEIGHT_RATIO, widthOverHeight).apply()
    }

    fun getPreviewWidthOverHeightRatio(context: Context): Float {
        return getPref(context).getFloat(PREVIEW_WIDTH_OVER_HEIGHT_RATIO, 1.5f)
    }

    fun setPreviewBitmapSizeLimit(context: Context, limit: Int) {
        getPref(context).edit().putInt(PREVIEW_BITMAP_SIZE_LIMIT, limit).apply()
    }

    fun getPreviewBitmapSizeLimit(context: Context): Int {
        return getPref(context).getInt(PREVIEW_BITMAP_SIZE_LIMIT, EtcConstant.PREVIEW_BITMAP_SIZE_LIMIT_DEFAULT)
    }

}