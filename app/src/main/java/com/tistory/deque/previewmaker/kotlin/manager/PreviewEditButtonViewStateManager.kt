package com.tistory.deque.previewmaker.kotlin.manager

import com.tistory.deque.previewmaker.kotlin.model.enums.PreviewEditButtonViewStateEnum
import com.tistory.deque.previewmaker.kotlin.model.enums.PreviewEditButtonViewStateEnum.*
import com.tistory.deque.previewmaker.kotlin.util.EzLogger

/**
 * 프리뷰를 편집하는 버튼들의 상태를 관리
 */
object PreviewEditButtonViewStateManager {

    var nowState: PreviewEditButtonViewStateEnum = HOME
    var prevState: PreviewEditButtonViewStateEnum? = null

    fun initState() {
        nowState = HOME
        prevState = null
    }

    fun isHomeState() = nowState == HOME

    fun setStampState(): Boolean {
        return when (nowState) {
            HOME -> run {
                prevState = HOME
                nowState = STAMP
                true
            }
            else -> false
        }
    }

    fun setFilterState(): Boolean {
        return when (nowState) {
            HOME -> run {
                prevState = HOME
                nowState = FILTER
                true
            }
            else -> false
        }
    }

    fun clickStampBrightness(): Boolean {
        return when (nowState) {
            STAMP -> run {
                prevState = STAMP
                nowState = ONE_SEEK_BAR
                true
            }
            else -> false
        }
    }

    fun clickFilterBrightContra(): Boolean {
        return when (nowState) {
            FILTER -> run {
                prevState = FILTER
                nowState = TWO_SEEK_BAR
                true
            }
            else -> false
        }
    }

    fun clickFilterKelvinSatu(): Boolean {
        return when (nowState) {
            FILTER -> run {
                prevState = FILTER
                nowState = TWO_SEEK_BAR
                true
            }
            else -> false
        }
    }

    fun clickFilterBlur(): Boolean {
        return when (nowState) {
            FILTER -> run {
                prevState = FILTER
                nowState = ONLY_CANCEL_OR_OK
                true
            }
            else -> false
        }
    }

    fun finishEdit(): Boolean {
        EzLogger.d("finishEdit, nowState : ${nowState}")
        return when (nowState) {
            STAMP -> run {
                prevState = STAMP
                nowState = HOME
                true
            }
            FILTER -> run {
                prevState = FILTER
                nowState = HOME
                true
            }
            ONE_SEEK_BAR, TWO_SEEK_BAR, ONLY_CANCEL_OR_OK -> run {
                val temp = prevState ?: HOME
                prevState = nowState
                nowState = temp
                true
            }
            else -> false
        }
    }

    fun forceChangeStampState() {
        EzLogger.d("forceChangeStampState")
        if(nowState != HOME) return
        EzLogger.d("prevState : $prevState, nowState: $nowState")
        prevState = nowState
        nowState = STAMP
        EzLogger.d("change -> \nprevState : $prevState, nowState: $nowState")
    }

}