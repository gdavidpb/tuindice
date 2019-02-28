package com.gdavidpb.tuindice.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.presentation.model.SummaryBase
import com.gdavidpb.tuindice.presentation.model.SummaryCredits
import com.gdavidpb.tuindice.presentation.model.SummaryHeader
import com.gdavidpb.tuindice.presentation.model.SummarySubjects
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.ui.adapters.base.BaseAdapter
import com.gdavidpb.tuindice.ui.viewholders.SummaryCreditsViewHolder
import com.gdavidpb.tuindice.ui.viewholders.SummaryHeaderViewHolder
import com.gdavidpb.tuindice.ui.viewholders.SummarySubjectsViewHolder
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder
import com.gdavidpb.tuindice.utils.*
import com.squareup.picasso.Picasso

open class SummaryAdapter(
        private val callback: AdapterCallback
) : BaseAdapter<SummaryBase>() {

    interface AdapterCallback {
        fun provideImageLoader(provider: (picasso: Picasso) -> Unit)
    }

    override fun provideComparator() = Comparator<SummaryBase> { o1, o2 ->
        if (o1::class == o2::class)
            when (o1) {
                is SummaryHeader -> {
                    o2 as SummaryHeader

                    o1.uid.compareTo(o2.uid)
                }
                is SummarySubjects -> {
                    o2 as SummarySubjects

                    if (o1 == o2) 0 else -1
                }
                is SummaryCredits -> {
                    o2 as SummaryCredits

                    if (o1 == o2) 0 else -1
                }
                else -> -1
            }
        else
            -1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<SummaryBase> {
        val layout = when (viewType) {
            VIEW_TYPE_SUMMARY_HEADER -> R.layout.item_summary_header
            VIEW_TYPE_SUMMARY_SUBJECTS -> R.layout.item_summary_subjects
            VIEW_TYPE_SUMMARY_CREDITS -> R.layout.item_summary_credits
            else -> throw IllegalStateException()
        }

        val itemView = LayoutInflater.from(parent.context).inflate(layout, parent, false)

        return when (viewType) {
            VIEW_TYPE_SUMMARY_HEADER -> SummaryHeaderViewHolder(itemView, callback)
            VIEW_TYPE_SUMMARY_SUBJECTS -> SummarySubjectsViewHolder(itemView)
            VIEW_TYPE_SUMMARY_CREDITS -> SummaryCreditsViewHolder(itemView)
            else -> throw IllegalStateException()
        }
    }

    fun setAccount(account: Account) {
        val header = account.toSummaryHeader()
        val subjects = account.toSummarySubjects()
        val credits = account.toSummaryCredits()

        val items = mutableListOf<SummaryBase>(header)

        if (subjects.enrolledSubjects > 0)
            items.add(subjects)

        if (credits.enrolledCredits > 0)
            items.add(credits)

        swapItems(new = items)
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> VIEW_TYPE_SUMMARY_HEADER
            1 -> VIEW_TYPE_SUMMARY_SUBJECTS
            2 -> VIEW_TYPE_SUMMARY_CREDITS
            else -> throw IllegalStateException()
        }
    }
}