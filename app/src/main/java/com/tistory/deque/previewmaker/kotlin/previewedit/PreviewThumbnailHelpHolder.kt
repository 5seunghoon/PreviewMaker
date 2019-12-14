package com.tistory.deque.previewmaker.kotlin.previewedit

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.tistory.deque.previewmaker.R
import com.tistory.deque.previewmaker.kotlin.model.Preview
import kotlinx.android.synthetic.main.kt_preview_thumbnail_help_item.view.*
import kotlinx.android.synthetic.main.kt_preview_thumbnail_item.view.*

class PreviewThumbnailHelpHolder(parent:ViewGroup): RecyclerView.ViewHolder(
        LayoutInflater
                .from(parent.context)
                .inflate(R.layout.kt_preview_thumbnail_help_item, parent, false)
){
    fun onBind(previewThumbnailHelpClickListener: () -> Unit) {
        itemView.run {
            preview_thumbnail_help_item_layout.setOnClickListener {
                previewThumbnailHelpClickListener()
            }
        }
    }
}