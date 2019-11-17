package com.gdavidpb.tuindice.ui.viewholders

import android.view.View
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.presentation.model.SummaryBase
import com.gdavidpb.tuindice.presentation.model.SummaryCredits
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder
import com.gdavidpb.tuindice.utils.animatePercent
import kotlinx.android.synthetic.main.item_summary_credits.view.*

open class SummaryCreditsViewHolder(itemView: View) : BaseViewHolder<SummaryBase>(itemView) {
    override fun bindView(item: SummaryBase) {
        item as SummaryCredits

        with(itemView) {
            val total = item.enrolledCredits.toFloat()

            val approvedPercent = item.approvedCredits / total
            val failedPercent = item.failedCredits / total

            val creditsHeader = context.resources.getQuantityString(
                    R.plurals.text_credits_header,
                    item.enrolledCredits,
                    item.enrolledCredits)

            tViewCreditsHeader.text = creditsHeader

            guidelineApproved.animatePercent(approvedPercent)
            guidelineFailed.animatePercent(approvedPercent + failedPercent)
            guidelineRetired.animatePercent(1f)

            mapOf(
                    tViewApproved to item.approvedCredits,
                    tViewFailed to item.failedCredits,
                    tViewRetired to item.retiredCredits
            ).forEach { (textView, value) ->
                if (value > 0) {
                    textView.text = "$value"
                    textView.visibility = View.VISIBLE
                } else
                    textView.visibility = View.GONE
            }
        }
    }
}