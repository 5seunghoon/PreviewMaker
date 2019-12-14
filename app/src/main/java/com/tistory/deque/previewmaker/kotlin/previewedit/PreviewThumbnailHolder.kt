package com.tistory.deque.previewmaker.kotlin.previewedit

import android.media.ExifInterface
import androidx.recyclerview.widget.RecyclerView
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
    fun onBind(item: Preview, position: Int, previewThumbnailClickListener: (Preview, Int) -> Unit) {
        itemView.run {
            when (item.rotation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> preview_thumbnail_item_image_view.rotation = 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> preview_thumbnail_item_image_view.rotation = 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> preview_thumbnail_item_image_view.rotation = 270f
            }
            item.thumbnailBitmap?.let {
                preview_thumbnail_item_image_view.setImageBitmap(it)
            } ?: run {
                preview_thumbnail_item_image_view.setImageURI(item.thumbnailImageUri ?: return@onBind)
            }
            preview_thumbnail_item_layout.setOnClickListener {
                previewThumbnailClickListener(item, position)
            }
        }
    }
}