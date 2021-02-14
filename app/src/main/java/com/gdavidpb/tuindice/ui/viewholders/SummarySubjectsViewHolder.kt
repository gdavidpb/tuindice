package com.gdavidpb.tuindice.ui.viewholders

import android.view.View
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.presentation.model.SummaryItemBase
import com.gdavidpb.tuindice.presentation.model.SummarySubjectsItem
import com.gdavidpb.tuindice.ui.customs.StatusCardView
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder

open class SummarySubjectsViewHolder(itemView: View) : BaseViewHolder<SummaryItemBase>(itemView) {
    override fun bindView(item: SummaryItemBase) {
        item as SummarySubjectsItem

        with(itemView as StatusCardView) {
            val subjectsHeader = context.resources.getQuantityString(
                    R.plurals.text_subjects_header,
                    item.enrolledSubjects,
                    item.enrolledSubjects
            )

            headerText = subjectsHeader
            approvedCount = item.approvedSubjects
            failedCount = item.failedSubjects
            retiredCount = item.retiredSubjects

            notifyChanges()
        }
    }
}