package com.tistory.deque.previewmaker.kotlin.main

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.tistory.deque.previewmaker.R
import com.tistory.deque.previewmaker.kotlin.model.Stamp
import kotlinx.android.synthetic.main.kt_stamp_list_item.view.*

class KtStampHolder(parent: ViewGroup, val clickStamp:(Stamp, Int)->Unit, val delStamp:(Stamp, Int)->Unit ) : RecyclerView.ViewHolder(
        LayoutInflater
                .from(parent.context)
                .inflate(R.layout.kt_stamp_list_item, parent, false)
) {
    fun onBind(stampItem: Stamp, position: Int) {
        itemView.run {
            stamp_item_image_view.run { post { setImageURI(stampItem.imageUri) } }
            stamp_item_name_text_view.run { post { text = stampItem.name } }

            stamp_item_select_layout.setOnClickListener {
                clickStamp(stampItem, position)
            }
            stamp_item_delete_button.setOnClickListener {
                delStamp(stampItem, position)
            }
        }
    }
}