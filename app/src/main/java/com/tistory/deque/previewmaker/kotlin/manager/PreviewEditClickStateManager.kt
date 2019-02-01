package com.tistory.deque.previewmaker.kotlin.manager

import com.tistory.deque.previewmaker.kotlin.model.enums.PreviewEditClickStateEnum.*

object PreviewEditClickStateManager {
    var nowState = STATE_NONE_CLICK

    fun initState(){
        nowState = STATE_NONE_CLICK
    }

    fun isShowGuildLine() = (nowState == STATE_STAMP_EDIT || nowState == STATE_STAMP_ZOOM)
}