package com.gdavidpb.tuindice.ui.adapters.base

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder
import java.util.Collections.synchronizedList

abstract class BaseAdapter<T : Any> : RecyclerView.Adapter<BaseViewHolder<T>>() {

    protected val currentList: MutableList<T> = synchronizedList(mutableListOf<T>())

    open fun provideComparator(): Comparator<T> {
        return Comparator { _, _ -> 0 }
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder<T>, position: Int) {
        val item = currentList[position]

        holder.bindView(item)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: BaseViewHolder<T>, position: Int, payloads: MutableList<Any>) {
        val item = if (payloads.isEmpty()) currentList[position] else payloads.first() as T

        holder.bindView(item)
    }

    open fun submitList(list: List<T>) {
        val diffUtil = ListDiffUtil(oldList = currentList, newList = list, comparator = provideComparator())
        val diffResult = DiffUtil.calculateDiff(diffUtil, true)

        currentList.clear()
        currentList.addAll(list)

        diffResult.dispatchUpdatesTo(this)
    }

    open fun getItem(position: Int): T {
        return currentList[position]
    }

    open fun addItem(item: T, position: Int = currentList.size, notify: Boolean = true) {
        currentList.add(position, item)

        if (notify) notifyItemInserted(position)
    }

    open fun removeItem(item: T, notify: Boolean = true) {
        val position = getItemPosition(item)

        currentList.removeAt(position)

        if (notify) notifyItemRemoved(position)
    }

    open fun updateItem(item: T) {
        val position = getItemPosition(item)

        currentList[position] = item

        notifyItemChanged(position, item)
    }

    @Deprecated("Remove after migration")
    open fun removeItemAt(position: Int, notifyChange: Boolean = true) {
        currentList.removeAt(position)

        if (notifyChange) notifyItemRemoved(position)
    }

    @Deprecated("Remove after migration")
    open fun addItemAt(item: T, position: Int, notifyChange: Boolean = true) {
        currentList.add(position, item)

        if (notifyChange) notifyItemInserted(position)
    }

    @Deprecated("Remove after migration")
    open fun replaceItemAt(item: T, position: Int, notifyChange: Boolean = true) {
        currentList[position] = item

        if (notifyChange) notifyItemChanged(position)
    }

    open fun getItemPosition(item: T): Int {
        val comparator = provideComparator()

        return currentList.indexOfFirst { comparator.compare(it, item) == 0 }
    }

    private class ListDiffUtil<Q>(
            private val oldList: List<Q>,
            private val newList: List<Q>,
            private val comparator: Comparator<Q>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]

            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]

            return comparator.compare(oldItem, newItem) == 0
        }
    }
}