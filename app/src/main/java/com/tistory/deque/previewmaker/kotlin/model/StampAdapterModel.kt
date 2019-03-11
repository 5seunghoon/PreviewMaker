package com.tistory.deque.previewmaker.kotlin.model

import java.lang.IndexOutOfBoundsException

class StampAdapterModel(){

    private var stampList: ArrayList<Stamp> = ArrayList()
    val size: Int get() = stampList.size

    fun getItem(position: Int) =  stampList[position]

    fun copyList(stampListParam : ArrayList<Stamp>) {
        stampList.clear()
        stampListParam.forEach {
            stampList.add(it)
        }
    }

    fun addStamp(stamp: Stamp) {
        stampList.add(stamp)
    }

    fun delStamp(position: Int) {
        try {
            stampList.removeAt(position)
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
        }
    }
}