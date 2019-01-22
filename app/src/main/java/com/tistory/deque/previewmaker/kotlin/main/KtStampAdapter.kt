package com.tistory.deque.previewmaker.kotlin.main

import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import android.view.ViewGroup
import com.tistory.deque.previewmaker.kotlin.model.Stamp
import com.tistory.deque.previewmaker.kotlin.model.StampAdapterModel

class KtStampAdapter(private val stampAdapterModel: StampAdapterModel): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    var stampClickListener: (Stamp)->Unit = {}
    var stampDeleteListener: (Stamp)->Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
            = KtStampHolder(parent, stampClickListener, stampDeleteListener)

    override fun getItemCount() = stampAdapterModel.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? KtStampHolder)?.onBind(stampAdapterModel.getItem(position))
    }

    fun copyStampList(stampList: ArrayList<Stamp>) {
        stampAdapterModel.copyList(stampList)
        notifyDataSetChanged()
    }

    fun addStamp(stamp: Stamp) {
        stampAdapterModel.addStamp(stamp)
        notifyItemInserted(stampAdapterModel.size)
    }

}