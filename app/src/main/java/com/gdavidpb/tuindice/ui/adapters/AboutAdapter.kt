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
import com.gdavidpb.tuindice.utils.VIEW_TYPE_ABOUT
import com.gdavidpb.tuindice.utils.VIEW_TYPE_ABOUT_HEADER

open class AboutAdapter : BaseAdapter<AboutItemBase>() {

    override fun provideComparator() = compareBy(AboutItemBase::hashCode)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<AboutItemBase> {
        val layout = when (viewType) {
            VIEW_TYPE_ABOUT -> R.layout.item_about
            VIEW_TYPE_ABOUT_HEADER -> R.layout.item_about_header
            else -> throw IllegalArgumentException("viewType: '$viewType'")
        }

        val itemView = LayoutInflater.from(parent.context).inflate(layout, parent, false)

        return when (viewType) {
            VIEW_TYPE_ABOUT -> AboutViewHolder(itemView)
            VIEW_TYPE_ABOUT_HEADER -> AboutHeaderViewHolder(itemView)
            else -> throw IllegalArgumentException("viewType: '$viewType'")
        }
    }

    override fun getItemViewType(position: Int) = when (items[position]) {
        is AboutItem -> VIEW_TYPE_ABOUT
        is AboutHeaderItem -> VIEW_TYPE_ABOUT_HEADER
        else -> throw IllegalArgumentException("viewType: '${items[position]}'")
    }
}