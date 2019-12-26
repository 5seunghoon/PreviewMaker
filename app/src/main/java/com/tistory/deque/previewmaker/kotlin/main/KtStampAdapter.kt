package com.tistory.deque.previewmaker.kotlin.main

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import com.tistory.deque.previewmaker.kotlin.model.Stamp
import com.tistory.deque.previewmaker.kotlin.model.StampAdapterModel

class KtStampAdapter(private val stampAdapterModel: StampAdapterModel) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ItemViewType(val type: Int) {
        STAMP(0), TOOLBOX(1);
    }

    var stampClickListener: (Stamp, Int) -> Unit = { _, _ -> }
    var stampDeleteListener: (Stamp, Int) -> Unit = { _, _ -> }
    var toolboxClickListener: ToolboxClickListener? = null

    val size:Int get() = stampAdapterModel.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            ItemViewType.TOOLBOX.type -> KtToolboxHolder(parent, toolboxClickListener)
            else -> KtStampHolder(parent, stampClickListener, stampDeleteListener)
        }
    }

    override fun getItemCount() = stampAdapterModel.size + 1

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            ItemViewType.TOOLBOX.type
        } else {
            ItemViewType.STAMP.type
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val stampPos = position - 1

        when (holder) {
            is KtStampHolder -> holder.onBind(stampAdapterModel.getItem(stampPos), stampPos)
            is KtToolboxHolder -> holder.onBind()
        }
    }

    fun copyStampList(stampList: ArrayList<Stamp>) {
        stampAdapterModel.copyList(stampList)
        notifyDataSetChanged()
    }

    fun addStamp(stamp: Stamp) {
        stampAdapterModel.addStamp(stamp)
        notifyItemInserted(stampAdapterModel.size)
    }

    fun delStamp(position:Int){
        stampAdapterModel.delStamp(position)
        notifyDataSetChanged()
    }

}