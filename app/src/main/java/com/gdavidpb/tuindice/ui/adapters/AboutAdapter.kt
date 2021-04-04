package com.gdavidpb.tuindice.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.presentation.model.AboutHeaderItem
import com.gdavidpb.tuindice.presentation.model.AboutItem
import com.gdavidpb.tuindice.presentation.model.AboutItemBase
import com.gdavidpb.tuindice.ui.adapters.base.BaseAdapter
import com.gdavidpb.tuindice.ui.viewholders.AboutHeaderViewHolder
import com.gdavidpb.tuindice.ui.viewholders.AboutViewHolder
import com.gdavidpb.tuindice.ui.viewholders.base.BaseViewHolder

class AboutAdapter : BaseAdapter<AboutItemBase>() {

    private enum class ViewType { HEADER, ABOUT }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<AboutItemBase> {
        val viewTypeClass = ViewType.values()[viewType]

        val layout = when (viewTypeClass) {
            ViewType.ABOUT -> R.layout.item_about
            ViewType.HEADER -> R.layout.item_about_header
        }

        val itemView = LayoutInflater
                .from(parent.context)
                .inflate(layout, parent, false)

        return when (viewTypeClass) {
            ViewType.ABOUT -> AboutViewHolder(itemView)
            ViewType.HEADER -> AboutHeaderViewHolder(itemView)
        }
    }

    fun submitAbout(items: List<AboutItemBase>) {
        submitList(items)
    }

    override fun getItemViewType(position: Int) = when (currentList[position]) {
        is AboutItem -> ViewType.ABOUT.ordinal
        is AboutHeaderItem -> ViewType.HEADER.ordinal
        else -> throw NoWhenBranchMatchedException("viewType: '${currentList[position]}'")
    }
}