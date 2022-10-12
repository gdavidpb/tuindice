package com.gdavidpb.tuindice.summary.ui.viewholders

import android.view.View
import com.gdavidpb.tuindice.summary.ui.customs.StatusCardView
import com.gdavidpb.tuindice.base.ui.viewholders.BaseViewHolder
import com.gdavidpb.tuindice.summary.presentation.model.SummaryItem

class SummaryViewHolder(
	itemView: View
) : BaseViewHolder<SummaryItem>(itemView) {
	override fun bindView(item: SummaryItem) {
		super.bindView(item)

		with(itemView as StatusCardView) {
			headerText = item.headerText
			approvedCount = item.approved
			failedCount = item.failed
			retiredCount = item.retired

			notifyChanges()
		}
	}
}