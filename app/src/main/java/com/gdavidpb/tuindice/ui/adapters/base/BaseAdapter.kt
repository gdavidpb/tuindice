package com.gdavidpb.tuindice.ui.adapters.base

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder

abstract class BaseAdapter<T> : RecyclerView.Adapter<BaseViewHolder<T>>() {
    protected val items = mutableListOf<T>()

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: BaseViewHolder<T>, position: Int) {
        val item = items[position]

        holder.bindView(item)
    }

    abstract fun provideComparator(): Comparator<T>

    open fun swapItems(new: List<T>) {
        val diffUtil = GenericDiffUtil(old = items, new = new, comparator = provideComparator())

        DiffUtil.calculateDiff(diffUtil, true).dispatchUpdatesTo(this)

        items.clear()
        items.addAll(new)
    }

    open fun getItem(position: Int): T {
        return items[position]
    }

    open fun removeItemAt(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    open fun addItemAt(item: T, position: Int) {
        items.add(position, item)
        notifyItemInserted(position)
    }

    private inner class GenericDiffUtil(
            private val old: List<T>,
            private val new: List<T>,
            private val comparator: Comparator<T>
    ) : DiffUtil.Callback() {

        override fun getOldListSize() = old.size

        override fun getNewListSize() = new.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = old[oldItemPosition]
            val newItem = new[newItemPosition]

            return comparator.compare(oldItem, newItem) == 0
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = old[oldItemPosition]
            val newItem = new[newItemPosition]

            return oldItem == newItem
        }
    }
}