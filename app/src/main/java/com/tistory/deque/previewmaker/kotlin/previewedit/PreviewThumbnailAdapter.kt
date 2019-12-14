package com.tistory.deque.previewmaker.kotlin.previewedit

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import com.tistory.deque.previewmaker.kotlin.model.Preview
import com.tistory.deque.previewmaker.kotlin.model.PreviewListModel

class PreviewThumbnailAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var previewListModel: PreviewListModel? = null
    var previewThumbnailClickListener: (Preview, Int) -> Unit = { _, _ -> }
    var previewThumbnailHelpClickListener: () -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder{
        return when(viewType) {
            0 -> PreviewThumbnailHolder(parent)
            else -> PreviewThumbnailHelpHolder(parent)
        }
    }

    override fun getItemCount(): Int = (previewListModel?.size ?: -1) + 1

    override fun getItemViewType(position: Int): Int {
        return if(previewListModel?.size != position) 0 //일반적인 아이템
        else 1 // 맨 끝에 있는 아이템 (도움말아이템)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? PreviewThumbnailHolder)?.onBind(previewListModel?.getPreview(position)
                ?: return, position, previewThumbnailClickListener)
        (holder as? PreviewThumbnailHelpHolder)?.onBind(previewThumbnailHelpClickListener)
    }
}
