package com.gdavidpb.tuindice.ui.viewholders

import android.view.View
import com.gdavidpb.tuindice.presentation.model.SummaryItem
import com.gdavidpb.tuindice.ui.customs.StatusCardView
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder

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