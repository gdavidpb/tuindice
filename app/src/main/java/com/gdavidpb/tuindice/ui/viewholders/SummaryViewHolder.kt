package com.gdavidpb.tuindice.ui.viewholders

import android.view.View
import com.gdavidpb.tuindice.presentation.model.SummaryItem
import com.gdavidpb.tuindice.ui.customs.StatusCardView
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder

open class SummaryViewHolder(
        itemView: View
) : BaseViewHolder<SummaryItem, Nothing>(itemView) {
    override fun bindView(item: SummaryItem) {
        super.bindView(item)

        itemView as StatusCardView

        with(itemView) {
            headerText = item.headerText
            approvedCount = item.approved
            failedCount = item.failed
            retiredCount = item.retired

            notifyChanges()
        }
    }
}