package com.tistory.deque.previewmaker.kotlin.previewedit

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.tistory.deque.previewmaker.kotlin.model.PreviewListModel

class PreviewThumbnailAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    var previewListModel: PreviewListModel? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
            = PreviewThumbnailHolder(parent)

    override fun getItemCount(): Int = previewListModel?.size ?: 0

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? PreviewThumbnailHolder)?.onBind(previewListModel?.getPreview(position) ?: return)
    }
}
