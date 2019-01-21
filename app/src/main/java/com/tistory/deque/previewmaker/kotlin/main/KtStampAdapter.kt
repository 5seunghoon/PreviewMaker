package com.tistory.deque.previewmaker.kotlin.main

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.tistory.deque.previewmaker.kotlin.model.StampAdapterModel

class KtStampAdapter(private val stampAdapterModel: StampAdapterModel): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = KtStampHolder(parent)

    override fun getItemCount() = stampAdapterModel.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? KtStampHolder)?.onBind(stampAdapterModel.getItem(position))
    }
}