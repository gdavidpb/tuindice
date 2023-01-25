package com.gdavidpb.tuindice.base.ui.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BaseViewHolder<T : Any>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private lateinit var item: T

    fun getItem(): T {
        return item
    }

    open fun bindView(item: T) {
        this.item = item
    }
}