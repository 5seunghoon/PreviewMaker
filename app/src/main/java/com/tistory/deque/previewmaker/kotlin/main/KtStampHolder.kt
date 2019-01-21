package com.tistory.deque.previewmaker.kotlin.main

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.tistory.deque.previewmaker.R
import com.tistory.deque.previewmaker.kotlin.model.Stamp
import kotlinx.android.synthetic.main.list_item.view.*

class KtStampHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater
                .from(parent.context)
                .inflate(R.layout.list_item, parent, false)
) {
    fun onBind(stampItem: Stamp) {
        itemView.run {
            stampListImageView.run { post { setImageURI(stampItem.imageUri) } }
            stampListTextView.run { post { text = stampItem.name } }
        }
    }
}