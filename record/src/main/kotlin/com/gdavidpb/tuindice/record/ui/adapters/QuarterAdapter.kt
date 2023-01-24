package com.gdavidpb.tuindice.record.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.gdavidpb.tuindice.base.ui.adapters.BaseAdapter
import com.gdavidpb.tuindice.base.ui.viewholders.BaseViewHolder
import com.gdavidpb.tuindice.record.R
import com.gdavidpb.tuindice.record.presentation.model.QuarterItem
import com.gdavidpb.tuindice.record.presentation.model.SubjectItem
import com.gdavidpb.tuindice.record.ui.viewholders.QuarterViewHolder
import kotlinx.android.synthetic.main.item_quarter.view.*
import kotlin.math.roundToInt

class QuarterAdapter(
	private val manager: AdapterManager
) : BaseAdapter<QuarterItem>() {

	init {
		setHasStableIds(true)
	}

	interface AdapterManager {
		fun onSubjectOptionsClicked(quarterItem: QuarterItem, subjectItem: SubjectItem)
		fun onSubjectGradeChanged(subjectItem: SubjectItem, grade: Int, dispatchChanges: Boolean)

		fun onSubmitQuarters(items: List<QuarterItem>)
	}

	private val averageSubjects by lazy {
		if (currentList.isNotEmpty())
			(currentList.sumOf { it.subjectsItems.size } / currentList.size.toFloat()).roundToInt()
		else
			0
	}

	override fun provideComparator() = compareBy(QuarterItem::id)

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<QuarterItem> {
		val itemView = LayoutInflater
			.from(parent.context)
			.inflate(R.layout.item_quarter, parent, false)

		/* Default inflation */
		with(itemView) {
			repeat(averageSubjects) {
				LayoutInflater
					.from(lLayoutQuarterContainer.context)
					.inflate(R.layout.item_subject, lLayoutQuarterContainer)
			}
		}

		return QuarterViewHolder(itemView, manager)
	}

	override fun getItemId(position: Int): Long {
		return currentList[position].uid
	}

	fun submitQuarters(items: List<QuarterItem>) {
		submitList(items)
		manager.onSubmitQuarters(items)
	}

	fun getQuarter(position: Int): QuarterItem {
		return getItem(position)
	}

	fun addQuarter(item: QuarterItem, position: Int) {
		addItem(item, position)
	}

	fun removeQuarter(item: QuarterItem) {
		removeItem(item)
	}

	fun updateQuarter(item: QuarterItem) {
		updateItem(item)
	}
}