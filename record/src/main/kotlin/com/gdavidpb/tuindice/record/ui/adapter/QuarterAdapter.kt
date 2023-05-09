package com.gdavidpb.tuindice.record.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import com.gdavidpb.tuindice.base.ui.adapter.BaseAdapter
import com.gdavidpb.tuindice.base.ui.viewholder.BaseViewHolder
import com.gdavidpb.tuindice.base.utils.extension.view
import com.gdavidpb.tuindice.record.R
import com.gdavidpb.tuindice.record.presentation.model.QuarterItem
import com.gdavidpb.tuindice.record.presentation.model.SubjectItem
import com.gdavidpb.tuindice.record.ui.viewholder.QuarterViewHolder
import kotlin.math.roundToInt

class QuarterAdapter(
	private val manager: AdapterManager
) : BaseAdapter<QuarterItem>() {

	init {
		setHasStableIds(true)
	}

	interface AdapterManager {
		fun onSubjectOptionsClicked(item: SubjectItem)
		fun onSubjectGradeChanged(item: SubjectItem, grade: Int, isSelected: Boolean)
	}

	private val averageSubjects by lazy {
		if (currentList.isNotEmpty())
			(currentList.sumOf { it.subjectsItems.size } / currentList.size.toFloat()).roundToInt()
		else
			0
	}

	override fun provideComparator() = compareBy(QuarterItem::id)

	override fun getItemId(position: Int) = currentList[position].uid

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<QuarterItem> {
		val itemView = LayoutInflater
			.from(parent.context)
			.inflate(R.layout.item_quarter, parent, false)

		/* Default inflation */
		val lLayoutQuarterContainer by itemView.view<LinearLayout>(R.id.lLayoutQuarterContainer)

		repeat(averageSubjects) {
			LayoutInflater
				.from(lLayoutQuarterContainer.context)
				.inflate(R.layout.item_subject, lLayoutQuarterContainer)
		}

		return QuarterViewHolder(itemView, manager)
	}
}