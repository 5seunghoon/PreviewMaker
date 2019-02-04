package com.tistory.deque.previewmaker.kotlin.manager

import com.tistory.deque.previewmaker.kotlin.model.enums.PreviewEditClickStateEnum.*

object PreviewEditClickStateManager {
    var nowState = STATE_NONE_CLICK
        private set

    fun initState() {
        nowState = STATE_NONE_CLICK
    }

    fun setNoneClickState() {
        nowState = STATE_NONE_CLICK
    }

    fun setStampEditState() {
        nowState = STATE_STAMP_EDIT
    }

    fun setBitmapFilterState() {
        nowState = STATE_BITMAP_FILTER
    }

    fun clickStamp(): Boolean {
        return if (nowState == STATE_NONE_CLICK) {
            nowState = STATE_STAMP_EDIT
            true
        } else false
    }

    fun clickStampZoomStart(): Boolean {
        return if (nowState == STATE_STAMP_EDIT) {
            nowState = STATE_STAMP_ZOOM
            true
        } else false
    }

    fun setStampZoomState() {
        nowState = STATE_STAMP_ZOOM
    }

    fun clickStampZoomEnd() {
        if (nowState == STATE_STAMP_ZOOM) {
            nowState = STATE_STAMP_EDIT
        }
    }

    fun isShowGuildLine() = (nowState == STATE_STAMP_EDIT || nowState == STATE_STAMP_ZOOM)

    fun isBlur() = (nowState == STATE_BITMAP_FILTER_BLUR)

    fun isBlurGuide() = (nowState == STATE_BITMAP_FILTER_BLUR_GUIDE)

    fun clickBlur() {
        if (nowState == STATE_BITMAP_FILTER) nowState = STATE_BITMAP_FILTER_BLUR_GUIDE
    }

    fun endBlurGuild() {
        // blur guide -> blur
        if (nowState == STATE_BITMAP_FILTER_BLUR_GUIDE) nowState = STATE_BITMAP_FILTER_BLUR
    }

    fun blurEnd() {
        if ((nowState == STATE_BITMAP_FILTER_BLUR_GUIDE) || (nowState == STATE_BITMAP_FILTER_BLUR)) {
            nowState = STATE_BITMAP_FILTER
        }
    }

    fun restartBlur() {
        if ((nowState == STATE_BITMAP_FILTER_BLUR_GUIDE) || (nowState == STATE_BITMAP_FILTER_BLUR)) {
            nowState = STATE_BITMAP_FILTER_BLUR_GUIDE
        }
    }

}