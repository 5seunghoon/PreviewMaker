package com.tistory.deque.previewmaker.kotlin.model

class PreviewAdapterModel {

    var previewArrayList: ArrayList<Preview> = ArrayList()

    val size: Int
        get() = previewArrayList.size

    fun getPreview(position: Int): Preview = previewArrayList[position]
    fun addPreview(preview: Preview) {
        previewArrayList.add(preview)
    }

    fun initPreviewList() {
        previewArrayList = ArrayList()
    }

    fun delete(position: Int) {
        previewArrayList.removeAt(position)
    }

}