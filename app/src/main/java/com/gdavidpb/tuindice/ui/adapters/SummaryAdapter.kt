package com.gdavidpb.tuindice.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.presentation.model.SummaryItem
import com.gdavidpb.tuindice.ui.adapters.base.BaseAdapter
import com.gdavidpb.tuindice.ui.viewholders.SummaryViewHolder
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder

class SummaryAdapter : BaseAdapter<SummaryItem>() {

    private enum class ViewType { SUBJECTS, CREDITS }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<SummaryItem> {
        val layout = when (ViewType.values()[viewType]) {
            ViewType.SUBJECTS -> R.layout.item_summary_subjects
            ViewType.CREDITS -> R.layout.item_summary_credits
        }

        val itemView = LayoutInflater
                .from(parent.context)
                .inflate(layout, parent, false)

        return SummaryViewHolder(itemView)
    }

    fun submitSummary(items: List<SummaryItem>) {
        submitList(items)
    }

    override fun getItemViewType(position: Int) = when (position) {
        0 -> ViewType.SUBJECTS.ordinal
        1 -> ViewType.CREDITS.ordinal
        else -> throw NoWhenBranchMatchedException("position: '$position'")
    }
}