package com.gdavidpb.tuindice.base.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.gdavidpb.tuindice.base.ui.viewholder.BaseViewHolder
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
	override fun onBindViewHolder(
		holder: BaseViewHolder<T>,
		position: Int,
		payloads: MutableList<Any>
	) {
		val item = if (payloads.isEmpty()) currentList[position] else payloads.first() as T

		holder.bindView(item)
	}

	fun submitList(list: List<T>) {
		val diffUtil =
			ListDiffUtil(oldList = currentList, newList = list, comparator = provideComparator())
		val diffResult = DiffUtil.calculateDiff(diffUtil, true)

		currentList.clear()
		currentList.addAll(list)

		diffResult.dispatchUpdatesTo(this)
	}

	fun isNotEmpty(): Boolean {
		return currentList.isNotEmpty()
	}

	fun getItem(position: Int): T {
		return currentList[position]
	}

	fun addItem(item: T, position: Int = currentList.size, notify: Boolean = true) {
		currentList.add(position, item)

		if (notify) notifyItemInserted(position)
	}

	fun removeItem(item: T, notify: Boolean = true) {
		val position = getItemPosition(item)

		if (position in currentList.indices) {
			currentList.removeAt(position)

			if (notify) notifyItemRemoved(position)
		}
	}

	fun updateItem(item: T) {
		val position = getItemPosition(item)

		if (position in currentList.indices) {
			currentList[position] = item

			notifyItemChanged(position, item)
		}
	}

	private fun getItemPosition(item: T): Int {
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