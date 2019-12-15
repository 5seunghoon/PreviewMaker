package com.tistory.deque.previewmaker.kotlin.previewedit

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.tistory.deque.previewmaker.R
import com.tistory.deque.previewmaker.kotlin.model.Preview
import kotlinx.android.synthetic.main.kt_preview_thumbnail_item.view.*

class PreviewThumbnailHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context)
                .inflate(R.layout.kt_preview_thumbnail_item, parent, false)
) {
    fun onBind(item: Preview, position: Int, previewThumbnailClickListener: (Preview, Int) -> Unit) {
        itemView.run {
            item.thumbnailBitmap?.let {
                preview_thumbnail_item_image_view.setImageBitmap(it)
            } ?: run {
                preview_thumbnail_item_image_view.setImageResource(R.drawable.ic_error_outline_black_24dp)
            }
            preview_thumbnail_item_layout.setOnClickListener {
                previewThumbnailClickListener(item, position)
            }
        }
    }
}