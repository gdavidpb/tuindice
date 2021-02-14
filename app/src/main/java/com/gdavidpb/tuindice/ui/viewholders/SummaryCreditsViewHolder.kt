package com.gdavidpb.tuindice.ui.viewholders

import android.view.View
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.presentation.model.SummaryCreditsItem
import com.gdavidpb.tuindice.presentation.model.SummaryItemBase
import com.gdavidpb.tuindice.ui.customs.StatusCardView
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder

open class SummaryCreditsViewHolder(itemView: View) : BaseViewHolder<SummaryItemBase>(itemView) {
    override fun bindView(item: SummaryItemBase) {
        item as SummaryCreditsItem

        with(itemView as StatusCardView) {
            val creditsHeader = context.resources.getQuantityString(
                    R.plurals.text_credits_header,
                    item.enrolledCredits,
                    item.enrolledCredits)

            headerText = creditsHeader
            approvedCount = item.approvedCredits
            failedCount = item.failedCredits
            retiredCount = item.retiredCredits

            notifyChanges()
        }
    }
}