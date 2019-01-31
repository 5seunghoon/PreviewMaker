package com.tistory.deque.previewmaker.kotlin.previewedit

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.tistory.deque.previewmaker.R
import com.tistory.deque.previewmaker.kotlin.model.Preview
import kotlinx.android.synthetic.main.kt_preview_thumbnail_item.view.*

class PreviewThumbnailHolder(parent:ViewGroup): RecyclerView.ViewHolder(
        LayoutInflater
                .from(parent.context)
                .inflate(R.layout.kt_preview_thumbnail_item, parent, false)
){
    fun onBind(item: Preview) {
        itemView.run {
            preview_thumbnail_item_image_view.setImageURI(item.thumbnailUri)
        }
    }
}