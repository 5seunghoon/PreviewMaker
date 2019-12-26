package com.tistory.deque.previewmaker.kotlin.main

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.tistory.deque.previewmaker.R
import com.tistory.deque.previewmaker.kotlin.model.Stamp
import kotlinx.android.synthetic.main.kt_toolbox_item_layout.view.*

class KtToolboxHolder(parent: ViewGroup, private val toolboxClickListener: ToolboxClickListener?) : RecyclerView.ViewHolder(
        LayoutInflater
                .from(parent.context)
                .inflate(R.layout.kt_toolbox_item_layout, parent, false)
) {
    fun onBind() {
        itemView.run {
            toolbox_option_button.setOnClickListener {
                toolboxClickListener?.optionClickListener()
            }
            toolbox_credit_button.setOnClickListener {
                toolboxClickListener?.creditClickListener()
            }
            toolbox_help_button.setOnClickListener {
                toolboxClickListener?.helpClickListener()
            }
        }
    }
}