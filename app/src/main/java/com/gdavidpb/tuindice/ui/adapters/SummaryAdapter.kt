package com.gdavidpb.tuindice.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.presentation.model.SummaryItemBase
import com.gdavidpb.tuindice.ui.adapters.base.BaseAdapter
import com.gdavidpb.tuindice.ui.viewholders.SummaryCreditsViewHolder
import com.gdavidpb.tuindice.ui.viewholders.SummarySubjectsViewHolder
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder
import com.gdavidpb.tuindice.utils.VIEW_TYPE_SUMMARY_CREDITS
import com.gdavidpb.tuindice.utils.VIEW_TYPE_SUMMARY_SUBJECTS
import com.gdavidpb.tuindice.utils.mappers.toSummaryCreditsItem
import com.gdavidpb.tuindice.utils.mappers.toSummarySubjectsItem

open class SummaryAdapter : BaseAdapter<SummaryItemBase>() {

    override fun provideComparator() = compareBy(SummaryItemBase::hashCode)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<SummaryItemBase> {
        val layout = when (viewType) {
            VIEW_TYPE_SUMMARY_SUBJECTS -> R.layout.item_summary_subjects
            VIEW_TYPE_SUMMARY_CREDITS -> R.layout.item_summary_credits
            else -> throw IllegalArgumentException("viewType: '$viewType'")
        }

        val itemView = LayoutInflater.from(parent.context).inflate(layout, parent, false)

        return when (viewType) {
            VIEW_TYPE_SUMMARY_SUBJECTS -> SummarySubjectsViewHolder(itemView)
            VIEW_TYPE_SUMMARY_CREDITS -> SummaryCreditsViewHolder(itemView)
            else -> throw IllegalArgumentException("viewType: '$viewType'")
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

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> VIEW_TYPE_SUMMARY_SUBJECTS
            1 -> VIEW_TYPE_SUMMARY_CREDITS
            else -> throw  IllegalArgumentException("position: '$position'")
        }
    }
}