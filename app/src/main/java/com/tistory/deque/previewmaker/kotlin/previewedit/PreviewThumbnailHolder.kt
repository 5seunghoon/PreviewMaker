package com.tistory.deque.previewmaker.kotlin.previewedit

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.tistory.deque.previewmaker.R
import com.tistory.deque.previewmaker.kotlin.model.Preview
import kotlinx.android.synthetic.main.kt_preview_thumbnail_item.view.*

class PreviewThumbnailHolder(parent:ViewGroup, var previewThumbnailClickListener: (Preview, Int) -> Unit): RecyclerView.ViewHolder(
        LayoutInflater
                .from(parent.context)
                .inflate(R.layout.kt_preview_thumbnail_item, parent, false)
){
    fun onBind(item: Preview, position: Int) {
        itemView.run {
            preview_thumbnail_item_image_view.setImageURI(item.thumbnailImageUri)
            preview_thumbnail_item_layout.setOnClickListener {
                previewThumbnailClickListener(item, position)
            }
        }
    }
}