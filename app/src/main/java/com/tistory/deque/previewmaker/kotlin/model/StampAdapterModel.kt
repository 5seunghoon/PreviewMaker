package com.tistory.deque.previewmaker.kotlin.model

class StampAdapterModel(){

    private var stampList: ArrayList<Stamp> = ArrayList()
    val size: Int get() = stampList.size

    fun getItem(position: Int) =  stampList[position]
}