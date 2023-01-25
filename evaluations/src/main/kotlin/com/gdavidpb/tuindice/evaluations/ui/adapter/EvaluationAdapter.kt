package com.gdavidpb.tuindice.evaluations.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationItem
import com.gdavidpb.tuindice.base.ui.adapter.BaseAdapter
import com.gdavidpb.tuindice.base.ui.viewholder.BaseViewHolder
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.ui.viewholder.EvaluationViewHolder

class EvaluationAdapter(
	private val manager: AdapterManager
) : BaseAdapter<EvaluationItem>() {

	init {
		setHasStableIds(true)
	}

	private val adapterSorter = compareBy(
		EvaluationItem::isDone,
		EvaluationItem::date
	)

	interface AdapterManager {
		fun onEvaluationClicked(item: EvaluationItem)
		fun onEvaluationOptionsClicked(item: EvaluationItem, position: Int)
		fun onEvaluationGradeChanged(item: EvaluationItem, grade: Double)
		fun onEvaluationDoneChanged(item: EvaluationItem, done: Boolean)

		fun onEvaluationAdded(item: EvaluationItem)
		fun onEvaluationRemoved(item: EvaluationItem)
		fun onEvaluationUpdated(item: EvaluationItem)

		fun onSubmitEvaluations(items: List<EvaluationItem>)
	}

	override fun provideComparator() = compareBy(EvaluationItem::id)

	override fun onCreateViewHolder(
		parent: ViewGroup,
		viewType: Int
	): BaseViewHolder<EvaluationItem> {
		val itemView = LayoutInflater
			.from(parent.context)
			.inflate(R.layout.item_evaluation, parent, false)

		return EvaluationViewHolder(itemView, manager)
	}

	override fun getItemId(position: Int): Long {
		return currentList[position].uid
	}

	fun submitEvaluations(items: List<EvaluationItem>) {
		submitSortedList(items)
		manager.onSubmitEvaluations(items)
	}

	fun addEvaluation(item: EvaluationItem, position: Int) {
		addItem(item, position)
		submitSortedList()
		manager.onEvaluationAdded(item)
	}

	fun removeEvaluation(item: EvaluationItem) {
		removeItem(item)
		manager.onEvaluationRemoved(item)
	}

	fun updateEvaluation(item: EvaluationItem) {
		updateItem(item)
		submitSortedList()
		manager.onEvaluationUpdated(item)
	}

	fun computeGradeSum(): Double {
		return currentList.sumOf { evaluation -> evaluation.data.grade }
	}

	private fun submitSortedList(list: List<EvaluationItem> = currentList) {
		super.submitList(list.sortedWith(adapterSorter))
	}
}