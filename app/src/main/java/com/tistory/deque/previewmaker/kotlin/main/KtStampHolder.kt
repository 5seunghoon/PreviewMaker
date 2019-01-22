package com.tistory.deque.previewmaker.kotlin.main

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.tistory.deque.previewmaker.R
import com.tistory.deque.previewmaker.kotlin.model.Stamp
import kotlinx.android.synthetic.main.kt_stamp_list_item.view.*
import kotlinx.android.synthetic.main.list_item.view.*

class KtStampHolder(parent: ViewGroup, val clickStamp:(Stamp)->Unit, val delStamp:(Stamp)->Unit ) : RecyclerView.ViewHolder(
        LayoutInflater
                .from(parent.context)
                .inflate(R.layout.kt_stamp_list_item, parent, false)
) {
    fun onBind(stampItem: Stamp) {
        itemView.run {
            stamp_item_image_view.run { post { setImageURI(stampItem.imageUri) } }
            stamp_item_name_text_view.run { post { text = stampItem.name } }

            stamp_item_select_layout.setOnClickListener {
                clickStamp(stampItem)
            }
        }
    }
}