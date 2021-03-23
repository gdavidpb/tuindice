package com.gdavidpb.tuindice.ui.viewholders.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BaseViewHolder<T : Any, Q : Any>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private lateinit var item: T

    fun getItem(): T {
        return item
    }

    open fun bindView(item: T) {
        this.item = item
    }

    open fun bindPayload(item: T, payload: List<Q>) {
        this.item = item
    }
}