package com.gdavidpb.tuindice.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.presentation.model.SummaryCreditsItem
import com.gdavidpb.tuindice.presentation.model.SummaryItemBase
import com.gdavidpb.tuindice.presentation.model.SummarySubjectsItem
import com.gdavidpb.tuindice.ui.adapters.base.BaseAdapter
import com.gdavidpb.tuindice.ui.viewholders.SummaryCreditsViewHolder
import com.gdavidpb.tuindice.ui.viewholders.SummarySubjectsViewHolder
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder
import com.gdavidpb.tuindice.utils.mappers.toSummaryCreditsItem
import com.gdavidpb.tuindice.utils.mappers.toSummarySubjectsItem

class SummaryAdapter : BaseAdapter<SummaryItemBase>() {

    private enum class ViewType { SUBJECTS, CREDITS }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<SummaryItemBase> {
        val viewTypeClass = ViewType.values()[viewType]

        val layout = when (viewTypeClass) {
            ViewType.SUBJECTS -> R.layout.item_summary_subjects
            ViewType.CREDITS -> R.layout.item_summary_credits
        }

        val itemView = LayoutInflater.from(parent.context).inflate(layout, parent, false)

        return when (viewTypeClass) {
            ViewType.SUBJECTS -> SummarySubjectsViewHolder(itemView)
            ViewType.CREDITS -> SummaryCreditsViewHolder(itemView)
        }
    }

    fun setAccount(account: Account) {
        val subjects = account.toSummarySubjectsItem()
        val credits = account.toSummaryCreditsItem()

        val items = mutableListOf<SummaryItemBase>()

        if (subjects.enrolledSubjects > 0)
            items.add(subjects)

        if (credits.enrolledCredits > 0)
            items.add(credits)

        swapItems(new = items)
    }

    override fun getItemViewType(position: Int) = when (items[position]) {
        is SummarySubjectsItem -> ViewType.SUBJECTS.ordinal
        is SummaryCreditsItem -> ViewType.CREDITS.ordinal
        else -> throw NoWhenBranchMatchedException("position: '$position'")
    }
}