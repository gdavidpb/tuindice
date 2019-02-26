package com.gdavidpb.tuindice.ui.viewholders

import android.view.View
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.data.model.database.SummaryBase
import com.gdavidpb.tuindice.data.model.database.SummarySubjects
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder
import com.gdavidpb.tuindice.utils.animatePercent
import kotlinx.android.synthetic.main.item_summary_subjects.view.*

open class SummarySubjectsViewHolder(itemView: View) : BaseViewHolder<SummaryBase>(itemView) {
    override fun bindView(item: SummaryBase) {
        item as SummarySubjects

        with(itemView) {
            val total = item.enrolledSubjects.toFloat()

            val approvedPercent = item.approvedSubjects / total
            val failedPercent = item.failedSubjects / total

            tViewSubjectsHeader.text = context.getString(R.string.text_subjects_header, item.enrolledSubjects)

            guidelineApproved.animatePercent(approvedPercent)
            guidelineFailed.animatePercent(approvedPercent + failedPercent)
            guidelineRetired.animatePercent(1f)

            mapOf(
                    tViewApproved to item.approvedSubjects,
                    tViewFailed to item.failedSubjects,
                    tViewRetired to item.retiredSubjects
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