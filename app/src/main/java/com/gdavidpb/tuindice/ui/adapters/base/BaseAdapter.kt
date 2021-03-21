package com.gdavidpb.tuindice.ui.adapters.base

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder
import java.util.Collections.synchronizedList

abstract class BaseAdapter<T : Any, Q : Any> : RecyclerView.Adapter<BaseViewHolder<T, Q>>() {

    protected val currentList: MutableList<T> = synchronizedList(mutableListOf<T>())

    open fun provideComparator(): Comparator<T> {
        return Comparator { _, _ -> 0 }
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder<T, Q>, position: Int) {
        val item = currentList[position]

        holder.bindView(item)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: BaseViewHolder<T, Q>, position: Int, payloads: MutableList<Any>) {
        val item = currentList[position]

        if (payloads.isEmpty()) {
            holder.bindView(item)
        } else {
            val params = payloads as List<Any>
            val payload = params[0] as Q

            holder.bindPayload(item, payload)
        }
    }

    open fun notifyItemPayload(position: Int, payload: Q) {
        notifyItemChanged(position, payload)
    }

    open fun submitList(list: List<T>) {
        val oldListIsEmpty = currentList.isEmpty()
        val newListIsEmpty = list.isEmpty()

        val diffUtil = ListDiffUtil(oldList = currentList, newList = list, comparator = provideComparator())
        val diffResult = DiffUtil.calculateDiff(diffUtil, true)

        currentList.clear()
        currentList.addAll(list)

        diffResult.dispatchUpdatesTo(this)

        if (oldListIsEmpty && newListIsEmpty) notifyDataSetChanged()
    }

    open fun getItem(position: Int): T {
        return currentList[position]
    }

    open fun addItem(item: T, position: Int = currentList.size) {
        currentList.add(position, item)

        notifyItemInserted(position)
    }

    open fun removeItem(item: T) {
        val position = getItemPosition(item)

        currentList.removeAt(position)

        notifyItemRemoved(position)
    }

    open fun updateItem(item: T, update: T.() -> T) {
        val position = getItemPosition(item)

        currentList[position] = currentList[position].update()

        notifyItemChanged(position)
    }

    open fun updateItem(item: T, notify: Boolean = true) {
        val position = getItemPosition(item)

        currentList[position] = item

        if (notify) notifyItemChanged(position)
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
    fun replaceItem(item: T, notifyChange: Boolean = true) {
        val comparator = provideComparator()

        currentList.indexOfFirst {
            comparator.compare(item, it) == 0
        }.also { position ->
            if (position != -1) replaceItemAt(item, position, notifyChange)
        }
    }

    @Deprecated("Remove after migration")
    open fun replaceItemAt(item: T, position: Int, notifyChange: Boolean = true) {
        currentList[position] = item

        if (notifyChange) notifyItemChanged(position)
    }

    open fun getItemPosition(item: T): Int {
        check(hasStableIds()) { "In order to use modifiers by item you have to set up stable ids." }

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